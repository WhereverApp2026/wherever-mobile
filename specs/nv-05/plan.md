# plan.md - NV-05 Acceso, registro y onboarding

## 1. Mapeo
- Creados: `specs/nv-05/spec.md`, `plan.md`, `task.md` y `checklists/gates.md`.
- Implementado: `feature/auth/{ui,domain,data}` con DTOs, `AuthRepository`, `OnboardingRepository`, `RemoteTokenRefresher`, parsers, `DeviceInfoProvider`, ViewModels, pantallas Compose, rutas tipadas y tests.
- Implementado onboarding R5-R8: entidad/DAO `onboarding_state` (DB v2 + migración `1→2`), preferencias, racional de ubicación, sugerencias/solicitudes y permisos opcionales.
- Actualizados: `docs/routes.md`, `docs/architecture.md`, `docs/permissions-matrix.md`, `Memoria.md` y `strings.xml`.
- No se implementan pantallas de `nv-06+`, FCM, Maps ni cámara.

## 2. Guardarraíles
- La constitución manda: offline-first, Keystore, solo HTTPS, sin secretos en logs, `Result`/`AppError`/`PageResult` y permisos en contexto.
- `nv-05` abre la etapa de producto; no se adelantan feed, planes, reservas ni chat.
- Google, contactos, Facebook y `CAMERA` no se implementan ni se simulan: se ocultan o se difieren `[planificado nv-06]`.
- Rutas reales del backend verificadas; respuestas exitosas no documentadas y aisladas en DTOs/parser tolerante.
- Sin `feature → feature`, sin Retrofit/Room en UI/ViewModel, sin `Dispatchers.IO` fijo, sin `fallbackToDestructiveMigration`.
- El código de reset se captura manualmente, se redacta de logs y no se persiste ni viaja por ruta.
- Toda referencia a un artefacto no creado conserva `[planificado nv-0X]`.

## 3. Pruebas
- Medición 2026-11-04: 75 tests JVM verdes y 17 instrumentados verdes; 0 fallos.
- Unit/Data/Security JVM: auth 22, onboarding 7, red 22, common/UI/demo/health/config 19.
- Arquitectura: 5 tests Konsist; instrumentados: DB 5, seguridad 7, UI 5.
- Data: `MockWebServer` para auth y onboarding; no se usan repositorios simulados en pruebas de datos.
- Pendiente: `MigrationTestHelper` para `1→2`, cobertura ≥45 % y `koverVerify` `[planificado nv-03]`.

## 4. Secuencia
1. Spec `nv-05` con R1-R11 y los 11 frames.
2. Plan, tareas y gates con trazabilidad.
3. Rutas reales y contratos verificados en `docs/routes.md`.
4. `TokenStore`/sesión y refresh real.
5. Repositorios, ViewModels y `UiState` por pantalla.
6. Pantallas Compose, textos y navegación auth.
7. Tests Unit/Data/Security/UI/Arquitectura y gates.
8. Onboarding R5-R8, `Memoria.md` final, `CHECKLIST_PR.md` y tag `vX.Y.Z-featN` al cerrar.
9. Reconciliación de `spec`, `plan`, `task`, `gates`, `Memoria.md` y `README.md` con la implementación.

## 5. Verificación
- Revisión manual: R1-R11 en `spec.md`, `plan.md`, `task.md` y `gates.md`; R5-R8 implementados.
- Revisión manual: frames auth y onboarding implementados; Google, contactos y Facebook fuera de alcance; cámara sin permiso.
- Revisión manual: 14 rutas auth en `docs/routes.md`; `permissions-matrix` actualizado y MASVS sin cambios de superficie.
- `git diff --check` sin errores.
- Evidencia ejecutada: `assembleDebug`, `testDebugUnitTest`, `connectedDebugAndroidTest`, ktlint, detekt, lint y scripts `check_strings`, `check_routes`, `check_sdd` y `critic-guardrails`.
- `MigrationTestHelper` de `1→2`, cobertura ≥45 %, enmienda constitucional `1.2.0` y aprobación de revisor pendientes.
