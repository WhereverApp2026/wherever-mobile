# Permissions Matrix — vivo

> Constitución §6. Los permisos se piden en contexto y la app funciona sin ellos.

| Permiso | Cuándo se pide | Sin el permiso | Spec |
|---|---|---|---|
| `INTERNET` | siempre (normal) | — | nv-01 |
| `POST_NOTIFICATIONS` | al llegar a Inicio con sesión (Android 13+) | la app funciona; no hay push | nv-46 |
| `ACCESS_COARSE_LOCATION` | en Inicio, tras la tarjeta que explica para qué (US-004); solo aproximada y mientras se usa la app | recomendaciones generales; tras 2 rechazos solo un aviso que abre Ajustes (US-009) | nv-47 |
| `CAMERA` | no se pide: la foto de perfil abre la cámara del sistema (`TakePicture`) y la galería usa el Photo Picker, que tampoco pide permiso de fotos. Se pedirá al escanear QR | — | nv-50 |

Medir antes de agregar un permiso nuevo; cada uno exige fila aquí (principio empírico §2.5).
