"""
modernization_subagent.py
─────────────────────────
Orchestrates Phase 2 (Subagent 2) Modernization verification steps:
  1. Re-runs the Business-Logic Flagging scan *after* code has been modernized,
     so any new ambiguities introduced during the upgrade are caught.
     Appends to reports/knowledge_gaps.md (never overwrites Subagent-1 entries).
  2. Placeholder hooks for self-healing and Testcontainers checks — these are
     executed by the Java build (mvn clean verify) and monitored externally.

Governed by .bob/SKILL.md — "Subagent 2 — Modernize & Verify" and
"Business-Logic Flagging (runs alongside all subagents)".
"""

from __future__ import annotations

import subprocess
import sys
from knowledge_gaps_scanner import run_flagging


def run_maven_verify() -> bool:
    """Execute mvn clean verify and return True on success."""
    print("[*] Running mvn clean verify…")
    result = subprocess.run(
        ["mvn", "clean", "verify", "-Dspring.profiles.active=test",
         "--no-transfer-progress"],
        capture_output=False,
    )
    return result.returncode == 0


def main() -> None:
    # ── Stage 2a: Business-logic re-scan on modernized source ────────────────
    # Run *before* the build so flagged risks appear in the report even if
    # the build subsequently fails.
    print("[*] Running business-logic flagging scan (Subagent 2 — post-modernization)…")
    try:
        gaps = run_flagging(
            source_roots=["src/main/java"],
            phase="Subagent-2",
            append=True,   # accumulate — never wipe Subagent-1 entries
        )
        if gaps:
            print(
                f"[!] {len(gaps)} business-logic gap(s) flagged after modernization — "
                f"see reports/knowledge_gaps.md"
            )
        else:
            print("[+] No new ambiguous methods detected after modernization.")
    except Exception as e:
        print(f"[!] Business-logic flagging error (non-fatal): {e}", file=sys.stderr)

    # ── Stage 2b: Full build + integration test gate ──────────────────────────
    print()
    ok = run_maven_verify()
    if not ok:
        print(
            "[-] mvn clean verify FAILED. Review terminal output above.\n"
            "    Self-healing: read the stack trace and patch the failing code,\n"
            "    then re-run this script. Max 3 self-heal attempts per SKILL.md.",
            file=sys.stderr,
        )
        sys.exit(1)

    print("[+] Subagent 2 complete — all tests passing, knowledge_gaps.md updated.")


if __name__ == "__main__":
    main()
