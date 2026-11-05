#!/usr/bin/env python3
"""Cobertura del diff contra la rama base (constitución §7, advisory).

Uso: scripts/check_delta_coverage.py --min 80 --advisory [--base origin/main]

Con --advisory siempre termina en 0: informa, no bloquea.
"""

from __future__ import annotations

import argparse
import glob
import subprocess
import sys
import xml.etree.ElementTree as ET


def sh(*args: str) -> str:
    proc = subprocess.run(args, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or " ".join(args))
    return proc.stdout


def changed_files(base: str) -> list[str]:
    for spec in (f"{base}...HEAD", f"{base}..HEAD", "HEAD~1"):
        try:
            out = sh("git", "diff", "--name-only", spec, "--", "*.kt")
        except RuntimeError:
            continue
        return [line for line in out.splitlines() if line.strip()]
    return []


def file_coverage(report: str) -> dict[str, tuple[int, int]]:
    root = ET.parse(report).getroot()
    result: dict[str, tuple[int, int]] = {}
    for source in root.iter("sourcefile"):
        name = source.get("name") or ""
        covered = missed = 0
        for line in source.findall("line"):
            covered += int(line.get("ci", line.get("covered", 0)))
            missed += int(line.get("mi", line.get("missed", 0)))
        if name and (covered or missed):
            result[name] = (covered, missed)
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--min", dest="min_pct", type=float, default=80.0)
    parser.add_argument("--base", default="origin/main")
    parser.add_argument("--advisory", action="store_true")
    parser.add_argument("--report", default=None)
    args = parser.parse_args()

    def done(message: str, failed: bool = False) -> int:
        print(f"DELTA-CHECK: {message}")
        if failed and not args.advisory:
            return 1
        if args.advisory:
            print("DELTA-CHECK: advisory, no bloquea")
        return 0

    files = changed_files(args.base)
    if not files:
        return done(f"sin .kt cambiados contra {args.base}")

    reports = [args.report] if args.report else sorted(
        glob.glob("app/build/reports/kover/*.xml") + glob.glob("build/reports/kover/*.xml")
    )
    reports = [r for r in reports if r]
    if not reports:
        return done("sin reporte Kover; ejecuta check_coverage.sh primero")

    try:
        per_file = file_coverage(reports[0])
    except ET.ParseError:
        return done(f"reporte ilegible: {reports[0]}")

    if not per_file:
        return done("el reporte no trae detalle por archivo; usa check_coverage.sh")

    covered = missed = missing = 0
    sin_datos: list[str] = []
    print(f"DELTA-CHECK: {len(files)} archivo(s) .kt cambiados contra {args.base}")
    for path in files:
        key = path.rsplit("/", 1)[-1]
        hit = per_file.get(key)
        if hit is None:
            missing += 1
            sin_datos.append(path)
            continue
        file_covered, file_missed = hit
        covered += file_covered
        missed += file_missed
        pct = 100.0 * file_covered / max(1, file_covered + file_missed)
        print(f"  - {path}: {pct:.1f}% ({file_covered}/{file_covered + file_missed})")

    for path in sin_datos[:10]:
        print(f"  - {path}: sin datos en el reporte")
    if len(sin_datos) > 10:
        print(f"  - … y {len(sin_datos) - 10} archivo(s) sin datos en el reporte")

    total = covered + missed
    if total == 0:
        return done("los archivos cambiados no aportan líneas al reporte")

    pct = 100.0 * covered / total
    print(
        f"DELTA-CHECK: {pct:.2f}% en el diff umbral {args.min_pct:.0f}% "
        f"({missing} sin datos)"
    )
    if pct + 1e-9 < args.min_pct:
        return done(f"{pct:.2f}% < {args.min_pct:.0f}%", failed=True)
    return done(f"{pct:.2f}% >= {args.min_pct:.0f}%")


if __name__ == "__main__":
    sys.exit(main())
