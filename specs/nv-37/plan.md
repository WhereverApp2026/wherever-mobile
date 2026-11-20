# plan.md — NV-37 — Negocio: cupones y cupo

> Deriva de `specs/nv-37/spec.md`. Responsable: Osmar. Respeta constitución §4 (cadena de red y capas).

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
- `CouponFormViewModel`: reglas por tipo (porcentaje 1–100, monto > 0), vigencia fin > inicio, título 1–40.
- `CouponsViewModel`: interruptor con 409 `con_canjes`.
- `CapacityViewModel`: múltiplos de 15 y 1–500.

### Datos (MockWebServer)
- MockWebServer: `POST cupones`, `PATCH` con `If-Match`, `PUT reservas-config` con `If-Match` (422 `cobros_no_activos`).

### Seguridad
- La validación de cupón no registra el código en logs.

### UI (Compose)
- Compose: la vista previa del cupón refleja el tipo y el valor.

## 4. Secuencia

1. Datos: DTOs, API y repositorio con su prueba de MockWebServer.
2. Dominio y ViewModels con pruebas unitarias.
3. Pantallas (`Content` + `Route`) frame por frame, con `@Preview` y captura.
4. Rutas y punto de entrada; `docs/routes.md`.
5. Compuertas una por una; prueba en teléfono o emulador contra staging.
6. PR a `main`.

## 5. Verificación

Ver `CONTRIBUTING.md` §Compuertas: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, cada una con `--no-daemon --max-workers=1`.
