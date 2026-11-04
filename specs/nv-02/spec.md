# NV-02 — Molde de funcionalidad: Repository → ViewModel → UiState

## Contexto
Molde reutilizable que toda funcionalidad copiará, con un caso de demostración que no es una pantalla del producto. **Depende de**: nv-01.

## Requisitos
1. R1 — Tipos comunes: `Result<T>`, `AppError`, `PageResult{items, nextCursor, limit}`; en el contrato JSON se conservan `siguienteCursor` y `limite`.
2. R2 — `Repository` base: Room como fuente de verdad + actualización desde la red.
3. R3 — `UiState` y `ViewModel` base con `StateFlow` y eventos de una sola vez.
4. R4 — Regla de capas comprobada por una prueba de arquitectura.

## Diseño
- Flujo: `DTO → Mapper → Entity (Room) → Domain → UiState`.
- Paquetes: `core/common` (`Result`, `PageResult`), `core/data` (`BaseRepository`), `core/ui` (`UiState`, `BaseViewModel`).
- El ViewModel nunca conoce Retrofit ni Room; el Composable nunca conoce el repositorio.
- Demostración: `DemoRepository`/`DemoViewModel` solo en el source set de pruebas, sin pantalla ni artefacto de release.
- Paginación por cursor con límite máximo 50.
- Los identificadores Kotlin van en inglés; los campos del contrato JSON conservan el nombre del backend.

## Contratos
N/A — sin endpoints nuevos; usa el formato de error y paginación del backend.

## Tests
- Arquitectura (Konsist) 5: capas, nombres, sin `Dispatchers.IO` fijo, sin textos fijos en Composables, sin acceso a Retrofit desde ViewModel.
- Unit — `PageResult`/cursor 4, `Result` y mapeos 4, ViewModel base 4.
- Demo — `DemoRepository`/`DemoViewModel` solo en pruebas; sin pantalla ni producción.
- Total: 17 pruebas + demo.

## Criterios
- La prueba de arquitectura falla si una capa se salta.
- El molde se copia en una nueva feature sin tocar `core/*`.
- `DemoRepository`/`DemoViewModel` no existen fuera del source set de pruebas.
- `core/common`, `core/data` y `core/ui` están registrados en `AGENTS.md`.

## Límites
Sin pantallas, sin entidades del producto, sin sincronización en segundo plano real.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador sin ejecutar
- **Revisores:** (pendiente de nombre)
- **Fecha:** —
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-02/checklists/gates.md` + `Memoria.md`.
