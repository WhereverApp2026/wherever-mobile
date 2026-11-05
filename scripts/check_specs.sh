#!/usr/bin/env bash
# Verifica que toda spec de producto (specs/NNN-*) tenga la estructura
# canónica de 7 secciones + Aprobación (constitución §11) y declare en su
# sección 6 las suites obligatorias Unit, Data (integración) y Security
# (constitución §7). Sale con SPECS-OK o SPECS-FAIL.
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

errors=()
shopt -s nullglob
specs=(specs/[0-9][0-9][0-9]-*/spec.md)
shopt -u nullglob

if [ "${#specs[@]}" -eq 0 ]; then
    printf 'SPECS-FAIL: no hay specs en specs/NNN-*\n' >&2
    exit 1
fi

for spec in "${specs[@]}"; do
    name="$(basename "$(dirname "$spec")")"

    for section in "${sections[@]}"; do
        grep -q "$section" "$spec" || errors+=("$name: falta la sección $section")
    done

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
