# plan.md — NV-33 — Ayuda

> Deriva de `specs/nv-33/spec.md`. Responsable: Diego. Respeta constitución §4 (cadena de red y capas).

## 1. Mapeo (archivos a crear)

- `app/src/main/java/com/wherever/mobile/feature/help/data/`: interfaz Retrofit, DTOs `@Serializable` (`snake_case` con `@SerialName`), `...RepositoryImpl` con `safeApiCall`, módulo Hilt en `data/di/`.
- `feature/help/domain/`: modelos y la interfaz del repositorio (sin Android).
- `feature/help/ui/`: un `UiState` y un `@HiltViewModel` por pantalla (`StateFlow`), `<Pantalla>Route` (con ViewModel) y `<Pantalla>Content` (sin ViewModel, con `@Preview` claro y oscuro).
- Reutilizar sin modificar: `PhotoRepository` (subida de fotos, nv-45), `core/designsystem`, `core/network`, `safeApiCall`, `AppError`, `PageResult`.
- Pruebas en `app/src/test/java/com/wherever/mobile/feature/help/` y capturas en `ScreenshotTest`.

## 2. Guardarraíles

- Constitución §2-§6 y `CONTRIBUTING.md`: sin lógica en Composables, dispatchers inyectados, textos en `strings.xml`, sin secretos.
- Konsist (`ArchitectureTest`) prohíbe que `ui` importe `core.network`, Room o `data`.
- Nada simulado: si falta un endpoint, se oculta el control y se anota en la spec.

## 3. Pruebas

### Unitarias
- `HelpViewModel`: filtro sin acentos, vacío y envío del reporte (éxito, 422, sin red).

### Datos (MockWebServer)
- MockWebServer: `POST /errores` manda `modulo`, `mensaje` y `version`; 202 sin cuerpo.

### Seguridad
- El mensaje se recorta a 500 y no agrega correo ni teléfono del perfil.

### UI (Compose)
- Compose: el acordeón abre y cierra; sin resultados muestra contactos.

## 4. Secuencia

1. Datos: DTOs, API y repositorio con su prueba de MockWebServer.
2. Dominio y ViewModels con pruebas unitarias.
3. Pantallas (`Content` + `Route`) frame por frame, con `@Preview` y captura.
4. Rutas y punto de entrada; `docs/routes.md`.
5. Compuertas una por una; prueba en teléfono o emulador contra staging.
6. PR a `main`.

## 5. Verificación

Ver `CONTRIBUTING.md` §Compuertas: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, cada una con `--no-daemon --max-workers=1`.

## 6. Preguntas frecuentes iniciales (`strings.xml`)

1. ¿Cómo armo un plan con la IA? 2. ¿Cómo reservo y cuánto cuesta el anticipo? 3. ¿Cómo cancelo una reserva? 4. ¿Qué es el Sello de Excelencia?
5. ¿Cómo agrego amigos? 6. ¿Quién ve mi ubicación? 7. ¿Cómo uso un cupón? 8. ¿Cómo elimino mi cuenta?
Las respuestas se redactan con los textos ya usados en la app; Jorge las revisa en el PR.
