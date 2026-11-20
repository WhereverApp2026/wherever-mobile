# plan.md — NV-31 — Recuerdos y postales

> Deriva de `specs/nv-31/spec.md`. Responsable: Diego. Respeta constitución §4 (cadena de red y capas).

## 1. Mapeo (archivos a crear)

- `app/src/main/java/com/wherever/mobile/feature/memories/data/`: interfaz Retrofit, DTOs `@Serializable` (`snake_case` con `@SerialName`), `...RepositoryImpl` con `safeApiCall`, módulo Hilt en `data/di/`.
- `feature/memories/domain/`: modelos y la interfaz del repositorio (sin Android).
- `feature/memories/ui/`: un `UiState` y un `@HiltViewModel` por pantalla (`StateFlow`), `<Pantalla>Route` (con ViewModel) y `<Pantalla>Content` (sin ViewModel, con `@Preview` claro y oscuro).
- Reutilizar sin modificar: `PhotoRepository` (subida de fotos, nv-45), `core/designsystem`, `core/network`, `safeApiCall`, `AppError`, `PageResult`.
- Pruebas en `app/src/test/java/com/wherever/mobile/feature/memories/` y capturas en `ScreenshotTest`.

## 2. Guardarraíles

- Constitución §2-§6 y `CONTRIBUTING.md`: sin lógica en Composables, dispatchers inyectados, textos en `strings.xml`, sin secretos.
- Konsist (`ArchitectureTest`) prohíbe que `ui` importe `core.network`, Room o `data`.
- Nada simulado: si falta un endpoint, se oculta el control y se anota en la spec.

## 3. Pruebas

### Unitarias
- `MemoriesViewModel`: paginación, vacío, error y reintentar.
- `PostcardViewModel`: estilo, texto ≤ 80, guardar, compartir y eliminar con confirmación.
- `CreateMomentViewModel`: etiquetados ≤ 10, visibilidad y doble toque en Publicar.

### Datos (MockWebServer)
- MockWebServer: `POST /postales` + `GET /postales` con cursor; `DELETE` manda `If-Match`; 404 de postal ajena → `AppError.NotFound`.

### Seguridad
- El archivo que se sube no trae EXIF GPS (prueba sobre un JPEG con ubicación).

### UI (Compose)
- Compose: `76_RecVacio` con Ver mis planes; Eliminar abre la confirmación.

## 4. Secuencia

1. Datos: DTOs, API y repositorio con su prueba de MockWebServer.
2. Dominio y ViewModels con pruebas unitarias.
3. Pantallas (`Content` + `Route`) frame por frame, con `@Preview` y captura.
4. Rutas y punto de entrada; `docs/routes.md`.
5. Compuertas una por una; prueba en teléfono o emulador contra staging.
6. PR a `main`.

## 5. Verificación

Ver `CONTRIBUTING.md` §Compuertas: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, cada una con `--no-daemon --max-workers=1`.
