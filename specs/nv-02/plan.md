# plan.md — NV-02 Molde de funcionalidad: Repository → ViewModel → UiState

1. Mapeo
- Crear: `core/common/{Result,PageResult}.kt`, `core/data/BaseRepository.kt`, `core/ui/{UiState,BaseViewModel}.kt`, `app/src/test/.../arch/ArchitectureTest.kt`, `app/src/test/.../demo/{DemoRepository,DemoViewModel}.kt`, `docs/feature-mold.md`.
- Actualizar: `AGENTS.md` con `core/common`, `core/data` y `core/ui` en la estructura.

2. Guardarraíles
- Dispatchers inyectados.
- Sin lógica de negocio en Composables.
- Textos en `strings.xml`.
- `DemoRepository`/`DemoViewModel` solo en `app/src/test`; prohibido en `main`.

3. Pruebas
- Los listados de §Tests: arquitectura 5, Unit 12, demo solo-test.

4. Secuencia
1. Tipos comunes y `PageResult` con límite 50.
2. Repositorio base.
3. UiState y ViewModel base.
4. Prueba de arquitectura.
5. Demo solo en pruebas.
6. Documentar el molde en `docs/feature-mold.md`.

5. Verificación
- `./gradlew testDebugUnitTest --tests '*ArchitectureTest' → 5/5`
- `./gradlew testDebugUnitTest → verdes`
- `./gradlew assembleRelease` sin `DemoRepository`/`DemoViewModel`.
