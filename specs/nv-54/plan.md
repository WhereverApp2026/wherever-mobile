# plan.md — NV-54 — Reservas

> Deriva de `specs/nv-54/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/reservations/domain/Reservations.kt`
- `feature/reservations/data/{ReservationsApi,ReservationsRepositoryImpl}.kt` y `data/di/ReservationsModule.kt`
- `feature/reservations/ui/{Reserve,Reservation,MyReservations}{Screen,ViewModel}.kt` y `ReservationViews.kt`
- `feature/places/ui/PlaceDetailScreen.kt` (botón Reservar), `feature/profile/ui/ProfileScreen.kt` (fila Mis reservas)
- `core/designsystem/Inputs.kt` (`SelectChip(enabled)`)
- `navigation/{Routes,WhereverNavHost}.kt`, `docs/routes.md`, `strings.xml`
- `app/build.gradle.kts`: versión 9 (0.7.0)

## 2. Guardarraíles
- Una llave de idempotencia por intento, nunca una por toque.
- La cadena de interceptores no cambia; `If-Match` como en el perfil.

## 3. Pruebas
### Unitarias
- `ReservationsViewModelsTest` (12)

### Integración
- `ReservationsIntegrationTest` (5)

### UI
- Sin pruebas nuevas

### Seguridad
- `SourceSecurityTest`, `ArchitectureTest`

## 4. Secuencia
1. Contrato del backend.
2. Datos, ViewModels, pantallas y cableado; pruebas.
3. Compuertas por partes; push a `main`.

## 5. Riesgos
- Staging no tiene negocios con reservas configuradas: la prueba de punta a punta espera a que se cargue la semilla.
