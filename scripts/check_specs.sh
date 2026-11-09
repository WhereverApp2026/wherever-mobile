#!/usr/bin/env bash
# Verifica que toda spec de funcionalidad (specs/nv-04 en adelante) tenga la
# estructura canónica de 7 secciones + Aprobación (constitución §11,
# .specify/templates/spec-template.md), los cuatro archivos (spec, plan, task,
# checklists/gates), el revisor nombrado en la Aprobación y en su sección 6 las
# suites obligatorias Unit, Data (integración) y Security (constitución §7).
# Las fases nv-00…nv-03 usan otra forma y las revisa check_sdd.sh.
# Sale con SPECS-OK o SPECS-FAIL.
set -euo pipefail
export LC_ALL=C

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

sections=(
    '^## 1. Objetivo'
    '^## 2. Alcance'
    '^## 3. Contratos'
    '^## 4. Modelo de datos'
    '^## 5. Seguridad'
    '^## 6. Tests'
    '^## 7. Plan de entrega'
    '^## Aprobaci'
)
suites=(Unit Data Security)
reviewer='^- \[[ x]\] Revisor: [A-Z]'

errors=()
specs=()
shopt -s nullglob
for dir in specs/nv-[0-9][0-9]; do
    number="${dir##*nv-}"
    if [ "$((10#$number))" -ge 4 ]; then
        specs+=("$dir")
    fi
done
shopt -u nullglob

if [ "${#specs[@]}" -eq 0 ]; then
    printf 'SPECS-FAIL: no hay specs en specs/nv-04 en adelante\n' >&2
    exit 1
fi

for dir in "${specs[@]}"; do
    name="$(basename "$dir")"
    spec="$dir/spec.md"

    for file in spec.md plan.md task.md checklists/gates.md; do
        [ -f "$dir/$file" ] || errors+=("$name: falta $file")
    done
    [ -f "$spec" ] || continue

    for section in "${sections[@]}"; do
        grep -q "$section" "$spec" || errors+=("$name: falta la sección $section")
    done
    grep -q "$reviewer" "$spec" || errors+=("$name: la Aprobación no nombra al revisor")

    start="$(grep -n '^## 6\.' "$spec" | head -n 1 | cut -d: -f1 || true)"
    end="$(grep -n '^## 7\.' "$spec" | head -n 1 | cut -d: -f1 || true)"
    if [ -z "${start:-}" ] || [ -z "${end:-}" ] || [ "$end" -le "$start" ]; then
        errors+=("$name: no se pudo leer la sección 6")
        continue
    fi

    body="$(sed -n "$((start + 1)),$((end - 1))p" "$spec")"
    for suite in "${suites[@]}"; do
        printf '%s\n' "$body" | grep -qi "$suite" \
            || errors+=("$name: la sección 6 no declara la suite $suite")
    done
done

if [ "${#errors[@]}" -gt 0 ]; then
    printf 'SPECS-FAIL (%d)\n' "${#errors[@]}" >&2
    printf ' - %s\n' "${errors[@]}" >&2
    exit 1
fi

printf 'SPECS-OK (%d specs)\n' "${#specs[@]}"
