#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ROUTES_DOC="docs/routes.md"
NAV_DIR="app/src/main/java/com/wherever/mobile/navigation"

fail() {
    printf 'ROUTES-FAIL: %s\n' "$*" >&2
    exit 1
}

[ -f "$ROUTES_DOC" ] || fail "falta $ROUTES_DOC"
[ -d "$NAV_DIR" ] || fail "falta $NAV_DIR"
grep -q '^| Ruta |' "$ROUTES_DOC" || fail "la tabla de rutas no tiene cabecera"

normalize() {
    printf '%s' "$1" | tr '[:upper:]' '[:lower:]' | sed 's/conexion/connection/g; s/[^a-z0-9]//g'
}

declare -A documented_routes=()
declare -A documented_norms=()

while IFS= read -r line; do
    case "$line" in
        '| `'*) ;;
        *) continue ;;
    esac

    route="$(printf '%s\n' "$line" | sed -n 's/^| `\([^`]*\)`.*/\1/p')"
    [ -n "$route" ] || continue

    spec="$(printf '%s\n' "$line" | awk -F'|' '{gsub(/^[ \t]+|[ \t]+$/, "", $6); print $6}')"
    normalized="$(normalize "$route")"

    [ -n "${documented_routes[$normalized]:-}" ] && fail "ruta duplicada en $ROUTES_DOC: $route"
    documented_routes["$normalized"]="$route"
    documented_norms["$normalized"]="$spec"
done < "$ROUTES_DOC"

mapfile -t route_classes < <(grep -rhoE '[A-Za-z0-9_]+Route' "$NAV_DIR" | sort -u)
[ "${#route_classes[@]}" -gt 0 ] || fail "no hay rutas tipadas en $NAV_DIR"

declare -A code_norms=()
for class_name in "${route_classes[@]}"; do
    base="${class_name%Route}"
    normalized="$(normalize "$base")"
    [ -n "$normalized" ] || fail "no se pudo normalizar la clase $class_name"
    code_norms["$normalized"]="$class_name"

    [ -n "${documented_routes[$normalized]:-}" ] || fail "ruta sin fila en $ROUTES_DOC: $class_name"
done

for normalized in "${!documented_norms[@]}"; do
    if [ "${documented_norms[$normalized]}" = "nv-01" ]; then
        [ -n "${code_norms[$normalized]:-}" ] || fail "ruta nv-01 documentada sin clase tipada: ${documented_routes[$normalized]}"
    fi
done

printf 'OK\n'
