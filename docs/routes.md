# Rutas de navegación — vivo (nv-01)

> Fuente de contratos de navegación. Las specs solo enlazan, no duplican. Constitución §4 y §6.
> Las filas de pantallas del producto se agregan con su spec (`nv-04+`).

| Ruta | Pantalla | Permisos | Visible en | Spec |
|---|---|---|---|---|
| `WelcomeRoute` | Bienvenida | ninguno | release | 002 |
| `LoginRoute` | Iniciar sesión | ninguno | release | 003 |
| `RegisterRoute` | Registro | ninguno | release | 005 |
| `RecoverRoute` | Recuperar contraseña | ninguno | release | 004 |
| `CheckEmailRoute(mode, email)` | Revisa tu correo | ninguno | release | 004 |
| `TastesRoute(fromSettings)` | Gustos | ninguno | release | 006 |
| `HomeRoute` | Inicio provisional | ninguno | release | 013 |
| `ProfileRoute` | Perfil | ninguno | release | 010 |
| `EditProfileRoute` | Editar perfil | ninguno | release | 010 |
| `SettingsRoute` | Ajustes | ninguno | release | 009 |
| `SessionsRoute` | Sesiones abiertas | ninguno | release | 009 |
| `PrivacyRoute` | Privacidad | ninguno | release | 011 |
| `DeleteAccountRoute` | Eliminar cuenta | ninguno | release | 011 |
| `DeletionPendingRoute` | Eliminación pendiente | ninguno | release | 011 |
| `debug/conexion` | Estado de conexión con la API (`/health`, `/ready`). Sin implementar | ninguno | solo debug | nv-01 |

Convención: rutas tipadas (`@Serializable`, en `navigation/Routes.kt`), sin parámetros sensibles (nunca tokens). `email` en `CheckEmailRoute` es un dato personal: vive solo en la pila de navegación y se muestra enmascarado.
