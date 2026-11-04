# Permissions Matrix — vivo

> Constitución §6. Los permisos se piden en contexto y la app funciona sin ellos.

| Permiso | Cuándo se pide | Sin el permiso | Spec |
|---|---|---|---|
| `INTERNET` | siempre (normal) | — | nv-01 |
| `POST_NOTIFICATIONS` | onboarding (paso 4) o al activar avisos (Android 13+) | la app funciona; no hay push | nv-05 |
| `ACCESS_COARSE_LOCATION` | onboarding (paso 2) o al usar “cerca de mí” | se elige la ciudad a mano; el onboarding continúa | nv-05 |
| `CAMERA` | al escanear QR o tomar foto | opción oculta o galería | `nv-06+` `[planificado nv-06]` |

- `nv-01` declara `INTERNET` (permiso normal, sin prompt) en `AndroidManifest.xml`.
- `nv-05` declara `ACCESS_COARSE_LOCATION` y `POST_NOTIFICATIONS`; los pide en contexto desde el onboarding y continúa si se deniegan. `POST_NOTIFICATIONS` solo se solicita en Android 13+; en versiones anteriores se muestra como no requerido.
- `nv-05` no solicita `READ_CONTACTS` ni integra Facebook: la importación de contactos y el proveedor social quedan diferidos `[planificado nv-06]`.
- `nv-05` no pide ubicación de fondo ni ubicación precisa; solo `ACCESS_COARSE_LOCATION` en contexto. El permiso ya concedido se muestra como estado y no se relanza el diálogo.

Medir antes de agregar un permiso nuevo; cada uno exige fila aquí (principio empírico §2.5).
