# Pruebas en el teléfono pendientes

> Lista viva. Lo que necesita el teléfono (o staging encendido, L-V 08:00–22:00) se anota aquí y el trabajo sigue.
> Teléfono de pruebas: Xiaomi 2207117BPG, Android 12. Tras reinstalar: `adb shell appops set com.wherever.mobile 10021 allow`.

| # | Pantalla | Qué probar | Por qué quedó pendiente | Estado |
|---|---|---|---|---|
| 1 | 25_MiCodigo (nv-20) | Escanear el QR de la cuenta QA B desde otro teléfono o pantalla y llegar a su perfil | 2026-11-21 00:00: staging apagado y el teléfono sin red | abierto |
| 2 | 25_MiCodigo (nv-20) | Negar la cámara → aviso propio de la app (no el diálogo del lector) | el teléfono perdió la red antes de llegar a la pantalla | abierto |
| 3 | 25_MiCodigo (nv-20) | Compartir mi código abre la hoja con la imagen y el texto | idem | abierto |
| 4 | 08_Permisos (nv-11) | Diálogo de avisos en Android 13+ | el teléfono es Android 12 | abierto |
| 5 | NuevaContrasena (nv-08) | Enlace real del correo | staging no manda correos | abierto |
| 6 | Editar perfil (nv-50) | Foto con cámara y galería tras los últimos cambios | — | abierto |
| 7 | Ficha, reservas y Explorar | Con negocios reales | staging sin negocios | abierto |
| 8 | APK de release (nv-41) | Instalar y abrir el APK firmado | — | abierto |
| 9 | 44_MisGrupos (nv-25) | Lista real, unirse y rechazar una invitación con la cuenta QA B | 2026-11-21 12:43: staging en 503 (sábado) | hecho 2026-11-22 (staging, cuentas QA A y B) |
| 10 | 45_GrupoChat (nv-25) | Enviar mensaje, recibirlo en el otro teléfono (sondeo de 5 s) y Reintentar sin red | idem | hecho 2026-11-22 (staging, cuentas QA A y B) |
| 11 | 86_Ayuda (nv-33) | Ajustes → Ayuda, buscar, abrir preguntas y mandar un reporte (`POST /errores`) | 2026-11-22 (domingo): staging apagado; pruebas de UI ya verdes en el Xiaomi | hecho 2026-11-22 (staging, cuentas QA A y B) |
| 12 | Reseñas (nv-32) | Cuenta con check-in: Por reseñar → 68_PostVisita → 78_ResCom → 69_Publicado; editar, eliminar con Deshacer; 80_ResLugar con Útil y Reportar | 2026-11-22 (domingo): staging apagado; pruebas de UI ya verdes en el Xiaomi | hecho 2026-11-22 (staging, cuentas QA A y B) |
| 13 | Recuerdos (nv-31) | Cámara real → 72_Postal → álbum; 75_PostalVer compartir/descargar/eliminar; 70_CrearPub publica un momento | 2026-11-22 (domingo): staging apagado; pruebas de UI ya verdes en el Xiaomi | hecho 2026-11-22 (staging, cuentas QA A y B) |

## Hecho
- 2026-11-21, nv-25: instalado sobre la build anterior (migración Room 4→5 sin cierre y con la sesión intacta); Planes → Mis grupos abre la pantalla, sin servidor muestra error con Reintentar y Volver regresa a Mis planes.
- 2026-11-21, 25_MiCodigo: el QR se dibuja con el código real de la cuenta del teléfono (`QA282D68`) desde Room, sin servidor; Guardar imagen deja el PNG en `Imágenes/Wherever` (se leyó de vuelta: QR correcto); Escanear pide la cámara en contexto y abre el lector.
