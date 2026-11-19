# NV-54 — Reservas: reservar desde la ficha, confirmación con código, Mis reservas, cambiar hora y cancelar

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Implementa el lado del consumidor de EP-08 con la API ya publicada (backend nv-59, J5). Rama `feat/reservas`, desde `main`.

## 1. Objetivo

Que la persona reserve en un lugar que acepta reservas, vea su código, la agregue a su calendario, la comparta, cambie la hora o la cancele sabiendo cuánto le devuelven (US-075, US-077, US-082, US-088; US-076 parcial).

## 2. Alcance / No-alcance

### Alcance
- **Ficha del lugar:** botón "Reservar" si `acepta_reservas`.
- **Reservar (55_Reservar):** próximas dos semanas (Hoy, Mañana, luego "jue 21 nov"), personas de 1 a 20, turnos con cupo (los llenos se ven sin poder elegirse), nota de hasta 140 caracteres y resumen antes de confirmar. `Idempotency-Key` por intento: se repite al reintentar tras una falla de red y cambia si cambia el turno. 409 = "Ese horario se acaba de llenar" y se recargan los turnos; 403 = verificar el correo.
- **Reserva (56_ReservaConfirmada):** estado, código de 6 caracteres en grande, fecha, personas, nota, anticipo, política de cancelación y mensajes del lugar. Agregar al calendario (`ACTION_INSERT`, sin permiso), compartir, cambiar hora (turnos libres del mismo día; `PATCH` con `If-Match`; 409 vuelve a leer) y cancelar (primero `GET /reembolso` y el monto exacto en el diálogo, luego `POST /cancelar` con motivo opcional).
- **Apartada con anticipo:** aviso de que el pago desde la app llega pronto y hasta qué hora se aparta.
- **Perfil → Mis reservas:** próximas y pasadas con cursor; el nombre del lugar se lee de su ficha.
- Versión `versionCode 9`, `versionName 0.7.0`.

### No-alcance
- Pago del anticipo (US-083 a US-086): necesita el SDK de Stripe (PaymentSheet), fuera del stack hasta acordar la excepción.
- QR de la reserva (parte de US-076) y verla sin señal (US-089): sin librería de QR; el código se muestra en texto.
- Reservar varias paradas de un plan (US-198) y pagar por partes en grupo (US-199).

## 3. Contratos (API y navegación)

wherever-backend `docs/endpoints.md` (`origin/main`, nv-59):

| Método | Ruta | Uso |
|---|---|---|
| GET | `/lugares/{id}/disponibilidad?fecha=&personas=` | Turnos |
| POST | `/reservas` + `Idempotency-Key` | Crear; 409 sin cupo, 403 correo sin verificar, 422 no acepta |
| GET | `/reservas?cuando=proximas\|pasadas&cursor=&limite=20` | Mis reservas |
| GET | `/reservas/{id}` | Detalle con mensajes |
| GET | `/reservas/{id}/reembolso` | Monto antes de cancelar |
| POST | `/reservas/{id}/cancelar {motivo?}` | Cancelar |
| PATCH | `/reservas/{id}` + `If-Match` `{fecha_hora}` | Cambiar hora |

Rutas nuevas en `docs/routes.md`: `ReserveRoute(placeId, placeName)`, `ReservationRoute(id)` y `MyReservationsRoute`. Al confirmar, la reserva reemplaza a Reservar en la pila.

## 4. Modelo de datos

Sin cambios en Room. `feature/reservations` (`ReservationsRepository`). `UiState`: `ReserveUiState`, `ReservationUiState` y `MyReservationsUiState`. `SelectChip` del design system gana `enabled`.

## 5. Seguridad y privacidad

- La app nunca pide ni manda datos de tarjeta; el anticipo queda fuera.
- El calendario se abre con un `Intent` y la persona confirma; no se pide `WRITE_CALENDAR`.
- Sin permisos nuevos y sin cambios en la cadena de red.

## 6. Tests / Gates

- **Unit:** `test/.../feature/reservations/ui/ReservationsViewModelsTest.kt` (12): días y personas, recargar turnos, turno lleno, confirmar con nota recortada, llave repetida y nueva, 409 y 403, nombre del lugar, reembolso antes de cancelar, cambiar hora con versión, conflicto que relee, Mis reservas y pesos.
- **Data:** `test/.../integration/ReservationsIntegrationTest.kt` (5) con la cadena real: query de disponibilidad, `Idempotency-Key` y cuerpo, 409/403, lista con cursor, reembolso, cancelar con motivo y `If-Match`.
- **UI:** sin pruebas instrumentadas nuevas.
- **Security:** `SourceSecurityTest` y `ArchitectureTest` siguen verdes.

## 7. Plan de entrega

Commit directo a `main` a pedido de Jorge (2026-11-19). Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
