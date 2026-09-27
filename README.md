# SpringPhoenix

Legacy Spring Boot services carry two kinds of debt: outdated code and undocumented knowledge.
SpringPhoenix uses **IBM Bob 2.0** to pay off both at once.

---

## The Problem

Upgrading an old Spring Boot service is high-risk. No one wants to touch it because no one can guarantee nothing breaks — and the reasoning behind half the code only exists in the head of a developer who might leave tomorrow.

---

## What SpringPhoenix Does

1. **Captures a Baseline** — Deterministically locks in existing behavior and generates JUnit 5 suites across independent domain modules (`User` and `Product`).
2. **Audits Coverage** — JaCoCo checks line and branch coverage; pipeline halts if below 80% and requests more tests.
3. **Modernizes with Real DB Tests** — Refactors the codebase (Java 8/11 $\rightarrow$ 17/21, Spring Boot 2 $\rightarrow$ 3, `javax.*` $\rightarrow$ `jakarta.*`), self-healing by reading compiler and test error logs.
4. **Guarantees Assertion Strength via Mutation Guardrails** — PITest injects bytecode mutants; assertions are strengthened until 100% of mutants are killed (0 surviving mutants).
5. **Orchestrates Independent Modules Concurrently** — Dispatches Subagent 1 $\rightarrow$ Subagent 2 $\rightarrow$ Subagent 3 pipelines in parallel across domain aggregates with real thread scheduling.
6. **Preserves Institutional Knowledge** — Business logic scanner flags ambiguous methods into `reports/knowledge_gaps.md` for human review.
7. **Generates Comprehensive Impact Reports** — Produces Before/After impact reports in Markdown and HTML with Gantt timeline analysis.

---

## Verified Results

| Metric | Baseline / Legacy | After SpringPhoenix | Gate Status |
|---|---|---|---|
| **Domain Modules** | Legacy structure | 2 independent modules (`User`, `Product`) | Fully modernized |
| **Total Test Suite** | 0 tests | **133/133 tests passing (100%)** | Clean pass |
| **Line Coverage** | 0.0% | **97.62%** (82/84 lines) | $\ge$ 80% Gate **PASSED** |
| **Branch Coverage** | 0.0% | **100.00%** (8/8 branches) | $\ge$ 80% Gate **PASSED** |
| **Mutation Score** | N/A | **100.0%** (42/42 mutants killed) | **0 surviving mutants** |
| **Concurrent Speedup** | 19.00s sequential | **10.26s concurrent** | **8.74s saved (1.9x speedup)** |
| **Self-Heal Cycles** | N/A | 0 cycles needed (clean first run) | Within 3-cycle limit |

---

## Project Structure

```
SpringPhoenix/
├── pom.xml                          # Spring Boot 3.2.3, JaCoCo 0.8.15, PITest 1.15.3
├── concurrent_orchestrator.py       # Concurrent multi-module pipeline orchestrator
├── baseline_subagent.py             # Subagent 1: Baseline coverage + logic scan
├── modernization_subagent.py        # Subagent 2: Modernization verification + re-scan
├── impact_report_generator.py       # Before/After Markdown & HTML impact report generator
├── knowledge_gaps_scanner.py        # Business-logic flagging engine
├── parser.py / reporter.py          # JaCoCo XML parser & baseline JSON writer
├── generate_report.sh               # Shell entry-point
│
├── src/main/java/com/example/legacy/
│   ├── SpringPhoenixApplication.java
│   ├── model/                       # User.java, Product.java
│   ├── repository/                  # UserRepository.java, ProductRepository.java
│   ├── service/                     # UserService.java, ProductService.java
│   └── controller/                  # UserController.java, ProductController.java
│
├── src/test/java/com/example/legacy/
│   ├── SpringPhoenixApplicationTests.java
│   ├── model/                       # UserTest.java, ProductTest.java
│   ├── repository/                  # UserRepositoryTest, UserRepositoryIntegrationTest, ProductRepositoryIntegrationTest
│   ├── service/                     # UserServiceTest, UserServiceIntegrationTest, ProductServiceTest, ProductServiceIntegrationTest
│   └── controller/                  # UserControllerTest, UserControllerIntegrationTest, ProductControllerTest, ProductControllerIntegrationTest
│
├── reports/
│   ├── baseline.json                # Pre-modernization JaCoCo coverage snapshot
│   ├── knowledge_gaps.md            # Flagged business logic questions for review
│   ├── pipeline_timeline.json       # Wall-clock timestamps & concurrent overlap proof
│   ├── impact_report.md             # Before/After Markdown report
│   └── impact_report.html           # Before/After HTML report with Gantt timeline
│
└── docs/
    ├── prd.md                       # Product requirements document
    ├── architecture.md              # System architecture & concurrent subagent design
    └── setup-guide.md               # Quickstart and execution guide
```

---

## Quickstart

```bash
# 1. Run all tests and coverage checks
mvn clean verify

# 2. Run PITest mutation testing
mvn pitest:mutationCoverage

# 3. Run the concurrent multi-module pipeline
python concurrent_orchestrator.py

# 4. Generate the impact report
python impact_report_generator.py 8
```

---

## Built With

- **IBM Bob 2.0** · Agent Mode · Parallel Subagents · Document Understanding
- **Spring Boot 3.2.3** · Java 17 · JaCoCo 0.8.15 · PITest 1.15.3 · Testcontainers

---

## Team

Built by **Uday Vashist** & **Ubee Kapoor** for the IBM Bob 2.0 Hackathon.
