# NV-04 — Arquitectura de Wherever Móvil

## T1 — Spec de arquitectura
- **Crear**: `spec.md`, `plan.md`, `task.md`, `checklists/gates.md`.
- **Verificar**: las decisiones que no vienen de la constitución están marcadas como "Decisión propuesta".

## T2 — Proyecto Gradle
- **Crear**: proyecto completo con pantalla `home` mínima.
- **Verificar**: `gradlew.bat assembleDebug`.
- **No hacer**: pantallas del producto ni implementar la red (`nv-01`).

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../core/network/SafeApiCallTest.kt` — cada `codigo` de la API → `AppError`, `Retry-After`, cuerpo ilegible
- [x] Unit: `test/.../core/network/InterceptorsTest.kt` — Bearer solo en rutas con JWT y `request_id` en cada petición
- [x] Unit: `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — 401 → un solo refresh, refresh rotado, dos 401 simultáneos
- [x] Unit: `test/.../core/common/ValidatorsTest.kt` — validaciones comunes
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — cadena de red real con respuestas de la doc: 401 con refresh y refresh rechazado
- [x] Integración: `test/.../integration/ProfileIntegrationTest.kt` — `If-Match`, 409, 422 y 401 con refresh en `PATCH /usuarios/yo`
- [ ] Integración: `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — Room real + MockWebServer por HTTPS local (instrumentada)
- [ ] Integración: `androidTest/.../feature/profile/data/UserProfileDaoTest.kt` — DAO con Room real (instrumentada)
- [x] Seguridad: `test/.../core/security/EncryptedTokensSerializerTest.kt` — el archivo cifrado no contiene los tokens en claro
- [x] Seguridad: `test/.../security/PlatformSecurityTest.kt` — sin backup, solo HTTPS, solo CA del sistema, componentes no exportados, permisos mínimos
- [x] Seguridad: `test/.../security/SourceSecurityTest.kt` — sin secretos ni URLs fijas; tokens solo cifrados; logs solo en el interceptor redactado
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — logs de red sin tokens, contraseñas ni correos
- [x] Seguridad: `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — refresh rechazado limpia la sesión
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
