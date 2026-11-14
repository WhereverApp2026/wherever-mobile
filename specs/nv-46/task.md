# NV-46 — Notificaciones push con Firebase

## T1 — Implementación
- [x] Firebase: plugin 4.6.0, BoM 35.2.0, `firebase-messaging`; `google-services.json` en `app/` fuera de Git.
- [x] `POST_NOTIFICATIONS` pedido al llegar a Inicio en Android 13+.
- [x] `PUT /dispositivos` tras login y registro, al arrancar con sesión y en `onNewToken`.
- [x] `DELETE /dispositivos` antes de logout y logout-todos.
- [x] Tocar un push lee `data.tipo` y los ids; hoy todos abren Inicio hasta que existan los destinos.
- [x] Textos en `strings.xml`.

## T2 — Verificación
- [ ] Compuertas locales con JDK 17 (este cambio se escribió sin SDK de Android: compilarlo es lo primero).
- [ ] CI del PR en verde.
- [ ] Teléfono con Android 13+: permiso, `PUT` al iniciar sesión, push de prueba con la app cerrada y abierta, tocarlo y `DELETE` al cerrar sesión.

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `PushDestinationTest` (4), `AuthRepositoryImplTest` (3 nuevas), `MainViewModelTest` (5 nuevas)
- [x] Integración: `DevicesIntegrationTest` (6)
- [x] Seguridad: token fuera de logs, ids validados, permisos y exportados
- **No hacer**: poner el token en la URL, subir `google-services.json` o pedir el permiso al abrir la app por primera vez sin contexto.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
