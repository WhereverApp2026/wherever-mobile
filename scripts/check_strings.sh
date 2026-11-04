#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

STRINGS_XML="app/src/main/res/values/strings.xml"
MAIN_DIR="app/src/main/java"

fail() {
    printf 'STRINGS-FAIL: %s\n' "$*" >&2
    exit 1
}

[ -f "$STRINGS_XML" ] || fail "falta $STRINGS_XML"
[ -d "$MAIN_DIR" ] || fail "falta $MAIN_DIR"

defined="$(sed -n 's/.*<string name="\([^"]*\)".*/\1/p' "$STRINGS_XML" | sort -u)"
[ -n "$defined" ] || fail "no hay strings definidos"

refs="$(grep -rhoE 'R\.string\.[A-Za-z0-9_]+' "$MAIN_DIR" | sed 's/^R\.string\.//' | sort -u)"

for ref in $refs; do
    printf '%s\n' "$defined" | grep -qx "$ref" || fail "string usado sin definir: R.string.$ref"
done

duplicates="$(sed -n 's/.*<string name="\([^"]*\)".*/\1/p' "$STRINGS_XML" | sort | uniq -d)"
[ -z "$duplicates" ] || fail "strings duplicados: $duplicates"

hardcoded="$(grep -RInE 'Text\("|contentDescription[[:space:]]*=[[:space:]]*"|android:text[[:space:]]*=[[:space:]]*"' "$MAIN_DIR" 2>/dev/null || true)"
[ -z "$hardcoded" ] || fail "texto hardcodeado en UI: $hardcoded"

printf 'OK\n'
