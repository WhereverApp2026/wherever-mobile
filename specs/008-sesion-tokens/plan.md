# plan.md — 008 — Sesión y tokens

1. Mapeo
- `core/network/*`
- `core/security/*`
- `core/di/NetworkModule.kt`
- `core/di/SecurityModule.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `core/network/TokenRefreshAuthenticatorTest.kt` (incluye dos 401 simultáneos = un solo refresh; se comprobó que falla sin el lock)
- `core/network/InterceptorsTest.kt`
- `core/network/SafeApiCallTest.kt`
- `core/security/EncryptedTokensSerializerTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
