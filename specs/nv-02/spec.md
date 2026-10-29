# NV-02 — Molde de funcionalidad: Repository → ViewModel → UiState

## Contexto
Molde reutilizable que toda funcionalidad copiará, con un caso de demostración que no es una pantalla del producto. **Depende de**: nv-01.

## Requisitos
1. Tipos comunes: `Result<T>`, `AppError`, `PageResult{items, siguienteCursor, limite}`.
2. `Repository` base: Room como fuente de verdad + actualización desde la red.
3. `UiState` y `ViewModel` base con `StateFlow` y eventos de una sola vez.
4. Regla de capas comprobada por una prueba de arquitectura.

## Diseño
- Flujo: `DTO → Mapper → Entity (Room) → Domain → UiState`.
- El ViewModel nunca conoce Retrofit ni Room; el Composable nunca conoce el repositorio.
- Demostración: `DemoRepository`/`DemoViewModel` solo en pruebas, sin pantalla.
- Paginación por cursor con límite máximo 50.

## Contratos
N/A — sin endpoints nuevos; usa el formato de error y paginación del backend.

## Tests
- Arquitectura (Konsist) 5: capas, nombres, sin `Dispatchers.IO` fijo, sin textos fijos en Composables, sin acceso a Retrofit desde ViewModel.
- Unit — `PageResult`/cursor 4, `Result` y mapeos 4, ViewModel base 4.

## Criterios
- La prueba de arquitectura falla si una capa se salta.
- El molde se copia en una nueva feature sin tocar `core/*`.

## Límites
Sin pantallas, sin entidades del producto, sin sincronización en segundo plano real.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador sin ejecutar
- **Revisores:** (pendiente de nombre)
- **Fecha:** —
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-02/checklists/gates.md` + `Memoria.md`.
