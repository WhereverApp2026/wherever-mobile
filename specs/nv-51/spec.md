# NV-51 — Cuenta: cambiar contraseña y correo, descargar mis datos y borrar historial

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Cierra el pendiente P9 de `docs/avance.md` (parte de US-007 y US-017). Rama `feat/cuenta-seguridad`, desde `main`.

## 1. Objetivo

Que la persona pueda, desde la app y contra la API real:
- cambiar su contraseña (US-017);
- cambiar su correo, que queda sin verificar hasta abrir el enlace (US-017);
- pedir una copia de sus datos, que llega por correo (US-007);
- borrar su historial sin borrar la cuenta (US-007).

## 2. Alcance / No-alcance

### Alcance
- **Ajustes → Cuenta:** filas "Correo" (muestra el actual) y "Contraseña".
- **Cambiar contraseña:** actual, nueva y repetir. La nueva sigue la regla de la API (8 a 128, letra y número), debe ser distinta de la actual y coincidir con la repetida. Aviso de que se cierran las otras sesiones. Al terminar, mensaje y "Listo".
- **Cambiar correo:** muestra el actual, pide el nuevo (sin espacios, distinto del actual) y la contraseña. Al terminar, "Te mandamos un enlace a …" y el perfil se vuelve a leer, así que Perfil muestra "Aún no verificas tu correo".
- **Privacidad → Mis datos:** "Pedir mi copia" (`POST /usuarios/yo/exportar`) y "Borrar historial" con confirmación (`DELETE /usuarios/yo/historial`). El 429 de la exportación (una por día) se muestra como aviso, no como error.
- Versión `versionCode 6`, `versionName 0.4.0`.

### No-alcance
- Restablecer contraseña desde el enlace del correo (`POST /auth/restablecer`) y descargar la copia desde el enlace: necesitan App Links verificados y que staging mande correos (P6).
- Horario silencioso (US-148): va con la pantalla de notificaciones.

## 3. Contratos (API y navegación)

wherever-backend `docs/endpoints.md` (`origin/main`, nv-04 y nv-05):

| Método | Ruta | Cuerpo | Respuestas que la app maneja |
|---|---|---|---|
| PUT | `/auth/password` | `{password_actual, password_nuevo}` | 204; 401 = contraseña actual incorrecta; 422 por campo |
| PUT | `/usuarios/yo/correo` | `{correo_nuevo, password}` | 200; 401 = contraseña incorrecta; 409 = correo con cuenta; 422 por campo |
| POST | `/usuarios/yo/exportar` | — | 202; 429 con `Retry-After` |
| DELETE | `/usuarios/yo/historial` | — | 204 |

El 401 de contraseña incorrecta pasa primero por el `Authenticator`: renueva el token una vez, repite y el segundo 401 llega a la app sin cerrar la sesión (se prueba en `AccountIntegrationTest`).

Rutas nuevas en `docs/routes.md`: `ChangePasswordRoute`, `ChangeEmailRoute` y `MyDataRoute`.

## 4. Modelo de datos

Sin cambios en Room. `feature/account` (dominio `AccountRepository`, datos `AccountApi` y `AccountRepositoryImpl`). Tres `UiState`: `ChangePasswordUiState`, `ChangeEmailUiState` y `MyDataUiState`.

## 5. Seguridad y privacidad

- Las contraseñas no se guardan ni se registran: viven solo en el `UiState` y el `RedactedLoggingInterceptor` no imprime cuerpos (`AccountIntegrationTest` revisa los logs).
- Sin permisos nuevos y sin cambios en la cadena de red (constitución §4).
- Borrar historial pide confirmación porque no se puede deshacer.

## 6. Tests / Gates

- **Unit:** `test/.../feature/account/ui/AccountViewModelsTest.kt` (11): contraseña válida, débil o distinta, igual a la actual, 401 y 422 por campo; correo nuevo, el mismo correo, 409 y 401; exportar con 202 y 429; borrar con y sin confirmación y su falla.
- **Data:** `test/.../integration/AccountIntegrationTest.kt` (6) con la cadena real y MockWebServer: cuerpos y rutas exactos, 401 tras renovar sin perder la sesión, 409, 429 con `Retry-After`, `DELETE` y logs sin contraseñas.
- **UI:** sin pruebas instrumentadas nuevas; las pantallas usan componentes ya probados (`FormScreen`, `LabeledTextField`).
- **Security:** `ArchitectureTest` y `SourceSecurityTest` siguen verdes con el paquete nuevo.

## 7. Plan de entrega

Commit directo a `main` a pedido de Jorge (2026-11-19). Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
