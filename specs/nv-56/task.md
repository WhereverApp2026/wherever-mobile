# NV-56 — Planes sin conexión (Room)

> Sin ejecutar. Fase 1 de `docs/itinerario-planes.md`. Plantilla: `.specify/templates/tasks-template.md`.

## T0 — Antes de la rama
- [ ] Spec en `specs/nv-56/spec.md` aprobada por Osmar
- [ ] Issue a Jorge: `.addMigrations(MIGRATION_5_6)` en `core/di/AppModule.kt`, versión en `app/build.gradle.kts` y botón de `89_SinConexion` en `MainActivity.kt`

## T1 — Slice liberable
- [ ] 1. Tablas `plan`, `plan_tab_entry`, `plan_stop`, `plan_transfer`, `plan_draft` + `PlanDao` + esquema 6 exportado + `MIGRATION_5_6`
- [ ] 2. `offlinePackage` en `PlansApi` con `ETag`/304 + mapeos + `AppError`
- [ ] 3. Repositorio con Room como fuente de verdad, sin Dispatchers fijos; la lista no borra paradas de un plan abierto; `PlansCleaner` registrado
- [ ] 4. ViewModels leen `Flow`; aviso sin red; las escrituras se intentan y sus errores conservan los datos
- [ ] 5. `89_SinConexion` cierra la capa y abre Mis planes; textos en `strings.xml`
- [ ] 6. Pruebas de los tres tipos (T2)
- [ ] 7. Gates: cobertura ≥45% + `routes.md` (sin rutas nuevas) + `permissions-matrix.md` (sin permisos nuevos) + `masvs-checklist.md` (almacenamiento local)
- [ ] 8. APK + verificación en teléfono en modo avión
- [ ] 9. Actualizar `Memoria.md` + `CHECKLIST_PR.md`; marcar en `nv-23`, `nv-24` y `nv-28` lo que cierra esta spec
- **No hacer**: `fallbackToDestructiveMigration`, encolar ediciones sin red, apagar botones o frenar peticiones con el monitor de red, guardar la frase o el ánimo.

## T2 — Pruebas
- [ ] Unit: ViewModels con Room primero, errores que conservan datos y aviso sin red; mapeos.
- [ ] Integración: Data (JVM): `refreshTab` sin borrar paradas, ediciones, `DELETE` y 404, paquete 200/304/422 y ventana de 30 minutos, borrador.
- [ ] Integración: Data (instrumentada): `PlanDao` en orden, un plan en dos pestañas, cascada, limpieza de viejos y `MigrationTest` 5 → 6.
- [ ] Seguridad: Security: `clearLocalSession()` vacía las cinco tablas; el borrador no guarda frase ni ánimo; sin cuerpos de planes en logs.

## T3 — Aprobación
- [ ] Revisor: Osmar — fecha:
