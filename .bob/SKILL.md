# SKILL.md — SpringPhoenix Modernization Rules

These rules govern how Bob 2.0 orchestrates subagents for this project. They exist so
modernization is safe, verifiable, and scoped — not a black box.

## Scope

- Only modify code under `target/`. Never touch `.bob/`, `scripts/`, or `docs/`.
- One module = one subagent pipeline run. Do not let a single subagent span multiple modules.
- Do not touch database schema files directly — flag schema changes for human review instead
  of applying them.

## Subagent 1 — Baseline & Coverage

- Generate JUnit 5 tests via Qwen 2.5 Coder to lock in current behavior *before* any
  refactoring begins. Never generate tests against already-modified code.
- Run JaCoCo after test generation. Coverage gate: 80% line and branch coverage.
- If coverage is below 80%, request additional tests from Qwen targeting the specific
  uncovered lines/branches reported by JaCoCo — do not regenerate the entire suite from scratch.
- Do not proceed to Subagent 2 until the gate passes.

## Subagent 2 — Modernize & Verify

- Target version must be confirmed from `pom.xml` before starting (e.g. Spring Boot 2.7 → 3.2,
  Java 8 → 21). Never assume the target version.
- Preserve existing public method signatures unless explicitly flagged and approved by a human.
- Do not change business logic behavior — only structure, syntax, and dependency versions.
- Run `mvn clean test` after every file change, not just at the end of the module.
- Self-healing: if a test fails, read the terminal error/stack trace and patch the code, then
  re-test. **Maximum 3 self-heal attempts per file.** If still failing after 3 attempts, stop
  and flag the file for human review instead of continuing to retry.
- Testcontainers must spin up real Docker DB instances for integration tests — mocking the
  database is not acceptable for this pipeline.
- Any change that could alter runtime behavior (not just structure) must be flagged for
  human approval, never applied silently.

## Subagent 3 — Mutation Guardrail

- Run PITest after Subagent 2 completes and all tests pass.
- If mutants survive, do not just add more tests — strengthen the specific `assertThat()`
  checks that failed to catch the mutant.
- **Maximum 3 mutation-strengthening cycles per module.** If mutants still survive after 3
  cycles, report the surviving mutants in the final report rather than looping indefinitely.
- Target: zero surviving mutants before the module is marked complete.

## Business-Logic Flagging (runs alongside all subagents)

- If a method's purpose isn't inferable from naming, comments, or surrounding context, flag it
  — do not guess and silently document.
- Flagged items go into `reports/knowledge_gaps.md` with: file path, method name, why it's
  unclear, and a suggested question for a human reviewer.

## Compliance Checks

- No hardcoded secrets or credentials — flag any found immediately, don't wait for the
  final report.
- Proper exception handling required (no empty catch blocks introduced during refactor).
- Logging must follow existing project conventions, not be invented fresh.

## What NOT to do autonomously

- Do not make unstated business-requirement decisions.
- Do not merge or apply changes without passing through the human-approval gate between
  Subagent 1 and Subagent 2.
- Do not assume database schema changes are safe — always flag for manual review.
- Do not exceed retry limits (3 for self-heal, 3 for mutation-strengthening) — flag and stop
  instead of looping.
