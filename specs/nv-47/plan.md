# plan.md — NV-47 — Inicio con recomendaciones, Explorar y ficha del lugar

> Deriva de `specs/nv-47/spec.md`. Respeta constitución §4 (capas y cadena de red), §5 (diseño solo desde `core/designsystem`) y §7 (pruebas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/location/`: `GeoPoint`, `LocationProvider` y `LocationPermissionPreferences` (interfaces), `AndroidLocationProvider` y `SharedLocationPermissionPreferences`; `core/di/LocationModule.kt`
- `feature/home/domain/HomeFeed.kt`, `feature/home/data/{HomeApi,HomeRepositoryImpl}.kt`, `data/di/HomeModule.kt`
- `feature/home/ui/{HomeScreen,HomeViewModel}.kt`: Inicio real
- `feature/places/domain/Places.kt`: modelos y `PlacesRepository`, `ReviewsRepository`, `FavoritesRepository`
- `feature/places/data/{PlacesApi,PlacesDtos,PlacesRepositoryImpl}.kt` (con `FavoritesApi`, `ReviewsRepositoryImpl`, `FavoritesRepositoryImpl`), `data/di/PlacesModule.kt`
- `feature/places/ui/`: `PlaceCardView.kt` (tarjeta y etiquetas), `Explore*`, `PlaceDetail*`, `PlaceSections.kt`, `Favorites*`
- `core/designsystem/Surfaces.kt`: `MainTab.EXPLORE` y `WhereverBottomBar(selected, onSelect)`
- `feature/profile/ui/ProfileScreen.kt`: fila "Mis favoritos"
- `navigation/{Routes,WhereverNavHost}.kt`: rutas nuevas y `openTab`
- `AndroidManifest.xml`, `docs/permissions-matrix.md`, `docs/routes.md`, `strings.xml`, `gradle/libs.versions.toml`, `app/build.gradle.kts`

## 2. Guardarraíles
- Sin lógica de negocio en Composables: filtros, permisos y descartes viven en los ViewModels.
- La UI no importa `data`, red ni Room (`ArchitectureTest`).
- Ubicación solo aproximada y en contexto; la app funciona sin ella.
- Sin URLs fijas: "Cómo llegar" usa `geo:` y el teléfono elige la app de mapas.

## 3. Pruebas
### Unitarias
- `HomeViewModelTest` (8), `PlacesViewModelsTest` (14)

### Integración
- `PlacesIntegrationTest` (12) con MockWebServer

### UI
- `PlaceCardTest` (1), en la CI del PR

### Seguridad
- `PlatformSecurityTest` y `SourceSecurityTest` actualizados

## 4. Secuencia
1. Dominio, DTO, API y repositorios de lugares e Inicio.
2. Ubicación en `core/location`.
3. Pantallas: tarjeta, Inicio, Explorar, ficha y favoritos.
4. Navegación y barra inferior.
5. Pruebas y compuertas.

## 5. Riesgos
- Staging sin negocios: no se pudo ver una ficha real. Las pruebas de datos usan los contratos de `docs/endpoints.md`.
- `coil-network-okhttp` 3.3.0 se declaró con OkHttp 4; Gradle resuelve OkHttp 5.4.0 del proyecto. Probar las fotos en el teléfono.
