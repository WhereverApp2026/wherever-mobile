# Rutas de navegación — vivo (nv-01 implementado; nv-05 auth y onboarding implementados)

> Fuente de contratos de navegación. Las specs solo enlazan, no duplican. Constitución §4 y §6.
> Los grupos de rutas previstos están en `docs/architecture.md`; el inventario de pantallas, en `docs/figma-inventory.md`; las filas del producto se agregan con su spec. `nv-05` registra `auth`; el resto sigue `[planificado nv-06+]`.
> `nv-01` implementa `home` y `debug/conexion`; `nv-05` implementa las 9 rutas auth, el arranque de onboarding y las 4 rutas de sus pasos.

| Ruta | Pantalla | Permisos | Visible en | Spec |
|---|---|---|---|---|
| `home` | Placeholder post-onboarding hasta `nv-06` `[planificado nv-06]` | ninguno | release | nv-01 |
| `debug/conexion` | Estado de conexión con la API (`/health`, `/ready`) | ninguno | solo debug | nv-01 |
| `auth/gate` | Arranque con sesión o sin sesión | ninguno | release | nv-05 |
| `auth/welcome` | Bienvenida; crear cuenta o iniciar sesión | ninguno | release | nv-05 |
| `auth/login` | Login email/contraseña y recuperación | ninguno | release | nv-05 |
| `auth/register` | Registro (paso 1 de 4) | ninguno | release | nv-05 |
| `auth/recover` | Solicitar enlace de recuperación | ninguno | release | nv-05 |
| `auth/recover/sent` | Revisa tu correo; reenvío con espera | ninguno | release | nv-05 |
| `auth/recover/reset` | Nueva contraseña con código | ninguno | release | nv-05 |
| `auth/verify-email` | Verificación de correo | ninguno | release | nv-05 |
| `auth/sessions` | Sesiones activas y cierre global | ninguno | release | nv-05 |
| `auth/onboarding` | Reanuda el paso local o consulta `GET /me` y sale a sesiones si ya completó | ninguno | release | nv-05 |
| `auth/register/preferences` | Gustos (paso 2 de 4) | ninguno | release | nv-05 |
| `auth/register/location` | Racional y permiso de ubicación | `ACCESS_COARSE_LOCATION` | release | nv-05 |
| `auth/register/friends` | Encuentra amigos (paso 3 de 4) | ninguno | release | nv-05 |
| `auth/register/permissions` | Permisos (paso 4 de 4) | `POST_NOTIFICATIONS` | release | nv-05 |

Convención: rutas tipadas, en minúsculas, sin parámetros sensibles (nunca tokens) ni deep links con códigos. El código de reset se captura manualmente en `auth/recover/reset`, se redacta de logs y no se persiste. El email de recuperación no viaja por ruta.
