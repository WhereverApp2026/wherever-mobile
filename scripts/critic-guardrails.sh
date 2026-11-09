#!/usr/bin/env bash
# Puerta crítica B1-B4 (constitución §2, §6) para esta rama:
# NetworkModule vive en core/di y RedactedLogging entra por debugLogging().
set -euo pipefail
export LC_ALL=C

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

fail() {
    printf 'PASS-FAIL: %s\n' "$*" >&2
    exit 1
}

SCAN_PATHS=(
    app/src
    gradle
    scripts
    .github
    .semgrep
    app/build.gradle.kts
    build.gradle.kts
    settings.gradle.kts
    local.properties.example
)
existing=()
for path in "${SCAN_PATHS[@]}"; do
    [ -e "$path" ] && existing+=("$path")
done

# Claves reales (API, tokens, llaves privadas): en todo el repo, tests incluidos.
KEY_PATTERN='(AIza[0-9A-Za-z_-]{35}|ghp_[0-9A-Za-z]{36}|sk-[0-9A-Za-z]{20,}|AKIA[0-9A-Z]{16}|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----)'
# Credenciales hardcodeadas: solo fuera de los juegos de pruebas, donde un
# password de fixture (p. ej. TEST_PASSWORD) no es un secreto.
CRED_PATTERN='(password|passwd|client[_-]?secret|api[_-]?key)[[:space:]]*[:=][[:space:]]*["'"'"'][^"'"'"']{6,}["'"'"']'

key_hits="$(grep -RInEi "$KEY_PATTERN" "${existing[@]}" 2>/dev/null || true)"
[ -z "$key_hits" ] || fail "B1 posibles claves: $key_hits"

cred_hits="$(
    grep -RInEi "$CRED_PATTERN" "${existing[@]}" 2>/dev/null \
        | grep -vE '^[^:]+/src/(test|androidTest)/' || true
)"
[ -z "$cred_hits" ] || fail "B1 posibles secretos: $cred_hits"

if git ls-files --error-unmatch local.properties >/dev/null 2>&1; then
    fail "B1 local.properties no debe estar versionado"
fi
if git ls-files --error-unmatch app/google-services.json >/dev/null 2>&1; then
    fail "B1 google-services.json no debe estar versionado"
fi

token_logs="$(grep -RInE 'Log\.[a-zA-Z]+\([^)]*(token|accessToken|refreshToken|Authorization)' app/src/main/java 2>/dev/null || true)"
[ -z "$token_logs" ] || fail "B2 tokens en logs: $token_logs"

NETWORK_MODULE="app/src/main/java/com/wherever/mobile/core/di/NetworkModule.kt"
[ -f "$NETWORK_MODULE" ] || fail "B3 falta $NETWORK_MODULE"

client="$(awk '/fun provideClient\(/{f=1} f{print} f&&/fun provideRetrofit/{exit}' "$NETWORK_MODULE")"
[ -n "$client" ] || fail "B3 no se encontró provideClient"

position() {
    printf '%s\n' "$client" | grep -n "$1" | head -n 1 | cut -d: -f1 || true
}
request_id="$(position 'RequestIdInterceptor(')"
auth_header="$(position 'AuthHeaderInterceptor(')"
redacted="$(position 'debugLogging()')"
authenticator="$(position 'authenticator(TokenRefreshAuthenticator')"

[ -n "$request_id" ] || fail "B3 cliente autenticado sin RequestIdInterceptor"
[ -n "$auth_header" ] || fail "B3 cliente autenticado sin AuthHeaderInterceptor"
[ -n "$redacted" ] || fail "B3 cliente autenticado sin RedactedLogging (debugLogging)"
[ -n "$authenticator" ] || fail "B3 falta el Authenticator en OkHttp"

if [ "$request_id" -lt "$auth_header" ] && [ "$auth_header" -lt "$redacted" ] && [ "$redacted" -lt "$authenticator" ]; then
    :
else
    fail "B3 orden alterado (RequestId → AuthHeader → RedactedLogging → Authenticator): $request_id $auth_header $redacted $authenticator"
fi

refresh="$(awk '/fun provideRefreshClient\(/{f=1} f{print} f&&/fun provideRefreshApi/{exit}' "$NETWORK_MODULE")"
[ -n "$refresh" ] || fail "B3 no se encontró provideRefreshClient"
printf '%s' "$refresh" | grep -q 'RequestIdInterceptor(' || fail "B3 refresh client sin RequestIdInterceptor"
if printf '%s' "$refresh" | grep -qE 'AuthHeaderInterceptor|authenticator\('; then
    fail "B3 refresh client con AuthHeader o Authenticator: recursión al renovar"
fi

MAIN_CFG="app/src/main/res/xml/network_security_config.xml"
[ -f "$MAIN_CFG" ] || fail "B3 falta $MAIN_CFG"
grep -q 'cleartextTrafficPermitted="false"' "$MAIN_CFG" || fail "B3 HTTP en claro permitido en $MAIN_CFG"
grep -q 'certificates src="system"' "$MAIN_CFG" || fail "B3 release sin CA del sistema en $MAIN_CFG"
if grep -q 'certificates src="user"' "$MAIN_CFG"; then
    fail "B3 CA de usuario en release"
fi

MANIFEST="app/src/main/AndroidManifest.xml"
[ -f "$MANIFEST" ] || fail "B3 falta $MANIFEST"
grep -q 'android:allowBackup="false"' "$MANIFEST" || fail "B3 allowBackup no es false en $MANIFEST"

DEBUG_CFG="app/src/debug/res/xml/network_security_config.xml"
if [ -d app/src/debug ]; then
    [ -f "$DEBUG_CFG" ] || fail "B3 falta $DEBUG_CFG"
    grep -q 'cleartextTrafficPermitted="false"' "$DEBUG_CFG" || fail "B3 HTTP en claro permitido en $DEBUG_CFG"
    grep -q 'debug-overrides' "$DEBUG_CFG" || fail "B3 CA de usuario fuera de debug-overrides"
    grep -q 'certificates src="user"' "$DEBUG_CFG" || fail "B3 debug no confía en la CA de desarrollo"
fi

"$ROOT/scripts/check_routes.sh" >/dev/null || fail "B4 rutas sin fila en docs/routes.md"

printf 'PASS\n'
