#!/usr/bin/env bash
set -euo pipefail

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

secret_hits="$(
    grep -RInEi \
        '(AIza[0-9A-Za-z_-]{35}|ghp_[0-9A-Za-z]{36}|sk-[0-9A-Za-z]{20,}|AKIA[0-9A-Z]{16}|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----|(password|passwd|client[_-]?secret|api[_-]?key)[[:space:]]*[:=][[:space:]]*["'"'"'][^"'"'"']{6,}["'"'"'])' \
        "${SCAN_PATHS[@]}" 2>/dev/null || true
)"
[ -z "$secret_hits" ] || fail "B1 posibles secretos: $secret_hits"

if git ls-files --error-unmatch local.properties >/dev/null 2>&1; then
    fail "B1 local.properties no debe estar versionado"
fi
if git ls-files --error-unmatch app/google-services.json >/dev/null 2>&1; then
    fail "B1 google-services.json no debe estar versionado"
fi

token_logs="$(grep -RInE 'Log\.[a-zA-Z]+\([^)]*(token|accessToken|refreshToken|Authorization)' app/src/main/java 2>/dev/null || true)"
[ -z "$token_logs" ] || fail "B2 tokens en logs: $token_logs"

NETWORK_MODULE="app/src/main/java/com/wherever/mobile/core/network/NetworkModule.kt"
[ -f "$NETWORK_MODULE" ] || fail "B3 falta $NETWORK_MODULE"

order="$(grep -oE 'addInterceptor\([A-Za-z]+\)' "$NETWORK_MODULE" | tr '\n' ' ')"
expected="addInterceptor(requestIdInterceptor) addInterceptor(authHeaderInterceptor) addInterceptor(redactedLoggingInterceptor) "
[ "$order" = "$expected" ] || fail "B3 orden de interceptores alterado: $order"

grep -q 'authenticator(tokenAuthenticator)' "$NETWORK_MODULE" || fail "B3 falta el Authenticator en OkHttp"

for config in \
    app/src/main/res/xml/network_security_config.xml \
    app/src/debug/res/xml/network_security_config.xml; do
    [ -f "$config" ] || fail "B3 falta $config"
    grep -q 'cleartextTrafficPermitted="false"' "$config" || fail "B3 HTTP en claro permitido en $config"
done

grep -q 'certificates src="user"' app/src/debug/res/xml/network_security_config.xml || fail "B3 debug no confía en la CA de desarrollo"
grep -q 'debug-overrides' app/src/debug/res/xml/network_security_config.xml || fail "B3 CA de usuario fuera de debug-overrides"
if grep -q 'certificates src="user"' app/src/main/res/xml/network_security_config.xml; then
    fail "B3 CA de usuario en release"
fi

"$ROOT/scripts/check_routes.sh" >/dev/null || fail "B4 rutas sin fila en docs/routes.md"

printf 'PASS\n'
