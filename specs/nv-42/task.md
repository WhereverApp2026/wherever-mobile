# NV-42 — Fecha de nacimiento sin fechas futuras

## T1 — Implementación
- [x] `validateBirthDate` con fecha futura, demasiado vieja (más de 100 años) y menor de 18, contra el `Clock` inyectado.
- [x] `BirthDatePicker` con `SelectableDates`: solo hasta hoy, año más viejo hace 100 años, abre en hace 18 años.
- [x] "Hoy" local convertido a medianoche UTC para el calendario (sin saltar a mañana de noche en México).
- [x] Mensajes en `strings.xml`.
- [x] 422 en `fecha_nacimiento`: el mensaje del backend se muestra en el campo (ya existía; ahora con prueba de integración).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (2026-11-12): `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (190), `koverVerifyDebug` (73,32 %), `assembleDebug`, `assembleRelease`; `assembleDebugAndroidTest` compila y los scripts de puerta pasan.
- [ ] CI del PR en verde (incluye la prueba de UI en emulador).
- [ ] En el celular: los días futuros salen grises y no se pueden tocar; el calendario abre en hace 18 años.
- [ ] Contra staging (L-V 08:00-22:00): `POST /usuarios` con fecha futura responde 422 en `fecha_nacimiento`; si no, anotar el `traza_id` en `spec.md` §3.

## T3 — Pruebas (unitarias, integración, seguridad y UI)
- [x] Unit: `BirthDateValidatorTest`, `BirthDateBoundsTest`, `AuthViewModelsTest`
- [x] Integración: `AuthIntegrationTest` (422 en `fecha_nacimiento`)
- [x] Seguridad: sin superficie nueva
- [x] UI: `BirthDatePickerTest`
- **No hacer**: usar `LocalDate.now()` sin reloj ni comparar los milisegundos del calendario con la hora UTC actual.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
