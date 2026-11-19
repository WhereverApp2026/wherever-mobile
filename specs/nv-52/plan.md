# plan.md — NV-52 — Avisos y horario silencioso

> Deriva de `specs/nv-52/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/notifications/domain/Notifications.kt`
- `feature/notifications/data/{NotificationsApi,NotificationsRepositoryImpl}.kt` y `data/di/NotificationsModule.kt`
- `feature/notifications/ui/{NotificationsScreen,NotificationsViewModel,UnreadBadgeViewModel}.kt`
- `feature/home/ui/HomeScreen.kt` (campana), `feature/settings/ui/Settings{Screen,ViewModel}.kt` (horario)
- `feature/profile/{domain/UserProfile,data/ProfileDtos,data/ProfileMappers}.kt` (`quietHours`)
- `navigation/{Routes,PushNavigation,WhereverNavHost}.kt`, `docs/routes.md`, `strings.xml`
- `app/build.gradle.kts`: versión 7 (0.5.0)

## 2. Guardarraíles
- La campana vive en su propio ViewModel para no sumar dependencias a `HomeViewModel`.
- La cadena de interceptores no cambia.

## 3. Pruebas
### Unitarias
- `NotificationsViewModelTest` (11)

### Integración
- `NotificationsIntegrationTest` (5)

### UI
- Sin pruebas nuevas

### Seguridad
- `SourceSecurityTest`, `ArchitectureTest`

## 4. Secuencia
1. Contrato del backend.
2. Datos, ViewModels, pantalla, campana y horario; pruebas.
3. Compuertas por partes; push a `main`.

## 5. Riesgos
- Staging aún no genera avisos (no hay amigos ni grupos en uso): la lista se verá vacía hasta que existan.
