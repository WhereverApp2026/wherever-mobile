# NV-16 — Sin conexión

> Sin ejecutar. Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [ ] 1. Spec en `specs/nv-16/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración
- [ ] 3. DTO de red + mapeos + `AppError`
- [ ] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [ ] 5. ViewModel con `UiState` y `StateFlow`
- [ ] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [ ] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador + tag `vX.Y.Z-featN`
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [ ] Unit: Unit: reintento con monitor de red falso inyectado.
- [ ] Integración: Data: sin red, la pantalla lee de Room lo ya guardado y `GET /health` reintenta con el monitor inyectado.
- [ ] Seguridad: Security: el QR guardado vive en almacenamiento privado y se borra al cerrar sesión (verificación sobre el archivo en disco).

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
## Estado real (2026-11-20)
- [x] `89_SinConexion` como capa sobre la app (`feature/status/ui/AppStatusScreens.kt`, `AppStatusOverlay` en `MainActivity`), no como ruta: `ConnectivityMonitor` (`ConnectivityManager`, `ACCESS_NETWORK_STATE`) con espera de 1,5 s para no avisar en cambios wifi↔datos.
- [x] Reintentar siempre intenta `GET /configuracion` (nv-40: ningún chequeo de red frena una petición): falla de red → "Sigues sin conexión"; servidor caído → aviso propio; éxito → se cierra. Atrás cierra el aviso hasta la siguiente pérdida de red.
- [x] `ConnectivityMonitor` (interfaz en `core/common`) solo informa a la UI; `AndroidConnectivityMonitor` es el único archivo con `ConnectivityManager` (`ConnectionSecurityTest` lo fija y prohíbe usarlo en `core/network` y `feature/*/data`).
- [x] Mantenimiento (`mantenimiento: true`) y versión mínima (`version_minima_app`, pantalla Actualizar → Play Store) con `GET /configuracion` (pública).
- [x] Unit: `test/.../feature/status/ui/AppStatusViewModelTest.kt` (monitor falso inyectado) y `feature/status/domain/VersionTest.kt`. Data: `integration/StatusIntegrationTest.kt` (MockWebServer). Seguridad: `PlatformSecurityTest` fija el permiso nuevo.
- [x] Capturas: `89_sin_conexion`, `89_sin_conexion_aviso`, `mantenimiento`, `actualizar` × `{claro,oscuro,chica_letra_grande}`.
- [x] Teléfono (2207117BPG, 2026-11-20): modo avión → Sin conexión → Reintentar avisa → Atrás vuelve a Inicio.
- Diferido: "Disponible sin internet" necesita planes y reservas en Room (otra área); sin datos guardados la sección se oculta. QR guardado: llega con Reservas (nv-27).
