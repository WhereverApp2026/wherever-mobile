# Permissions Matrix — vivo

> Constitución §6. Los permisos se piden en contexto y la app funciona sin ellos.

| Permiso | Cuándo se pide | Sin el permiso | Spec |
|---|---|---|---|
| `INTERNET` | siempre (normal) | — | nv-01 |
| `POST_NOTIFICATIONS` | al llegar a Inicio con sesión (Android 13+) | la app funciona; no hay push | nv-46 |
| `ACCESS_COARSE_LOCATION` | al usar “cerca de mí” | se elige la ciudad a mano | posterior |
| `CAMERA` | al escanear QR o tomar foto | opción oculta o galería | posterior |

Medir antes de agregar un permiso nuevo; cada uno exige fila aquí (principio empírico §2.5).
