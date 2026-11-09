# MASVS Checklist — vivo

> Constitución §6. Trazable a `nv-01`. Referencia: OWASP MASVS.
> Las casillas las marca el revisor. "Evidencia" lista las pruebas y puertas que cubren cada control (2026-11-05); cada spec las cita en su `plan.md §3`.

- [ ] STORAGE: tokens cifrados con Keystore; sin datos sensibles en logs, capturas ni respaldos
  - Evidencia: `EncryptedTokensSerializerTest` (el archivo no contiene los tokens en claro), `SourceSecurityTest` (tokens solo en el `TokenStore` cifrado; SharedPreferences solo guarda marcas; logs solo en el interceptor redactado), `AuthIntegrationTest` (logs de red sin tokens, contraseñas ni correos), `InterceptorsTest` (log sin cabeceras ni cuerpo), `PlatformSecurityTest` (sin backup).
- [ ] CRYPTO: sin cripto propia; solo APIs de Keystore y bibliotecas mantenidas
  - Evidencia: `KeystoreTokenCipher` (AES-256-GCM de Keystore, IV aleatorio) cubierto por `EncryptedTokensSerializerTest`; `SourceSecurityTest` comprueba el cableado con Keystore. Semgrep registry lo marca como `gcm-detection` informativo.
- [ ] AUTH: refresh seguro, un solo reintento ante 401, sesión limpia si falla
  - Evidencia: `TokenRefreshAuthenticatorTest` (un solo refresh, dos 401 simultáneos, rotación), `AuthIntegrationTest` (401 → refresh de la doc → repite; refresh rechazado limpia; logout limpia tokens, Room y onboarding; 401 de login sin refresh), `MainViewModelTest` (sesión rechazada → Login).
- [ ] NETWORK: solo HTTPS (`networkSecurityConfig`), sin confiar certificados a ciegas
  - Evidencia: `PlatformSecurityTest` (cleartext prohibido, solo CA del sistema, sin `domain-config` ni `debug-overrides`), `SourceSecurityTest` (sin `http://` ni URL base fija), `checkedApiBaseUrl` en `app/build.gradle.kts`, regla Semgrep `wherever-no-cleartext-http`. La prueba instrumentada usa HTTPS local con un certificado que solo confía su propio cliente.
- [ ] PLATFORM: `allowBackup` controlado, componentes no exportados salvo necesidad, permisos mínimos
  - Evidencia: `PlatformSecurityTest` (`allowBackup=false`, reglas de extracción que excluyen todo, solo la actividad launcher exportada, solo `INTERNET`), `SourceSecurityTest` (rutas sin tokens ni contraseñas).
- [ ] CODE: dependencias revisadas, `lint` y `detekt` como error, sin código de depuración en release
  - Evidencia: CI `ci-pr.yml` (gitleaks bloqueante, OSV informativo, Android Lint con advertencias como error, detekt, Semgrep propio bloqueante), `SourceSecurityTest` (sin llaves ni credenciales), `ValidatorsTest` y pruebas de ViewModel (entradas validadas antes de enviar).
- [ ] RESILIENCE: R8 activo en release; sin datos de depuración en el APK
  - Evidencia: `isMinifyEnabled = true` en release; el log de red solo existe con `BuildConfig.DEBUG`. Sin prueba automática.
- [ ] PRIVACY: aviso de privacidad y eliminación de cuenta (posterior), permisos en contexto
  - Evidencia: `SettingsViewModelsTest` y `ProfileIntegrationTest` (eliminar cuenta 202/422, cancelar 409, la sesión local se limpia), `AuthIntegrationTest` (el 202 de recuperar no trae datos de la cuenta).
