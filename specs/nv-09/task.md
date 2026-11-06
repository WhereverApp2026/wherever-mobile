# NV-09 — Registro con correo

## T1 — Implementación
- [x] Validación en vivo de nombre, edad y contraseña; el correo se comprueba con `GET /usuarios/correo-disponible` al salir del campo.
- [x] Se agregó la fecha de nacimiento, que la maqueta no tiene y la API exige (D01). Se elige con `DatePicker`.
- [x] `version_terminos` viene de `BuildConfig.TERMS_VERSION`; `promociones_correo` es `false` (D14).
- [x] Los errores `campos[]` de la API se muestran bajo su campo.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/auth/ui/AuthViewModelsTest.kt` — validación en vivo, términos, correo disponible, fecha ISO, 409 y 422 por campo
- [x] Unit: `test/.../core/common/ValidatorsTest.kt` — nombre, edad ≥ 18 y contraseña
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — `POST /usuarios` 201 de la doc deja sesión; 409 llega como conflicto
- [x] Integración: `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — registro manda términos y su versión
- [x] Seguridad: `test/.../core/common/ValidatorsTest.kt` — entradas validadas antes de enviar
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — la contraseña no aparece en logs
- [x] Seguridad: `test/.../core/security/EncryptedTokensSerializerTest.kt` — los tokens de la sesión nueva se guardan cifrados
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
