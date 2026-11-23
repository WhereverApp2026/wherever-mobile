# plan.md — NV-56 — Planes sin conexión (Room)

> Deriva de `specs/nv-56/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/plans/data/`: `PlanEntity`, `PlanTabEntryEntity`, `PlanStopEntity`, `PlanTransferEntity`, `PlanDraftEntity`, `PlanDao`, `PlansCleaner` y mapeos entidad ↔ dominio; `PlansRepositoryImpl` y `PlanAssistantRepository` escriben en Room.
- `feature/plans/data/PlansApi.kt`: `offlinePackage(id, ifNoneMatch)` → `Response<OfflinePackageDto>` para leer `ETag` y 304.
- `feature/plans/data/di/PlansModule.kt`: `PlanDao` y `@IntoSet` del `SessionCleaner`.
- `feature/plans/domain/Plans.kt`: `observeTab`, `observe`, `refreshTab`, `refresh`, borrador guardado.
- `feature/plans/ui/{MyPlans,PlanDetail,CreatePlan}ViewModel.kt` y sus pantallas: leen `Flow`; aviso sin red desde `ConnectivityMonitor` (solo en `ui`); las acciones se intentan siempre.
- `core/database/WhereverDatabase.kt` (versión 6, entidades nuevas), `Migrations.kt` (`MIGRATION_5_6`), `app/schemas/com.wherever.mobile.core.database.WhereverDatabase/6.json`.
- **Por Issue a Jorge** (`CONTRIBUTING.md`, "Lo que no se toca"): `core/di/AppModule.kt` (`.addMigrations(MIGRATION_5_6)`, una por línea, sin spread) y `app/build.gradle.kts` (versión).
- `feature/status/ui/AppStatusScreens.kt` y `MainActivity.kt`: "Ver mis planes guardados" cierra la capa (`dismiss`) y navega a `MyPlansRoute`; se avisa a Jorge.
- `strings.xml`: avisos de sin conexión y última actualización.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red sin tocar, textos en `strings.xml`, sin secretos.
- Sin `fallbackToDestructiveMigration`; `MIGRATION_5_6` solo crea tablas.
- Despachadores inyectados; reloj inyectado para la ventana de 30 minutos y "hoy o mañana".
- El monitor de red solo informa a la UI; no frena peticiones ni apaga botones (`ConnectionSecurityTest`, nv-40).
- La lista por pestaña no reemplaza paradas de un plan con `detailLoaded`.
- Sin lógica de negocio en Composables.

## 3. Pruebas

### Unitarias
- ViewModels: Room primero, luego actualización; sin red; error que conserva datos.
- Mapeos entidad ↔ dominio.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- JVM: `refreshTab`, ediciones, 404, paquete 200/304/422 y ventana de 30 minutos, borrador.
- Instrumentada: `PlanDao` y cascada; `MigrationTest` 5 → 6.

### Seguridad (`docs/masvs-checklist.md`)
- Cerrar sesión vacía las cuatro tablas; sin cuerpos de planes en logs.

### Otras
- UI opcional: Mis planes sin red.
- Arquitectura: `ArchitectureTest` con las entidades nuevas.

## 4. Secuencia
0. Issue a Jorge por `AppModule.kt`, `app/build.gradle.kts` y `MainActivity.kt`.
1. Entidades, DAO, `MIGRATION_5_6`, esquema 6 y `MigrationTest`.
2. Mapeos y `PlansCleaner`.
3. Repositorio: lectura por `Flow`, actualización y guardado tras editar.
4. Paquete sin conexión con `ETag`.
5. Borrador del asistente.
6. ViewModels y pantallas: aviso sin red y errores que conservan los datos.
7. `89_SinConexion`: cerrar la capa y abrir Mis planes.
8. Pruebas, compuertas, teléfono y PR.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer, seguridad y arquitectura)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real y `MigrationTest`; teléfono o emulador)

## 6. Riesgos
- Otra rama puede subir Room a la versión 6 antes: se renumera a `MIGRATION_6_7` al integrar `main`.
- Staging sin negocios: puede no haber planes reales para probar en el teléfono.
- Si Jorge no puede tocar `AppModule.kt` a tiempo, la rama queda bloqueada en el paso 1: sin registrar la migración, la app se cae al abrir con la base 5.
