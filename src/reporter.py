"""
src/reporter.py
Format coverage reports and export baseline JSON.
"""

from __future__ import annotations

import json
import os
from parser import CoverageReport

def print_report(report: CoverageReport) -> None:
    print(f"[*] Report Name: {report.report_name}")
    print(f"[*] Overall Line Coverage: {report.overall.percentage}% "
          f"({report.overall.covered}/{report.overall.total} lines covered)")

def save_baseline_json(report: CoverageReport, output_path: str = "reports/baseline.json") -> None:
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    data = {
        "report_name": report.report_name,
        "overall": {
            "name": report.overall.name,
            "missed": report.overall.missed,
            "covered": report.overall.covered,
            "total": report.overall.total,
            "percentage": report.overall.percentage
        },
        "packages": [{"name": p.name, "missed": p.missed, "covered": p.covered, "percentage": p.percentage} for p in report.packages],
        "classes": [{"name": c.name, "missed": c.missed, "covered": c.covered, "percentage": c.percentage} for c in report.classes]
    }

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    
    print(f"[+] Baseline coverage successfully saved to {output_path}")
