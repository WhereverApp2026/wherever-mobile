#!/usr/bin/env bash
# Cobertura con Kover (constitución §7): genera el reporte si falta y exige
# que la línea cubierta alcance el umbral.
# Uso: scripts/check_coverage.sh [--min 45]
set -euo pipefail
export LC_ALL=C

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MIN=45
while [ $# -gt 0 ]; do
    case "$1" in
        --min)
            MIN="${2:?falta el valor de --min}"
            shift 2
            ;;
        *)
            printf 'Uso: check_coverage.sh [--min N]\n' >&2
            exit 2
            ;;
    esac
done

CANDIDATES=(
    app/build/reports/kover/report.xml
    app/build/reports/kover/report.debug.xml
    build/reports/kover/report.xml
)

have_report() {
    for file in "${CANDIDATES[@]}"; do
        [ -f "$file" ] && return 0
    done
    return 1
}

if ! have_report; then
    printf 'COVERAGE: generando reporte Kover…\n'
    for task in :app:koverXmlReportDebug :app:koverXmlReport koverXmlReport; do
        if ./gradlew --quiet "$task" >/dev/null 2>&1; then
            break
        fi
    done
fi

REPORT=""
for file in "${CANDIDATES[@]}"; do
    if [ -f "$file" ]; then
        REPORT="$file"
        break
    fi
done
if [ -z "$REPORT" ]; then
    REPORT="$(find app/build/reports/kover build/reports/kover -name '*.xml' -type f 2>/dev/null | head -n 1 || true)"
fi
if [ -z "$REPORT" ] || [ ! -f "$REPORT" ]; then
    printf 'COVERAGE-FAIL: sin reporte Kover en app/build/reports/kover\n' >&2
    exit 1
fi

# El alias de Microsoft Store responde con exit 49: se comprueba de verdad.
PY=""
for candidate in python3 python; do
    if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c 'import sys' >/dev/null 2>&1; then
        PY="$candidate"
        break
    fi
done
if [ -z "$PY" ]; then
    printf 'COVERAGE-FAIL: se necesita Python 3 (python3 o python)\n' >&2
    exit 1
fi

"$PY" - "$REPORT" "$MIN" <<'PY'
import sys
import xml.etree.ElementTree as ET

report, min_pct = sys.argv[1], float(sys.argv[2])
root = ET.parse(report).getroot()

counters = [c for c in root.findall("counter") if c.get("type") == "LINE"]
if not counters:
    counters = [c for c in root.iter("counter") if c.get("type") == "LINE"]
if not counters:
    print(f"COVERAGE-FAIL: sin contadores LINE en {report}", file=sys.stderr)
    sys.exit(1)

best = max(counters, key=lambda c: int(c.get("covered", 0)) + int(c.get("missed", 0)))
covered = int(best.get("covered", 0))
missed = int(best.get("missed", 0))
total = covered + missed
if total == 0:
    print(f"COVERAGE-FAIL: reporte vacío ({report})", file=sys.stderr)
    sys.exit(1)

pct = 100.0 * covered / total
print(f"COVERAGE: {pct:.2f}% ({covered}/{total}) umbral {min_pct:.0f}%  [{report}]")
if pct + 1e-9 < min_pct:
    print(f"COVERAGE-FAIL: por debajo del umbral de {min_pct:.0f}%", file=sys.stderr)
    sys.exit(1)
PY

printf 'COVERAGE-OK\n'
