# Permissions Matrix — vivo

> Constitución §6. Los permisos se piden en contexto y la app funciona sin ellos.

| Permiso | Cuándo se pide | Sin el permiso | Spec |
|---|---|---|---|
| `INTERNET` | siempre (normal) | — | nv-01 |
| `ACCESS_NETWORK_STATE` | siempre (normal, sin diálogo): detectar Sin conexión con `ConnectivityManager` | — | nv-16 |
| `POST_NOTIFICATIONS` | en `08_Permisos` (paso 3 del alta) con "Permitir y continuar"; si no se decidió ahí, al llegar a Inicio (Android 13+) | la app funciona; no hay push | nv-11, nv-46 |
| `ACCESS_COARSE_LOCATION` | en `08_Permisos` (paso 3 del alta) y en Inicio, tras la tarjeta que explica para qué (US-004); solo aproximada y mientras se usa la app. También al tocar «Usar mi ubicación» en la llegada a una parada (nv-30), solo para ese check-in | recomendaciones generales; tras 2 rechazos solo un aviso que abre Ajustes (US-009) | nv-47 |
| `CAMERA` | al tocar Escanear en 25_MiCodigo y «Escanear el código del negocio» en la llegada (nv-30): lo declara y lo pide en contexto el lector de ZXing (`zxing-android-embedded`). También al tocar el disparador de 71_Camara (nv-31): como el manifiesto ya lo declara, `TakePicture` lo necesita concedido. La foto de perfil usa la cámara del sistema (`TakePicture`) y el Photo Picker | Escanear queda deshabilitado; el código se escribe a mano en Agregar amigos. En 71_Camara se elige la foto de la galería | nv-20, nv-50, nv-31 |

Medir antes de agregar un permiso nuevo; cada uno exige fila aquí (principio empírico §2.5).
