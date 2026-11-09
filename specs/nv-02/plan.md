# plan.md — NV-02 Molde de funcionalidad: Repository → ViewModel → UiState

1. Mapeo
- Crear: `core/common/{Result,PageResult}.kt`, `core/data/BaseRepository.kt`, `core/ui/{UiState,BaseViewModel}.kt`, `app/src/test/.../arch/ArchitectureTest.kt`, `docs/feature-mold.md`.

2. Guardarraíles
- Dispatchers inyectados.
- Sin lógica de negocio en Composables.
- Textos en `strings.xml`.

3. Pruebas
- Los listados de §Tests.

4. Secuencia
1. Tipos comunes.
2. Repositorio base.
3. UiState y ViewModel base.
4. Prueba de arquitectura.
5. Documentar el molde.

5. Verificación
- `./gradlew testDebugUnitTest --tests '*ArchitectureTest' → 5/5`
- `./gradlew testDebugUnitTest → verdes`
