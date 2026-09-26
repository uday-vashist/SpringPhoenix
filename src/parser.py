"""
src/parser.py
Safely parse JaCoCo XML coverage reports using defusedxml.
"""

from __future__ import annotations

import os
from dataclasses import dataclass, field
from typing import List, Optional

import defusedxml.ElementTree as ET

DEFAULT_JACOCO_XML = os.path.join(
    "target", "site", "jacoco", "jacoco.xml"
)

@dataclass
class LineCoverage:
    name: str
    missed: int
    covered: int

    @property
    def total(self) -> int:
        return self.missed + self.covered

    @property
    def percentage(self) -> float:
        if self.total == 0:
            return 100.0
        return round(self.covered / self.total * 100, 2)

@dataclass
class CoverageReport:
    report_name: str
    overall: LineCoverage
    packages: List[LineCoverage] = field(default_factory=list)
    classes: List[LineCoverage] = field(default_factory=list)

def _line_counter(element: ET.Element) -> Optional[tuple[int, int]]:
    for counter in element.findall("counter"):
        if counter.get("type") == "LINE":
            return int(counter.get("missed", 0)), int(counter.get("covered", 0))
    return None

def parse_jacoco_xml(path: str = DEFAULT_JACOCO_XML) -> CoverageReport:
    if not os.path.isfile(path):
        raise FileNotFoundError(f"JaCoCo XML not found: {path!r}")

    tree = ET.parse(path)
    root = tree.getroot()
    report_name: str = root.get("name", "unknown")

    overall_counts = _line_counter(root)
    if overall_counts is None:
        raise ValueError(f"No LINE counter found at the report level in {path!r}")

    overall = LineCoverage(
        name=report_name,
        missed=overall_counts[0],
        covered=overall_counts[1],
    )

    packages: List[LineCoverage] = []
    classes: List[LineCoverage] = []

    for pkg in root.findall("package"):
        pkg_name = pkg.get("name", "unknown").replace("/", ".")
        pkg_counts = _line_counter(pkg)
        if pkg_counts is not None:
            packages.append(LineCoverage(name=pkg_name, missed=pkg_counts[0], covered=pkg_counts[1]))

        for cls in pkg.findall("class"):
            cls_name = cls.get("name", "unknown").replace("/", ".")
            cls_counts = _line_counter(cls)
            if cls_counts is not None:
                classes.append(LineCoverage(name=cls_name, missed=cls_counts[0], covered=cls_counts[1]))

    return CoverageReport(
        report_name=report_name,
        overall=overall,
        packages=packages,
        classes=classes,
    )
