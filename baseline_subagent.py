"""
src/baseline_subagent.py
Orchestrates Phase 1 Baseline Testing: parses JaCoCo XML and outputs baseline metrics.
"""

from __future__ import annotations

import sys
from parser import DEFAULT_JACOCO_XML, parse_jacoco_xml
from reporter import print_report, save_baseline_json

def main() -> None:
    print(f"[*] Reading JaCoCo XML report from: {DEFAULT_JACOCO_XML}")
    try:
        report = parse_jacoco_xml(DEFAULT_JACOCO_XML)
        print_report(report)
        save_baseline_json(report)
    except Exception as e:
        print(f"[-] Error processing baseline coverage: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
