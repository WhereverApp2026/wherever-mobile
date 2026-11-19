# Rutas de navegación — vivo (nv-01)

> Fuente de contratos de navegación. Las specs solo enlazan, no duplican. Constitución §4 y §6.
> Las filas de pantallas del producto se agregan con su spec (`nv-04+`).

| Ruta | Pantalla | Permisos | Visible en | Spec |
|---|---|---|---|---|
| `WelcomeRoute` | Bienvenida | ninguno | release | nv-06 |
| `LoginRoute` | Iniciar sesión | ninguno | release | nv-07 |
| `RegisterRoute` | Registro | ninguno | release | nv-09 |
| `RecoverRoute` | Recuperar contraseña | ninguno | release | nv-08 |
| `CheckEmailRoute(mode, email)` | Revisa tu correo | ninguno | release | nv-08 |
| `TastesRoute(fromSettings)` | Gustos | ninguno | release | nv-10 |
| `HomeRoute` | Inicio con recomendaciones, clima y eventos | ubicación aproximada (opcional, en contexto) | release | nv-47 |
| `ExploreRoute(query, category)` | Explorar: búsqueda, filtros y recientes | ubicación aproximada (opcional) | release | nv-47 |
| `PlaceRoute(id, origin)` | Ficha del lugar | ninguno | release | nv-47 |
| `FavoritesRoute` | Mis favoritos | ninguno | release | nv-47 |
| `CreatePlanRoute(phrase)` | Crear plan con IA y hoja rápida | ubicación aproximada (obligatoria para generar) | release | nv-48 |
| `PlanRoute(id)` | Plan: línea de tiempo y edición | ninguno | release | nv-48 |
| `NotificationsRoute` | Avisos: lista, filtros, marcar leídas y borrar | ninguno | release | nv-52 |
| `MyPlansRoute` | Mis planes (próximos, guardados y pasados) | ninguno | release | nv-48 |
| `ProfileRoute` | Perfil | ninguno | release | nv-14 |
| `EditProfileRoute` | Editar perfil | ninguno | release | nv-14 |
| `SettingsRoute` | Ajustes | ninguno | release | nv-13 |
| `SessionsRoute` | Sesiones abiertas | ninguno | release | nv-13 |
| `PrivacyRoute` | Privacidad | ninguno | release | nv-15 |
| `ChangePasswordRoute` | Cambiar contraseña | ninguno | release | nv-51 |
| `ChangeEmailRoute` | Cambiar correo | ninguno | release | nv-51 |
| `MyDataRoute` | Mis datos: descargar copia y borrar historial | ninguno | release | nv-51 |
| `DeleteAccountRoute` | Eliminar cuenta | ninguno | release | nv-15 |
| `DeletionPendingRoute` | Eliminación pendiente | ninguno | release | nv-15 |
| `debug/conexion` | Estado de conexión con la API (`/health`, `/ready`). Sin implementar | ninguno | solo debug | nv-01 |

Convención: rutas tipadas (`@Serializable`, en `navigation/Routes.kt`), sin parámetros sensibles (nunca tokens). `email` en `CheckEmailRoute` es un dato personal: vive solo en la pila de navegación y se muestra enmascarado.
