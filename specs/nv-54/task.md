# NV-54 — Reservas

## T1 — Implementación
- [x] `ReservationsRepository` con los siete endpoints.
- [x] Reservar, Reserva (código, calendario, compartir, cambiar hora, cancelar) y Mis reservas.
- [x] Botón Reservar en la ficha y fila en Perfil; rutas en `docs/routes.md`; versión 9 (0.7.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] En el teléfono contra staging con un negocio que acepte reservas.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `ReservationsViewModelsTest` (12)
- [x] Integración: `ReservationsIntegrationTest` (5)
- [x] UI: sin pruebas nuevas
- [x] Seguridad: `SourceSecurityTest`, `ArchitectureTest`
- **No hacer**: generar una llave de idempotencia por toque, pedir datos de tarjeta o pedir permiso de calendario.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
