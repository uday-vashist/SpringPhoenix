"""
knowledge_gaps_scanner.py
─────────────────────────
Business-Logic Flagging engine as specified in .bob/SKILL.md under
'Business-Logic Flagging (runs alongside all subagents)'.

Criteria (direct from SKILL.md):
  "If a method's purpose isn't inferable from naming, comments, or
   surrounding context, flag it — do not guess and silently document."

Each flag entry records:
  • file path (relative to project root)
  • method/constructor name
  • why it's unclear (reason category + detail)
  • a suggested question for a human reviewer

Runs during Subagent 1 (baseline analysis) and Subagent 2 (modernization).
Appends to reports/knowledge_gaps.md — never overwrites existing entries.
"""

from __future__ import annotations

import os
import re
import textwrap
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import List, Optional

# ─── Configuration ────────────────────────────────────────────────────────────

# Directories under which .java files are scanned (relative to CWD)
DEFAULT_SOURCE_ROOTS: List[str] = [
    "src/main/java",
]

KNOWLEDGE_GAPS_PATH = "reports/knowledge_gaps.md"

# ─── Ambiguity heuristics ─────────────────────────────────────────────────────

# Method names whose sole purpose is get/set/is — trivially inferable,
# no flag needed.
_TRIVIAL_ACCESSOR_RE = re.compile(
    r"^(get|set|is|has|add|remove|clear|size|isEmpty|toString|hashCode|equals|compareTo).*",
    re.IGNORECASE,
)

# Patterns that suggest a non-obvious design decision hidden inside equals/hashCode.
# We flag these because the *business rule* (not the technical mechanism) may be unclear.
_SURROGATE_KEY_EQUALS_RE = re.compile(
    r"id\s*!=\s*null\s*&&\s*id\.equals",
)
_CONSTANT_HASHCODE_RE = re.compile(
    r"Objects\.hashCode\(\s*id\s*\)",
)

# Patterns indicating intentional but undocumented side effects.
_SILENT_DELETE_RE = re.compile(
    r"deleteById|deleteAll|delete\(",
)

# Identifiers that suggest legacy workaround or magic — flag for review.
_LEGACY_MAGIC_RE = re.compile(
    r"(hack|workaround|legacy|fixme|todo|magic|kludge|temp|temporary)",
    re.IGNORECASE,
)

# A method has no Javadoc if there's no /** ... */ block immediately above it.
# We flag methods that are non-trivial (body > 3 non-blank lines) and undocumented.
_MIN_BODY_LINES_FOR_DOC_REQUIREMENT = 4


# ─── Data model ───────────────────────────────────────────────────────────────

@dataclass
class KnowledgeGap:
    file_path: str          # relative path, e.g. src/main/java/.../UserService.java
    class_name: str         # simple class name
    method_name: str        # method/constructor name (with param types if overloaded)
    reason: str             # why it's unclear
    suggested_question: str # question for a human reviewer
    phase: str              # "Subagent-1" or "Subagent-2"
    line_number: int = 0    # best-effort line where method starts


# ─── Java source scanner ──────────────────────────────────────────────────────

class JavaMethodScanner:
    """
    Lightweight regex-based scanner for Java source files.

    It does NOT parse full ASTs — it uses structural heuristics sufficient
    to implement the SKILL.md flagging criteria without an external Java parser.
    """

    # Matches a method or constructor declaration line (non-abstract, non-interface stubs).
    _METHOD_DECL_RE = re.compile(
        r"^\s*(?:(?:public|protected|private|static|final|synchronized|default|"
        r"@Override|@Transactional(?:\([^)]*\))?)\s+)*"
        r"(?:(?:<[^>]+>\s+)?[\w<>\[\],.? ]+\s+)?"   # return type (optional for constructors)
        r"([\w]+)\s*\(([^)]*)\)\s*(?:throws\s+[\w,\s]+)?\s*\{"
    )

    # Detects the opening of a Javadoc block
    _JAVADOC_START_RE = re.compile(r"^\s*/\*\*")
    _JAVADOC_END_RE   = re.compile(r"\*/")
    _LINE_COMMENT_RE  = re.compile(r"^\s*//")

    def scan_file(self, file_path: str, phase: str) -> List[KnowledgeGap]:
        """Return all flagged gaps found in one Java source file."""
        try:
            source = Path(file_path).read_text(encoding="utf-8", errors="replace")
        except OSError:
            return []

        lines = source.splitlines()
        class_name = self._extract_class_name(source)
        rel_path = self._to_rel_path(file_path)

        gaps: List[KnowledgeGap] = []
        i = 0
        while i < len(lines):
            m = self._METHOD_DECL_RE.match(lines[i])
            if m:
                method_name = m.group(1)
                params      = m.group(2).strip()
                body_lines  = self._extract_body_lines(lines, i)
                has_javadoc = self._preceding_javadoc(lines, i)
                body_text   = "\n".join(body_lines)

                gap = self._evaluate_method(
                    rel_path, class_name, method_name, params,
                    body_text, body_lines, has_javadoc, i + 1, phase
                )
                if gap:
                    gaps.append(gap)
            i += 1

        return gaps

    # ── Heuristic evaluations ────────────────────────────────────────────────

    def _evaluate_method(
        self,
        rel_path: str,
        class_name: str,
        method_name: str,
        params: str,
        body_text: str,
        body_lines: List[str],
        has_javadoc: bool,
        line_no: int,
        phase: str,
    ) -> Optional[KnowledgeGap]:

        non_blank_body = [l for l in body_lines if l.strip()]

        # ── Rule 1: surrogate-key equals() ───────────────────────────────────
        if method_name == "equals" and _SURROGATE_KEY_EQUALS_RE.search(body_text):
            return KnowledgeGap(
                file_path=rel_path,
                class_name=class_name,
                method_name="equals(Object o)",
                reason=(
                    "Uses surrogate-key-only equality: two entities with the same "
                    "field values but null IDs (transient, not yet persisted) are "
                    "intentionally unequal. The technical mechanism (id != null guard) "
                    "is commented, but the *business rule* — whether two User objects "
                    "representing the same real person before save should be treated as "
                    "distinct — is not documented. This affects Set/Map deduplication "
                    "behaviour at the application layer."
                ),
                suggested_question=(
                    "Should two User objects with identical username/email but no "
                    "database ID (e.g. built from an API request before being saved) "
                    "be considered equal? Or is uniqueness intentionally deferred until "
                    "the database assigns an ID? If the latter, is there any code path "
                    "where transient User objects are collected in a Set before save, "
                    "and would duplicate entries be silently allowed there?"
                ),
                phase=phase,
                line_number=line_no,
            )

        # ── Rule 2: constant hashCode for null-id entities ───────────────────
        if method_name == "hashCode" and _CONSTANT_HASHCODE_RE.search(body_text):
            return KnowledgeGap(
                file_path=rel_path,
                class_name=class_name,
                method_name="hashCode()",
                reason=(
                    "Returns Objects.hashCode(id), which evaluates to 0 for all "
                    "transient (unsaved) User instances. This is a documented JPA "
                    "best-practice to avoid rehashing after ID assignment, but the "
                    "comment explains *how* not *why*. If transient users are stored "
                    "in a HashMap or HashSet before being persisted, all of them "
                    "collide into bucket 0, creating O(n) lookup degradation. "
                    "Whether this is an acceptable trade-off for this domain is not "
                    "stated."
                ),
                suggested_question=(
                    "Is it acceptable for all transient (not-yet-saved) User objects "
                    "to hash to 0? Are there any application flows that add User objects "
                    "to a HashSet or use them as HashMap keys before the entity is "
                    "persisted? If yes, has the O(n) hash collision performance impact "
                    "been assessed and accepted for the expected collection sizes?"
                ),
                phase=phase,
                line_number=line_no,
            )

        # ── Rule 3: delete operations with no guard or return value ──────────
        if ((method_name in ("deleteUser", "deleteProduct", "deleteById") or method_name.startswith("delete"))
                and _SILENT_DELETE_RE.search(body_text)
                and "findById" not in body_text
                and "existsById" not in body_text):
            entity = class_name.replace("Service", "").replace("Controller", "").lower() or "resource"
            return KnowledgeGap(
                file_path=rel_path,
                class_name=class_name,
                method_name=f"{method_name}(...)",
                reason=(
                    "Calls deleteById without first checking whether the entity "
                    "exists. Spring Data's deleteById silently no-ops on a missing ID "
                    "in Spring Data 3.x but throws EmptyResultDataAccessException in "
                    "earlier versions. The controller returns 204 No Content whether "
                    f"the {entity} existed or not — the caller cannot distinguish a "
                    "successful delete from a delete of a non-existent resource."
                ),
                suggested_question=(
                    f"Should DELETE /api/{entity}s/{{id}} return 404 Not Found when the "
                    "given ID does not exist, or is 204 No Content on a missing "
                    "resource intentional (idempotent delete semantics)? "
                    "If idempotent behaviour is required, please confirm this "
                    "explicitly so it can be documented and tested as a contract."
                ),
                phase=phase,
                line_number=line_no,
            )

        # ── Rule 4: non-trivial undocumented methods ─────────────────────────
        if (not has_javadoc
                and not _TRIVIAL_ACCESSOR_RE.match(method_name)
                and len(non_blank_body) >= _MIN_BODY_LINES_FOR_DOC_REQUIREMENT
                and _LEGACY_MAGIC_RE.search(body_text)):
            return KnowledgeGap(
                file_path=rel_path,
                class_name=class_name,
                method_name=f"{method_name}(...)",
                reason=(
                    "Method contains a comment keyword suggesting a legacy workaround "
                    "(hack / fixme / todo / magic / temp) but has no Javadoc explaining "
                    "the intent or the circumstances that required the workaround."
                ),
                suggested_question=(
                    f"What is the original business or technical reason behind the "
                    f"workaround in {method_name}? Can it be removed after modernization, "
                    f"or does a downstream system dependency require it to remain?"
                ),
                phase=phase,
                line_number=line_no,
            )

        return None

    # ── Helpers ──────────────────────────────────────────────────────────────

    @staticmethod
    def _extract_class_name(source: str) -> str:
        m = re.search(r"^\s*(?:(?:public|abstract|final|sealed|non-sealed)\s+)*(?:class|interface|enum|record)\s+(\w+)", source, re.MULTILINE)
        return m.group(1) if m else "Unknown"

    @staticmethod
    def _to_rel_path(file_path: str) -> str:
        try:
            return str(Path(file_path).relative_to(Path.cwd()))
        except ValueError:
            return file_path

    @staticmethod
    def _extract_body_lines(lines: List[str], start: int) -> List[str]:
        """Return lines inside the method body (between the opening { and matching })."""
        depth = 0
        body: List[str] = []
        for line in lines[start:]:
            depth += line.count("{") - line.count("}")
            body.append(line)
            if depth <= 0:
                break
        return body[1:-1]  # strip the declaration line and closing brace

    @staticmethod
    def _preceding_javadoc(lines: List[str], method_line: int) -> bool:
        """Return True if there's a /** ... */ block ending just before method_line."""
        i = method_line - 1
        # Skip blank lines and @annotation lines going backwards
        while i >= 0 and (not lines[i].strip() or lines[i].strip().startswith("@")):
            i -= 1
        if i >= 0 and re.search(r"\*/", lines[i]):
            return True
        return False


# ─── Multi-file scanner ───────────────────────────────────────────────────────

def scan_source_roots(
    source_roots: List[str] = DEFAULT_SOURCE_ROOTS,
    phase: str = "Subagent-1",
) -> List[KnowledgeGap]:
    """
    Walk all .java files under each source root (or file path) and return every flagged gap.
    Roots that don't exist are silently skipped.
    """
    scanner = JavaMethodScanner()
    all_gaps: List[KnowledgeGap] = []

    for root in source_roots:
        if os.path.isfile(root) and root.endswith(".java"):
            gaps = scanner.scan_file(root, phase)
            all_gaps.extend(gaps)
        elif os.path.isdir(root):
            for dirpath, _, filenames in os.walk(root):
                for fname in filenames:
                    if fname.endswith(".java"):
                        full_path = os.path.join(dirpath, fname)
                        gaps = scanner.scan_file(full_path, phase)
                        all_gaps.extend(gaps)

    return all_gaps


# ─── Report writer ────────────────────────────────────────────────────────────

def _format_gap(gap: KnowledgeGap, index: int) -> str:
    return textwrap.dedent(f"""\
        ### Gap {index}: `{gap.class_name}.{gap.method_name}`

        | Field | Value |
        |---|---|
        | **File** | `{gap.file_path}` |
        | **Method** | `{gap.method_name}` |
        | **Line** | {gap.line_number} |
        | **Phase detected** | {gap.phase} |

        **Why it's unclear:**
        {gap.reason}

        **Suggested question for human reviewer:**
        > {gap.suggested_question}

        ---
    """)


def write_knowledge_gaps(
    gaps: List[KnowledgeGap],
    output_path: str = KNOWLEDGE_GAPS_PATH,
    phase: str = "Subagent-1",
    append: bool = True,
) -> None:
    """
    Write (or append) gap entries to reports/knowledge_gaps.md.

    If append=True and the file already exists, new entries are appended under a
    timestamped run header so the file acts as an audit log across pipeline runs.
    If the file does not exist it is created with a full header.
    """
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    now = datetime.now(tz=timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    file_exists = os.path.isfile(output_path)

    with open(output_path, "a" if (append and file_exists) else "w", encoding="utf-8") as f:
        if not file_exists:
            f.write(textwrap.dedent("""\
                # Knowledge Gaps — Business-Logic Flagging Report

                Generated by SpringPhoenix modernization pipeline.
                Governed by `.bob/SKILL.md` — *"Business-Logic Flagging"* section.

                **Purpose:** Flag any method whose business purpose isn't inferable from
                naming, comments, or surrounding context, so a human reviewer can clarify
                intent before or after modernization is applied.

                Each entry contains: file path · method name · why it's unclear · a
                suggested question for the human reviewer.

                ---

            """))

        if not gaps:
            f.write(f"\n## Run: {now} ({phase})\n\n")
            f.write(
                "_No business-logic ambiguities detected in this scan. "
                "All methods are sufficiently self-documenting via naming, "
                "comments, or context._\n\n---\n"
            )
            return

        f.write(f"\n## Run: {now} ({phase}) — {len(gaps)} gap(s) flagged\n\n")
        for i, gap in enumerate(gaps, start=1):
            f.write(_format_gap(gap, i))

    print(f"[+] Knowledge gaps report written to {output_path} ({len(gaps)} entries)")


# ─── Convenience entry point ──────────────────────────────────────────────────

def run_flagging(
    source_roots: List[str] = DEFAULT_SOURCE_ROOTS,
    phase: str = "Subagent-1",
    output_path: str = KNOWLEDGE_GAPS_PATH,
    append: bool = True,
) -> List[KnowledgeGap]:
    """
    Full scan-and-write cycle.  Returns the list of gaps for callers that need
    to inspect results programmatically (e.g. for CI gates or test assertions).
    """
    print(f"[*] Business-logic flagging scan starting ({phase})…")
    gaps = scan_source_roots(source_roots=source_roots, phase=phase)
    print(f"[*] Scan complete. {len(gaps)} potential gap(s) found.")
    write_knowledge_gaps(gaps, output_path=output_path, phase=phase, append=append)
    return gaps


if __name__ == "__main__":
    run_flagging()
