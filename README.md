# SpringPhoenix

Legacy Spring Boot services carry two kinds of debt: outdated code and undocumented knowledge.
SpringPhoenix uses IBM Bob 2.0 to pay off both at once.

## The Problem

Upgrading an old Spring Boot service is high-risk. No one wants to touch it because no one
can guarantee nothing breaks — and the reasoning behind half the code only exists in the
head of a developer who might leave tomorrow.

## What SpringPhoenix Does

1. **Captures a baseline** — current build status, test coverage, pass/fail state.
2. **Understands the service** — ingests existing docs (if any) via Bob's document understanding.
3. **Parallel subagents analyze each module** — comprehension, refactor plan, and flags for
   undocumented or high-risk business logic, all running simultaneously.
4. **Human approval gate** — you review the refactor plan before any code changes.
5. **Parallel subagents execute** — refactor + auto-generate missing unit tests, module by module.
6. **Re-test and compare** — build/test results re-run and diffed against the baseline.
7. **Knowledge doc generated** — every flagged piece of undocumented logic is compiled into
   a living reference, so the upgrade leaves the team smarter, not just the code newer.

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
