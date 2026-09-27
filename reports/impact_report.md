# SpringPhoenix — Before/After Impact Report

**Module:** `com.example.legacy (user-module + product-module)`  
**Generated:** 2026-09-27 11:58 UTC  
**Pipeline:** SpringPhoenix Modernization Pipeline (IBM Bob 2.0 + JaCoCo + PITest)

---

## Summary Table

| Metric | Before Pipeline | After Baseline Generation | After Modernization |
|--------|-----------------|--------------------------|---------------------|
| **Line Coverage %** | 0.00% (0/84 lines) | 97.62% (82/84 lines) | 97.62% (82/84 lines) |
| **Branch Coverage %** | 0.0% (no tests) | 0.0% (not captured) | 100.00% |
| **Mutation Score %** | N/A (no tests existed) | N/A (baseline only) | 100.0% (42/42 killed, 0 survived) |
| **Build Pass Rate** | Compiled, 0 tests | Baseline tests pass | 133/133 (100%) (133 tests, 26.1s) |
| **Self-Heal Cycles Used** | — | — | 0 cycles total |
| **Business-Logic Gaps Flagged** | 0 | 0 | 5 gap(s) |
| **Pipeline Runtime** | — | — | 59s (~1.0 min) |
| **Manual Refactor Estimate** | ~8h (manual estimate) | — | — |
| **Estimated Time Saved** | — | — | ≈ 479 min saved (8h manual → 1.0 min automated) |

---

## Test Suite Breakdown

| Test Class | Tests | Pass | Fail | Time (s) |
|------------|-------|------|------|----------|
| `ProductControllerIntegrationTest` | 15 | 15 | 0 | 19.20 |
| `ProductControllerTest` | 8 | 8 | 0 | 0.47 |
| `UserControllerIntegrationTest` | 15 | 15 | 0 | 0.75 |
| `UserControllerTest` | 7 | 7 | 0 | 0.13 |
| `ProductTest` | 7 | 7 | 0 | 0.04 |
| `UserTest` | 12 | 12 | 0 | 0.06 |
| `ProductRepositoryIntegrationTest` | 11 | 11 | 0 | 1.39 |
| `UserRepositoryIntegrationTest` | 13 | 13 | 0 | 0.59 |
| `UserRepositoryTest` | 5 | 5 | 0 | 0.97 |
| `ProductServiceIntegrationTest` | 13 | 13 | 0 | 1.80 |
| `ProductServiceTest` | 7 | 7 | 0 | 0.21 |
| `UserServiceIntegrationTest` | 12 | 12 | 0 | 0.31 |
| `UserServiceTest` | 6 | 6 | 0 | 0.16 |
| `SpringPhoenixApplicationTests` | 2 | 2 | 0 | 0.05 |

**Total:** 133 tests · 133 passed · 0 failed · 26.1s

---

## Mutation Testing Detail

| Stat | Value |
|------|-------|
| Total mutants generated | 42 |
| Mutants killed | 42 |
| Mutants survived | 0 |
| Test strength | 100.0% |
| Strengthening cycles needed | 0 (all mutants killed first run) |

---

## Self-Heal Cycles

None required — all tests passed first-time

---

## Business-Logic Gaps Flagged

5 method(s) flagged by the business-logic scanner (`.bob/SKILL.md`):

  - **`User.equals(Object o)`** (`src/main/java/com/example/legacy/model/User.java`) — Uses surrogate-key-only equality: two entities with the same field values but null IDs (transient, not yet persisted) ar…
  - **`User.hashCode()`** (`src/main/java/com/example/legacy/model/User.java`) — Returns `Objects.hashCode(id)`, which evaluates to `0` for all transient (unsaved) `User` instances. This is a documente…
  - **`UserService.deleteUser(Long id)`** (`src/main/java/com/example/legacy/service/UserService.java`) — Calls `deleteById` without first checking whether the entity exists. Spring Data's `deleteById` silently no-ops on a mis…
  - **`ProductService.deleteProduct(...)`** (`src/main/java/com/example/legacy/Service/ProductService.java`) — Calls deleteById without first checking whether the entity exists. Spring Data's deleteById silently no-ops on a missing…
  - **`UserService.deleteUser(...)`** (`src/main/java/com/example/legacy/Service/UserService.java`) — Calls deleteById without first checking whether the entity exists. Spring Data's deleteById silently no-ops on a missing…

> Full details: [`reports/knowledge_gaps.md`](knowledge_gaps.md)

---

## Time Saved

| | Value |
|---|---|
| Pipeline runtime | **59s (~1.0 min)** |
| Manual modernization estimate | **~8h (manual estimate)** |
| Net time saved | **≈ 479 min saved (8h manual → 1.0 min automated)** |

> _Manual estimate covers: reading legacy code, writing unit + integration tests,_  
> _upgrading dependencies, fixing test failures, running coverage checks, and_  
> _verifying mutation resilience by hand._

---

## Parallel Execution Timeline

2 independent module pipelines ran concurrently (`user-module`, `product-module`).

| Metric | Value |
|--------|-------|
| Wall-clock time (concurrent) | **10.26s** |
| Sequential equivalent | 19.00s |
| Parallelism gain | **8.74s saved** |
| Speed-up factor | **1.9x** |
| Stage overlaps proven | 5 concurrent pairs |

### Stage Timeline

| Module | Stage | Start (UTC) | End (UTC) | Duration (s) | Detail |
|--------|-------|-------------|-----------|-------------|--------|
| `user-module` | Subagent-1 | 11:56:17.587 | 11:56:19.608 | 2.02 | line_cov=100.0%  logic_gaps=4 |
| `product-module` | Subagent-1 | 11:56:17.588 | 11:56:19.208 | 1.62 | line_cov=100.0%  logic_gaps=2 |
| `product-module` | Subagent-2 | 11:56:19.208 | 11:56:23.628 | 4.42 | tests=61  passed=61  failed=0  logic_gaps=2 |
| `user-module` | Subagent-2 | 11:56:19.608 | 11:56:24.642 | 5.03 | tests=72  passed=72  failed=0  logic_gaps=4 |
| `product-module` | Subagent-3 | 11:56:23.628 | 11:56:26.331 | 2.70 | mutants=17  killed=17  survived=0  score=100% |
| `user-module` | Subagent-3 | 11:56:24.642 | 11:56:27.844 | 3.20 | mutants=25  killed=25  survived=0  score=100% |

> **Overlap proof:** Genuine concurrent execution verified across independent domain modules: `product-module/Subagent-1` with `user-module/Subagent-1` (1.62s), `user-module/Subagent-1` with `product-module/Subagent-2` (0.40s), `product-module/Subagent-2` with `user-module/Subagent-2` (4.02s). All 2 modules dispatched concurrently and overlapped across stages.

---

_Generated by SpringPhoenix modernization pipeline — IBM Bob 2.0_