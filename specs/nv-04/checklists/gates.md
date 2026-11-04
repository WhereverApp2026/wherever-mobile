# Checklist — nv-04 Arquitectura de la aplicación móvil

> Trazabilidad: `spec R1-R8 → plan §3-§5 → task T1-T6`. Estado: Borrador con evidencia; aprobación pendiente.

- [x] Los 11 módulos y sus paquetes `feature/*` están documentados (R1)
- [x] Reglas de dependencia y plantilla MVVM por feature documentadas (R1, R2)
- [x] `UiState`/`StateFlow`/eventos y límites ViewModel-Composable definidos (R2)
- [x] Grafos de navegación, auth gate y deep links definidos; rutas reales diferidas a `nv-05+` (R3)
- [x] Room fuente de verdad, propiedad de tablas, caché, paginación y sync definidos (R4)
- [x] Migraciones explícitas y prohibición de `fallbackToDestructiveMigration` (R4)
- [x] Convenciones REST y 11 grupos propuestos, pendientes del backend Golang (R5)
- [x] Auth con Keystore, refresh único, logout/reset y permisos en contexto (R6)
- [x] FCM, Maps, cámara y medios como interfaces diferidas `[planificado nv-06]` (R6)
- [x] Design system, `strings.xml`, estados, accesibilidad y observabilidad (R7)
- [x] Matriz de pruebas R8 y cobertura ≥45 % definidas para `nv-05+` (R8)
- [x] `docs/architecture.md` creado y enlazado desde la spec
- [x] `docs/figma-inventory.md` cubre 158 frames, 11 módulos y solapamientos
- [x] `AGENTS.md`, `README.md`, `CLAUDE.md` y `Memoria.md` actualizados
- [x] `docs/routes.md`, `docs/permissions-matrix.md` y `docs/masvs-checklist.md` alineados
- [x] Sin referencias a `nv-04` como pantalla o implementación
- [x] `git diff --check` sin errores
- [x] Revisión manual SDD realizada; `scripts/check_sdd.sh` queda para `nv-01` `[planificado nv-01]`
- [ ] Aprobación firmada: (pendiente de revisor)
