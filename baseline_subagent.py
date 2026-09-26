"""
baseline_subagent.py
────────────────────
Orchestrates Phase 1 (Subagent 1) Baseline Testing:
  1. Parses JaCoCo XML and outputs baseline coverage metrics.
  2. Runs the Business-Logic Flagging scan over src/main/java and appends
     any ambiguous methods to reports/knowledge_gaps.md, as required by
     .bob/SKILL.md — "Business-Logic Flagging (runs alongside all subagents)".
"""

from __future__ import annotations

import sys
from parser import DEFAULT_JACOCO_XML, parse_jacoco_xml
from reporter import print_report, save_baseline_json
from knowledge_gaps_scanner import run_flagging

def main() -> None:
    # ── Stage 1a: Coverage baseline ──────────────────────────────────────────
    print(f"[*] Reading JaCoCo XML report from: {DEFAULT_JACOCO_XML}")
    try:
        report = parse_jacoco_xml(DEFAULT_JACOCO_XML)
        print_report(report)
        save_baseline_json(report)
    except Exception as e:
        print(f"[-] Error processing baseline coverage: {e}", file=sys.stderr)
        sys.exit(1)

    # ── Stage 1b: Business-logic flagging (SKILL.md requirement) ─────────────
    print()
    print("[*] Running business-logic flagging scan (Subagent 1)…")
    try:
        gaps = run_flagging(
            source_roots=["src/main/java"],
            phase="Subagent-1",
            append=True,   # accumulate across pipeline runs
        )
        if gaps:
            print(f"[!] {len(gaps)} business-logic gap(s) flagged — see reports/knowledge_gaps.md")
        else:
            print("[+] No ambiguous methods detected in this module.")
    except Exception as e:
        # Flagging failure must never abort the pipeline — log and continue.
        print(f"[!] Business-logic flagging error (non-fatal): {e}", file=sys.stderr)

if __name__ == "__main__":
    main()
