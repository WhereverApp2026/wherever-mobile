# plan.md — NV-34 — Negocio: registro

> Deriva de `specs/nv-34/spec.md`. Responsable: Osmar. Respeta constitución §4 (cadena de red y capas).

## 1. Mapeo (archivos a crear)

- `app/src/main/java/com/wherever/mobile/feature/business/data/`: interfaz Retrofit, DTOs `@Serializable` (`snake_case` con `@SerialName`), `...RepositoryImpl` con `safeApiCall`, módulo Hilt en `data/di/`.
- `feature/business/domain/`: modelos y la interfaz del repositorio (sin Android).
- `feature/business/ui/`: un `UiState` y un `@HiltViewModel` por pantalla (`StateFlow`), `<Pantalla>Route` (con ViewModel) y `<Pantalla>Content` (sin ViewModel, con `@Preview` claro y oscuro).
- Reutilizar sin modificar: `PhotoRepository` (subida de fotos, nv-45), `core/designsystem`, `core/network`, `safeApiCall`, `AppError`, `PageResult`.
- Pruebas en `app/src/test/java/com/wherever/mobile/feature/business/` y capturas en `ScreenshotTest`.

## 2. Guardarraíles

- Constitución §2-§6 y `CONTRIBUTING.md`: sin lógica en Composables, dispatchers inyectados, textos en `strings.xml`, sin secretos.
- Konsist (`ArchitectureTest`) prohíbe que `ui` importe `core.network`, Room o `data`.
- Nada simulado: si falta un endpoint, se oculta el control y se anota en la spec.

## 3. Pruebas

### Unitarias
- `BusinessOnboardingViewModel`: avance por pasos, retomar en `paso_alta`, Guardar y salir, 422 con lista de faltantes.
- Validaciones: nombre 3–60, descripción ≤ 300, CLABE de 18 dígitos, documento ≤ 5 MB.

### Datos (MockWebServer)
- MockWebServer: `POST /negocios` con `requiere_refresh` dispara la renovación; `PATCH alta/{paso}` manda `If-Match`; multipart de documentos con `tipo` y `archivo`.

### Seguridad
- Ningún log contiene RFC, CLABE ni nombres de archivo del expediente (prueba sobre el interceptor de logs).

### UI (Compose)
- Compose: el botón Continuar se habilita solo con el paso válido; 422 de enviar-revision lista lo que falta.

## 4. Secuencia

1. Datos: DTOs, API y repositorio con su prueba de MockWebServer.
2. Dominio y ViewModels con pruebas unitarias.
3. Pantallas (`Content` + `Route`) frame por frame, con `@Preview` y captura.
4. Rutas y punto de entrada; `docs/routes.md`.
5. Compuertas una por una; prueba en teléfono o emulador contra staging.
6. PR a `main`.

## 5. Verificación

Ver `CONTRIBUTING.md` §Compuertas: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, cada una con `--no-daemon --max-workers=1`.
