# Setup Guide — SpringPhoenix

## Prerequisites

- Java 17+ and Maven (for the target Spring Boot service)
- Python 3.10+ (for the reporting/metrics layer)
- IBM Bob 2.0 (hackathon-provisioned account with Bobcoins)
- Git

## 1. Clone the repo

\`\`\`bash
git clone https://github.com/[your-username]/spring-phoenix.git
cd spring-phoenix
\`\`\`

## 2. Install Python dependencies

\`\`\`bash
pip install -r requirements.txt
\`\`\`

## 3. Point SpringPhoenix at a target service

Place the legacy Spring Boot service you want to modernize under \`target/\`,
or set its path in \`.env\`:

\`\`\`bash
cp .env.example .env
# edit TARGET_REPO_PATH in .env
\`\`\`

## 4. Capture baseline

\`\`\`bash
./scripts/capture_baseline.sh
\`\`\`

This runs the existing build/tests and saves results to \`reports/baseline.json\` — $0 token cost, pure shell.

## 5. Run the Bob 2.0 workflow

Open the project in Bob 2.0 and run the workflow defined in \`.bob/workflows/modernize.yaml\`
(or trigger via Bob Shell CLI):

\`\`\`bash
bob workflow run modernize
\`\`\`

This walks through: document understanding → parallel subagent analysis → human approval gate →
parallel refactor + test generation → re-test → knowledge doc generation.

## 6. View the results

\`\`\`bash
./scripts/generate_report.sh
\`\`\`

Outputs \`reports/impact_report.md\` — before/after coverage, build pass rate, and time comparison.

## Troubleshooting

- **Bob workflow fails to find target repo** — check \`TARGET_REPO_PATH\` in \`.env\`
- **Baseline script fails** — ensure Maven can build the target service standalone first