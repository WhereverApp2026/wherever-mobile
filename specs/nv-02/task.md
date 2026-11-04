# NV-02 — Molde de funcionalidad: Repository → ViewModel → UiState

## T1 — Tipos comunes
- **Crear**: `Result`, `PageResult` (límite 50) y pruebas 8.
- **No hacer**: entidades del producto.

## T2 — Repositorio y ViewModel base
- **Crear**: `BaseRepository`, `UiState`, `BaseViewModel`, `DemoRepository`/`DemoViewModel` solo en `app/src/test` y pruebas 4.
- **No hacer**: demo en `main` ni pantalla.

## T3 — Arquitectura y documentación
- **Crear**: `ArchitectureTest.kt (5)`, `docs/feature-mold.md`.
- **Actualizar**: `AGENTS.md` con `core/common`, `core/data` y `core/ui`.
- **Verificar**: 5/5 y ausencia de demo en release.
