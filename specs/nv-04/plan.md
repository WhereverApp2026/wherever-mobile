# plan.md — NV-04 Arquitectura de la aplicación móvil

1. Mapeo
- Crear: `specs/nv-04/spec.md`, `specs/nv-04/plan.md`, `specs/nv-04/task.md`, `specs/nv-04/checklists/gates.md`, `docs/architecture.md` y `docs/figma-inventory.md`.
- Actualizar: `AGENTS.md`, `README.md`, `CLAUDE.md`, `Memoria.md`, `specs/nv-01/spec.md`, `specs/nv-01/task.md`, `specs/nv-01/checklists/gates.md`, `docs/routes.md`, `docs/permissions-matrix.md`, `docs/masvs-checklist.md`, `.claude/agents/*`, `.claude/skills/*` y `.specify/templates/tasks-template.md` donde se refieran a `nv-04` como implementación.
- No crear: Gradle, código Kotlin, tests, CI, scripts, pantallas ni endpoints.

2. Guardarraíles
- La constitución manda: offline-first, Keystore, solo HTTPS, sin secretos en logs, `Result`/`AppError`/`PageResult`.
- No duplicar documentos vivos: `docs/architecture.md` resume; `specs/nv-04/spec.md` decide.
- Toda referencia a implementación futura se marca `[planificado nv-05]`; toda referencia a herramientas no creadas conserva `[planificado nv-0X]`.
- Sin nuevos endpoints definitivos ni cambios de contrato con la API; los grupos son propuestos.
- Sin pantallas de producto en `nv-04`; la maqueta se valida en las specs de `nv-05+`.

3. Pruebas
- En `nv-04` no hay pruebas ejecutables; la verificación es documental.
- La matriz de pruebas de R8 se implementa desde `nv-05` `[planificado nv-05]`: Arquitectura 8, Unit, Data, Security, UI, Contratos 11 y cobertura ≥45 %.
- Revisión manual: 8 secciones canónicas, R1-R8 en plan/task/gates, 11 módulos cubiertos y referencias alineadas.

4. Secuencia
1. Spec `nv-04` con R1-R8 y mapa de módulos.
2. Plan, tareas y gates con trazabilidad.
3. Documento vivo `docs/architecture.md`.
4. Renumeración de referencias `nv-04` → `nv-05` para pantallas e implementación.
5. Actualización de `AGENTS.md`, `README.md`, `CLAUDE.md` y `Memoria.md`.
6. Alineación de `docs/routes.md`, `docs/permissions-matrix.md` y `docs/masvs-checklist.md`.
7. Verificación de secciones, referencias y `git diff --check`.

5. Verificación
- `git diff --check → sin errores`
- Revisión manual: `specs/nv-04/spec.md` contiene las 8 secciones y R1-R8.
- Revisión manual: `plan.md`, `task.md` y `checklists/gates.md` cubren R1-R8.
- Búsqueda de referencias: no queda `nv-04` asociado a pantalla, refresh real, FCM, Maps, permisos ni features.
- `docs/architecture.md` cubre 11 módulos y enlaza a `specs/nv-04/spec.md`.
- `docs/figma-inventory.md` asigna los 158 frames a 11 módulos y documenta los solapamientos.
- `scripts/check_sdd.sh` `[planificado nv-01]` se ejecutará cuando exista; hoy no aplica.
- Sin código, tests ni CI nuevos.
