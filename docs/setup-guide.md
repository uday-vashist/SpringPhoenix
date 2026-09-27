# Setup Guide — SpringPhoenix

## Prerequisites

- **Java 17+ and Maven** (for the target Spring Boot service)
- **Docker** (running locally, required for Testcontainers database integration tests)
- **Maven Plugins configured in `pom.xml`**:
  - `jacoco-maven-plugin` (for 80%+ line/branch coverage auditing)
  - `pitest-maven` (for bytecode mutation testing guardrails)
- **Qwen 2.5 Coder API Access / Key** (configured for automated test synthesis, refactoring, and assertion hardening)
- **Python 3.10+** (for the reporting and metrics layer)
- **IBM Bob 2.0** (hackathon-provisioned account with Bobcoins)
- **Git**

---

## 1. Clone the repo

```bash
git clone https://github.com/[your-username]/spring-phoenix.git
cd spring-phoenix
```

---

## 2. Install Python dependencies

```bash
pip install -r requirements.txt
```

---

## 3. Point SpringPhoenix at a target service

Place the legacy Spring Boot service you want to modernize under `target/`, or set its path in `.env`:

```bash
cp .env.example .env
# Edit TARGET_REPO_PATH and QWEN_API_KEY in .env
```

---

## 4. Capture baseline

```bash
# Capture coverage and flag knowledge gaps via Subagent 1
python baseline_subagent.py
```

This runs the existing build/tests and saves results to `reports/baseline.json` and `reports/knowledge_gaps.md` — **$0 token cost, pure deterministic execution**.

---

## 5. Run coverage gate check

```bash
# Verify build and enforce 80% line + branch coverage gate
mvn clean verify
```

Checks the JaCoCo coverage report against the **80% line and branch coverage threshold**. If coverage is below 80%, Bob Subagent 1 automatically requests additional targeted JUnit 5 tests from Qwen 2.5 Coder until the 80% gate is satisfied before modernization begins.

---

## 6. Run the Concurrent Multi-Module Pipeline

Run the concurrent orchestrator to execute the Subagent 1 $\rightarrow$ Subagent 2 $\rightarrow$ Subagent 3 pipelines across independent domain modules (`user-module` and `product-module`) in parallel:

```bash
python concurrent_orchestrator.py
```

This orchestrates the 3-subagent pipeline concurrently across independent modules:
1. **Baseline & Coverage Lock** (Subagent 1 — JaCoCo parsing + business logic scan)
2. **Modernize & Verify** (Subagent 2 — Surefire test verification + logic re-scan)
3. **Mutation Guardrail** (Subagent 3 — PITest mutant analysis)
4. **Timeline Persistence** (Writes wall-clock timestamps and overlap data to `reports/pipeline_timeline.json`)

---

## 7. Run mutation guardrail

```bash
mvn pitest:mutationCoverage
```

PITest runs after modernization completes, injecting artificial bytecode mutants into the refactored code. If any mutants survive, Subagent 3 invokes Qwen 2.5 Coder to strengthen `assertThat()` checks. The pipeline only completes when **zero mutants survive (100% mutation score, 42/42 mutants killed)**.

---

## 8. View the results

```bash
python impact_report_generator.py 8
```

Outputs `reports/impact_report.md` and `reports/impact_report.html` — before/after coverage, mutation scores, 133-test suite breakdown, time saved, and concurrent Gantt timeline analysis.

---

## Troubleshooting

- **Testcontainers fails to start** — Check that the Docker daemon is running locally (`docker ps`). Testcontainers requires Docker socket access to launch database containers.
- **Coverage gate keeps failing** — Check your `pom.xml` JaCoCo plugin configuration to ensure rule limits and report directories match `target/site/jacoco/jacoco.xml`.
- **PITest reports high surviving mutants** — This is expected on the initial pass. Subagent 3 will automatically analyze the surviving mutant traces and strengthen assertions in the subsequent cycle until all mutants are killed.
- **Bob workflow fails to find target repo** — Check `TARGET_REPO_PATH` in `.env`.
- **Baseline script fails** — Ensure Maven can build the target service standalone first (`mvn clean test`).