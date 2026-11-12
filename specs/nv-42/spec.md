# NV-42 — Fecha de nacimiento sin fechas futuras

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Corrige `specs/nv-09` (Registro, US-012). Rama `fix/fecha-nacimiento`.

## 1. Objetivo

En Registro, el calendario de fecha de nacimiento dejaba escoger días del futuro (por ejemplo, el mes que viene) y abría en hoy. Con esta corrección solo se puede escoger una fecha entre hoy hace 100 años y hoy, y el código vuelve a validarla por si llega por otro camino.

## 2. Alcance / No-alcance

### Alcance
- `DatePicker` de `RegisterScreen` con `SelectableDates`: los días después de hoy salen deshabilitados, el año más viejo es el de hace 100 años y el calendario abre en el mes de hace 18 años.
- `validateBirthDate` (`feature/auth/domain/RegistrationValidator.kt`) con tres mensajes distintos, en `strings.xml`:

| Caso | Regla | Mensaje |
|---|---|---|
| Fecha futura | después de hoy | "La fecha no puede ser posterior a hoy" (`validation_birth_date_future`) |
| Demasiado vieja | antes de hoy hace 100 años (ese día sí se acepta) | "Revisa el año de nacimiento" (`validation_birth_date_too_old`) |
| Menor de edad | menos de 18 años cumplidos | "Debes ser mayor de 18 años" (`validation_adult`, ya existía) |

- "Hoy" sale del `Clock` inyectado (`AppModule.provideClock`, zona del teléfono), nunca de `LocalDate.now()` sin reloj.
- **Zona horaria:** el `DatePicker` representa cada día como la medianoche UTC de ese día. La app convierte el "hoy" local a esa medianoche UTC (`BirthDateBounds`) y no compara con la hora actual en UTC. Así, escoger hoy a las 23:30 en México (ya es mañana en UTC) sigue siendo hoy.
- 29 de febrero: se usa `Period` de `java.time`; quien nació el 29/02/2008 cumple 18 el 01/03/2026.
- 422 del backend en `fecha_nacimiento`: su `mensaje` se muestra bajo el campo. Ya lo hacía `RegisterViewModel.onFailure`; ahora también lo cubre una prueba de integración.

### No-alcance
- Edad máxima en el backend: la doc (`docs/contexto_api_sprint1.md`) solo pide 18+ y formato AAAA-MM-DD; el límite de 100 años es de la app.
- Fecha de nacimiento en Editar perfil: el perfil no la edita.

## 3. Contratos (API y navegación)

Sin cambios. `POST /api/v1/usuarios` sigue enviando `fecha_nacimiento` en AAAA-MM-DD. Sin rutas nuevas.

**Cotejo con staging (2026-11-12):** `POST /usuarios` con fecha futura responde 422 `validacion` con `campos[fecha_nacimiento]` = "debes tener 18 años o más (fecha AAAA-MM-DD)".

## 4. Modelo de datos

`RegisterUiState.maxBirthYear` se reemplaza por `today: LocalDate` (hoy según el reloj), que acota el calendario. Sin cambios en Room.

## 5. Seguridad y privacidad

- La fecha de nacimiento sigue fuera de los logs (`specs/nv-09` §5). La prueba nueva de integración no la registra.
- Sin permisos ni datos nuevos.

## 6. Tests / Gates

- **Unit**: `test/.../feature/auth/domain/BirthDateValidatorTest.kt`: hoy, mañana, el mes que viene, el año que viene, justo 18 años, un día antes de 18, 29 de febrero, hace 101 años, hace 100 años justos y la noche en México. `test/.../feature/auth/ui/BirthDateBoundsTest.kt`: días que se pueden tocar, rango de años, mes inicial y milisegundos UTC. `AuthViewModelsTest`: mensajes distintos, "hoy" en la zona del reloj y que no se envía una fecha futura.
- **Data**: `test/.../integration/AuthIntegrationTest.kt`: `POST /usuarios` responde 422 con `campos[fecha_nacimiento]` por MockWebServer con la cadena de red real; llega como `AppError.Validation` de ese campo y no guarda tokens.
- **Security**: sin superficie nueva; `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes.
- **UI**: `androidTest/.../feature/auth/ui/BirthDatePickerTest.kt` (Compose Testing): un día futuro sale deshabilitado y no se puede escoger; hoy y los días pasados sí.

## 7. Plan de entrega

PR "fix: fecha de nacimiento sin fechas futuras". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
