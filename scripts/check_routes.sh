#!/usr/bin/env bash
# Comprueba que cada ruta tipada de navigation/ tiene fila en docs/routes.md
# y que las filas nv-01 sin implementar no fingen ser clases. Constitución §4.
set -euo pipefail
export LC_ALL=C

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

# `CheckEmailRoute(mode, email)` y `debug/conexion` se normalizan igual que en código.
normalize() {
    printf '%s' "$1" | sed 's/(.*//' | tr '[:upper:]' '[:lower:]' | sed 's/conexion/connection/g; s/[^a-z0-9]//g'
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
    [ -n "$normalized" ] || continue

    [ -n "${documented_routes[$normalized]:-}" ] && fail "ruta duplicada en $ROUTES_DOC: $route"
    documented_routes["$normalized"]="$route"
    documented_norms["$normalized"]="$spec"
done < "$ROUTES_DOC"

# Solo las declaraciones tipadas (`data object X` / `data class X`); no se
# buscan `hasRoute`/`startRoute`, que no son rutas tipadas.
mapfile -t route_classes < <(
    grep -rhoE 'data (object|class) [A-Za-z0-9_]+' "$NAV_DIR" \
        | awk '{print $3}' | sort -u
)
[ "${#route_classes[@]}" -gt 0 ] || fail "no hay rutas tipadas en $NAV_DIR"

declare -A code_norms=()
for class_name in "${route_classes[@]}"; do
    normalized="$(normalize "$class_name")"
    [ -n "$normalized" ] || fail "no se pudo normalizar la clase $class_name"
    code_norms["$normalized"]="$class_name"

    [ -n "${documented_routes[$normalized]:-}" ] || fail "ruta sin fila en $ROUTES_DOC: $class_name"
done

# Recíproco: una fila con espec nv-01 que parece clase debe existir en código.
for normalized in "${!documented_norms[@]}"; do
    [ "${documented_norms[$normalized]}" = "nv-01" ] || continue
    route_name="${documented_routes[$normalized]}"
    case "$route_name" in
        (*[!A-Za-z0-9_]*) continue ;;
    esac
    [ -n "${code_norms[$normalized]:-}" ] || fail "ruta nv-01 documentada sin clase tipada: $route_name"
done

printf 'OK\n'
