#!/usr/bin/env bash
# generate_report.sh
# ──────────────────
# Produces reports/impact_report.md and reports/impact_report.html by
# running impact_report_generator.py.
#
# Usage:
#   ./generate_report.sh                  # manual estimate defaults to 8 hours
#   ./generate_report.sh 12               # pass your own manual-hours estimate
#
# Prerequisites:
#   pip install jinja2 defusedxml
#   mvn clean verify must have been run (produces jacoco.xml + surefire XMLs)
#   mvn pitest:mutationCoverage must have been run (produces mutations.xml)

set -euo pipefail

MANUAL_HOURS="${1:-8}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=================================================="
echo "     SPRINGPHOENIX — IMPACT REPORT GENERATOR     "
echo "=================================================="
echo ""
echo "[*] Working directory : $SCRIPT_DIR"
echo "[*] Manual hours estimate passed in : ${MANUAL_HOURS}h"
echo ""

# ── Sanity checks ─────────────────────────────────────────────────────────────
check_file() {
  if [[ ! -f "$1" ]]; then
    echo "[-] Required file not found: $1"
    echo "    Run 'mvn clean verify' (JaCoCo) and 'mvn pitest:mutationCoverage' first."
    exit 1
  fi
}

check_file "reports/baseline.json"
check_file "target/site/jacoco/jacoco.xml"
check_file "target/pit-reports/mutations.xml"
check_file "reports/knowledge_gaps.md"

# ── Run generator ─────────────────────────────────────────────────────────────
echo "[*] Running impact_report_generator.py …"
python impact_report_generator.py "${MANUAL_HOURS}"

echo ""
echo "=================================================="
echo "  Reports written:"
echo "    reports/impact_report.md"
echo "    reports/impact_report.html"
echo "=================================================="
