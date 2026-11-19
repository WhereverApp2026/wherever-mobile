# NV-49 — Autenticación en el teléfono

## T1 — Implementación
- [x] `FormScreen` con pie dentro del scroll en pantallas bajas.
- [x] Enviar cierra el teclado (Login, Registro, Recuperar); Siguiente/Listo en Login.
- [x] Correo sin espacios.
- [x] `RetryingDns` en todos los clientes.
- [x] Versión 2 (0.1.1).

## T2 — Verificación
- [x] Reproducción en el teléfono con logcat (antes) y confirmación tras reinstalar (después).
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] `FormScreenCompactTest` en la CI del PR.
- [ ] Correo de recuperación real: pendiente del equipo de API (SMTP en staging).

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `RetryingDnsTest` (3), `ValidatorsTest` (+1)
- [x] Integración: sin superficie nueva
- [x] UI: `FormScreenCompactTest` (2)
- [x] Seguridad: sin superficie nueva
- **No hacer**: reintentar peticiones que ya salieron, bloquear la rotación o escribir cuentas de prueba en archivos.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
