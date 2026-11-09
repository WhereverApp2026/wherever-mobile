#!/usr/bin/env bash
# Puerta SDD: artefactos vivos, secciones de las specs de fase nv-*,
# sin marcadores sin issue y AGENTS.md al día. Constitución §11.
set -euo pipefail
export LC_ALL=C

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

fail() {
    printf 'SDD-FAIL: %s\n' "$*" >&2
    exit 1
}

for file in \
    AGENTS.md \
    Memoria.md \
    constitution.md \
    .specify/memory/constitution.md \
    docs/routes.md \
    specs/nv-01/spec.md; do
    [ -f "$file" ] || fail "falta $file"
done

for phase_dir in specs/nv-*; do
    [ -d "$phase_dir" ] || continue
    phase="$(basename "$phase_dir")"

    for file in spec.md plan.md task.md checklists/gates.md; do
        [ -f "$phase_dir/$file" ] || fail "$phase sin $file"
    done

    # nv-04 en adelante siguen spec-template.md: sus secciones las revisa check_specs.sh.
    number="${phase##*nv-}"
    if [ "$((10#$number))" -ge 4 ]; then
        continue
    fi

    for section in \
        '## Contexto' \
        '## Requisitos' \
        '## Diseño' \
        '## Contratos' \
        '## Tests' \
        '## Criterios' \
        '## Límites' \
        '## Aprobación y Control de Cambios'; do
        grep -qF "$section" "$phase_dir/spec.md" || fail "$phase/spec.md sin sección: $section"
    done
done

placeholders="$(grep -RInE '(TODO|FIXME)[[:space:]]*[:(]|TBD' specs --include='*.md' 2>/dev/null || true)"
[ -z "$placeholders" ] || fail "marcadores sin issue o decisión: $placeholders"

[ -f "specs/nv-01/checklists/gates.md" ] || fail "falta la checklist de nv-01"
grep -q 'compileSdk' AGENTS.md || fail "AGENTS.md no registra compileSdk"

printf 'SDD-OK\n'
