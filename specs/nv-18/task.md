# NV-18 — Notificaciones

> Implementada en nv-46 (push), nv-52 (Avisos) y 2026-11-19 (aviso en la app, bloqueo y Aceptar). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-18/spec.md` aprobada
- [x] 2. Sin Room: los avisos se leen de la API por cursor (decisión de nv-52; el contador de no leídos también)
- [x] 3. DTO de red + mapeos + `AppError` (`NotificationsApi`, `NotificationsRepositoryImpl`)
- [x] 4. Repositorio sin Dispatchers fijos
- [x] 5. ViewModel con `UiState` y `StateFlow` (`NotificationsViewModel`, `UnreadBadgeViewModel`, `MainViewModel.banner`)
- [x] 6. Pantallas: `18_Notif` (filtros, marcar leídas, borrar, grupos por día), `Notif · solicitud aceptada` (Aceptar desde la fila → "Ya es tu amigo"), `87_PushBloqueo` (notificación privada en el bloqueo, canal `general`), `88_PushBanner` (push con la app abierta → aviso con Ver y Más tarde, 8 s)
- [x] 7. Pruebas (T2)
- [x] 8. Gates: cobertura ≥45% + `routes.md` vivos
- [x] 9. En el teléfono (Xiaomi, 2026-11-19) con push real de staging: cuenta QA B manda solicitud a la cuenta del teléfono → aviso en la app → Ver abre el perfil; Aceptar en Avisos (`PATCH /amistades/{id}` y `/notificaciones/{id}`); solicitud ya respondida → aviso; app en segundo plano → notificación `vis=PRIVATE`. Capturas `docs/pruebas/nv18_*`.
- [x] 10. `Memoria.md` actualizado
- [ ] Pushes de reserva, reseña y "Es hora de salir" (frames 87 y 88): el backend solo manda los 5 tipos sociales (ver `docs/revision-jorge.md`).

## T2 — Pruebas
- [x] Unit: `NotificationsViewModelTest` — páginas, filtros, leídas, borrar, aceptar desde el aviso y solicitud que ya no está
- [x] Unit: `MainViewModelTest` — aviso en la app (Ver navega, se va solo) y en segundo plano no se ofrece
- [x] UI: `androidTest/.../navigation/InAppPushBannerTest.kt` (pasó en el teléfono)
- [x] Integración: `NotificationsIntegrationTest` (MockWebServer)
- [x] Seguridad: `PushDestination` valida los ids; el push no lleva datos sensibles; `google-services.json` fuera del repositorio

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
