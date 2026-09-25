# SpringPhoenix

Legacy Spring Boot services carry two kinds of debt: outdated code and undocumented knowledge.
SpringPhoenix uses IBM Bob 2.0 to pay off both at once.

## The Problem

Upgrading an old Spring Boot service is high-risk. No one wants to touch it because no one
can guarantee nothing breaks — and the reasoning behind half the code only exists in the
head of a developer who might leave tomorrow.

## What SpringPhoenix Does

1. **Captures a baseline** — Qwen 2.5 Coder generates JUnit 5 tests to lock in existing behavior.
2. **Audits coverage** — JaCoCo checks line/branch coverage; pipeline stops below 80% and requests more tests.
3. **Modernizes** — Qwen refactors the module (e.g. Java 8→21, Spring Boot 2→3), self-healing by reading `mvn test` error logs.
4. **Validates against real DBs** — Testcontainers spins up actual PostgreSQL in Docker, no mocks.
5. **Proves test quality** — PITest injects mutants into bytecode; if tests don't catch them, Qwen strengthens assertions until the suite is resilient.
6. **Knowledge doc generated** — undocumented business logic flagged along the way, compiled into a living reference.

## Why It Matters

| | Before | After |
|---|---|---|
| Upgrade approach | Manual, file-by-file | Parallel, subagent-driven |
| Test coverage | Often near-zero | Auto-generated per module |
| Institutional knowledge | In someone's head | Captured in a living doc |
| Confidence to merge | Low | Backed by baseline diff |

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for the full Bob 2.0 Workflow breakdown —
which stages are Bob-native (Agent Mode, subagents, document understanding) and which are our
custom layer (`.bob/SKILL.md` rules, local shell scripts for baseline capture and reporting).

## Project Structure

\`\`\`
src/            application/pipeline code
docs/           problem-statement.md, prd.md, architecture.md, setup-guide.md
.bob/           SKILL.md and workflow definitions
demo/           screenshots, demo video
\`\`\`

## Setup

See [`docs/setup-guide.md`](docs/setup-guide.md) for install and run steps.

## Built With

IBM Bob 2.0 · Agent Mode · Parallel Subagents · Document Understanding · Bob Shell CLI

## Team

Built by Uday Vashist & Ubee Kapoor for the IBM Bob 2.0 Hackathon.
