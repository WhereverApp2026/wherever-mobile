# plan.md — NV-53 — Amigos

> Deriva de `specs/nv-53/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/friends/domain/Friends.kt`
- `feature/friends/data/{FriendsApi,FriendsRepositoryImpl}.kt` y `data/di/FriendsModule.kt`
- `feature/friends/ui/{Friends,AddFriend,Person}{Screen,ViewModel}.kt` y `FriendsViews.kt`
- `feature/profile/{domain,data}`: `username`, `friendCode`, `searchableByUser`, `updateUsername`
- `core/database/{Migrations,WhereverDatabase}.kt`, `core/di/AppModule.kt`, `app/schemas/.../4.json`
- `feature/profile/ui/ProfileScreen.kt` (fila Amigos), `feature/settings/ui/PrivacyScreen.kt`
- `navigation/{Routes,PushNavigation,WhereverNavHost}.kt`, `docs/routes.md`, `strings.xml`
- `app/build.gradle.kts`: versión 8 (0.6.0)

## 2. Guardarraíles
- Migración explícita, sin `fallbackToDestructiveMigration`; `@ColumnInfo(defaultValue = "1")` igual al `DEFAULT` del `ALTER`.
- La cadena de interceptores no cambia.

## 3. Pruebas
### Unitarias
- `FriendsViewModelsTest` (11)

### Integración
- `FriendsIntegrationTest` (5)

### UI
- `MigrationTest` (+1)

### Seguridad
- `SourceSecurityTest`, `ArchitectureTest`

## 4. Secuencia
1. Contrato del backend.
2. Perfil y migración; datos, ViewModels y pantallas; pruebas.
3. Compuertas por partes; push a `main`.

## 5. Riesgos
- Staging tiene pocas cuentas: para probar de punta a punta hacen falta dos cuentas de prueba.
