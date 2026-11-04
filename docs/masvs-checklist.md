# MASVS Checklist — vivo

> Constitución §6. Trazable a `nv-01` y, en auth/privacidad, a `nv-05`. Referencia: OWASP MASVS.

- [x] STORAGE: tokens cifrados con Keystore; sin datos sensibles en logs, capturas ni respaldos
- [x] CRYPTO: sin cripto propia; solo APIs de Keystore y bibliotecas mantenidas
- [x] AUTH: `Authenticator` de un reintento y refresh real implementados en `nv-05`; reset con respuesta genérica y código manual; sesión limpiada ante rechazo del servidor
- [x] NETWORK: solo HTTPS (`networkSecurityConfig`), sin confiar certificados a ciegas; CA de usuario solo en `debug-overrides`
- [x] PLATFORM: `allowBackup=false` con reglas explícitas, componentes no exportados salvo `MainActivity`, solo `INTERNET` en `nv-01`
- [x] CODE: `ktlintCheck`, `detekt` y `lintDebug` sin errores en `nv-01`; sin código de depuración de producto
- [ ] RESILIENCE: R8/minify de release aún no activado; sin datos de depuración en el APK
- [ ] PRIVACY: enlaces de términos y privacidad en `nv-05`; eliminación de cuenta `[planificado nv-13]`; permisos en contexto

Evidencia `nv-01`: `EncryptedTokenStore` (AES/GCM + Keystore), `app/src/*/res/xml/network_security_config.xml`, `AndroidManifest.xml`, `scripts/critic-guardrails.sh → PASS`.
