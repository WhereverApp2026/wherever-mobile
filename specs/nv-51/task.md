# NV-51 — Cuenta: contraseña, correo y mis datos

## T1 — Implementación
- [x] `AccountRepository` con los cuatro endpoints.
- [x] Cambiar contraseña, cambiar correo y Mis datos; filas en Ajustes y Privacidad.
- [x] Rutas en `docs/routes.md`; versión 6 (0.4.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] En el teléfono contra staging: cambiar contraseña y correo con una cuenta de prueba.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `AccountViewModelsTest` (11)
- [x] Integración: `AccountIntegrationTest` (6)
- [x] UI: sin pruebas nuevas
- [x] Seguridad: `ArchitectureTest`, `SourceSecurityTest`
- **No hacer**: guardar o registrar contraseñas, ni cerrar la sesión por un 401 de contraseña incorrecta.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
