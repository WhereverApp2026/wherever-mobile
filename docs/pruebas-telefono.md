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
| 14 | 83_Insignias (nv-14) | Catálogo, avance y nivel con `GET /usuarios/yo/insignias` | — | hecho 2026-11-23 (staging, cuenta QA A); arreglado el texto del siguiente nivel |
| 15 | 43_CrearGrupo, 46_Grupo, 48_Votar (nv-25) | Crear grupo desde «Invitar amigos» de 39_Plan con QA B y nombre; ver integrantes; votaciones vacías | — | hecho 2026-11-23 (staging, cuenta QA A) |
| 16 | 48_Votar con una votación real (nv-25) | Votar, cambiar el voto y cerrar | la app aún no crea votaciones (`POST /grupos/{id}/votaciones`) | hecho 2026-11-24 01:55 (staging): votación creada por la API como organizador (`parada_id`, `opciones` = ids de lugar, `cierra_en`); en la app votar, cambiar el voto y cerrar con «Ganó». Crear votaciones desde la app sigue pendiente |
| 17 | 07_EncuentraAmigos (nv-20) | Buscar en mis contactos | manda a staging los números reales de la agenda del teléfono: necesita el visto bueno de Jorge o un teléfono con agenda de prueba | abierto |
| 18 | InvitarPlan (nv-25) | Perfil de QA B → Invitar a un plan → elegir plan → Enviar | — | hecho 2026-11-24 01:51 (staging, QA A → QA B): crea el grupo del plan, «Ir al grupo» abre el chat. Antes, 26_PerfilAmigo fallaba con «respondió algo inesperado»: `GET /personas/{id}` manda ahora `nivel: {numero, nombre}`; arreglado (acepta texto u objeto) |
| 19 | Planes de Diego (nv-23, 24, 26, 30) | Contra staging | integradas en `main` (#67) | 2026-11-23: hechas IAPalabra, 17_AjustaPlan, Generando 1–3, 39_Plan, 40_Cambiar, 41_AgregarParada, CrearPlan (con error de parada cerrada) y 28_Vaca vacía. 2026-11-24: 45_AgregarGasto y La vaca con deuda y Marcar pagado (QA B confirmó el grupo). Abiertas: Generando 4/4 (422 del backend), 26_CheckinOk (hay que estar en el lugar) |
| 21 | Modo negocio (nv-34, 35, 37) | Alta completa y panel | integradas en `main` (#67) | 2026-11-23: paso 2 del alta guardado. Abierto: el paso 3 manda la ubicación real del teléfono (necesita el visto bueno de Jorge o ubicación simulada) y el panel necesita un negocio aprobado por un administrador en staging |
| 20 | Biometría y Room cifrada (#68) | Encender en Ajustes, salir más de 1 min y volver; migración con datos reales | — | migración hecha 2026-11-23 (datos intactos, archivo cifrado); biometría abierta |
| 22 | Me gusta, comentarios, 13_Reacciones e historias (nv-17) | Dar y quitar me gusta, comentar, responder, borrar; abrir reacciones; fila de historias y visor | 2026-11-23 16:07: la cuenta con sesión en el teléfono tiene 0 amigos y 0 publicaciones: el muro de Amigos sale vacío (estado vacío correcto) y no hay momento donde probar | hecho 2026-11-24 01:45 (staging, QA A con QA B): me gusta y quitar, comentar, responder, borrar, 13_Reacciones, fila de historias, visor y «Ver publicación». Arreglados en la app: el conteo de comentarios no cambiaba, «Ver más comentarios» salía con cursor vacío y el rechazo salía como «Datos inválidos» sin motivo. Backend: algunas respuestas (`padre_id`) dan 422 «no cumple las normas» con textos inocentes (avisar a Osmar) |
| 23 | 32_ExpMapa (nv-21) | Ver mapa desde Explorar, pines de staging, Buscar en esta zona, filtros, tarjeta, Ver detalle y A mi plan | — | hecho 2026-11-23 (staging): pines reales, tarjeta, Ver detalle, A mi plan (abre IAPalabra con el nombre), mover → Buscar en esta zona, filtro Cafetería con estado vacío, timeout de la API con aviso y reintento. Arreglados: el botón salía sin mover el mapa y los pines eran los de osmdroid |
| 24 | Explorar y ficha con negocios reales | Lista, filtro Abierto ahora, búsqueda «cafe», ficha con horario, Llamar y Reseñas | staging ya tiene lugares reales cerca (Tula) | hecho 2026-11-23. Pendiente: «Cómo llegar» se partía en dos líneas junto a «Llamar» (arreglado en `78d4e39`: una línea con puntos, capturas regrabadas); Reservar no sale porque estos lugares no son negocios de Wherever; Inicio «Cerca de ti» sale vacío aunque hay lugares a 1 km: 2026-11-24 `GET /inicio?lat&lng` devuelve `recomendaciones: []` junto a «Deportivo Hidalgo» (backend; la app muestra bien su estado vacío; avisar a Osmar) |
| 25 | Generando 4/4 (nv-23) | Con lugares cerca | — | 2026-11-23 16:57: `POST /planes/generar` sigue en 422 con 15+ lugares abiertos a menos de 2 km, a pie y en coche: bloqueo del backend, avisar a Osmar |

## Hecho
- 2026-11-21, nv-25: instalado sobre la build anterior (migración Room 4→5 sin cierre y con la sesión intacta); Planes → Mis grupos abre la pantalla, sin servidor muestra error con Reintentar y Volver regresa a Mis planes.
- 2026-11-21, 25_MiCodigo: el QR se dibuja con el código real de la cuenta del teléfono (`QA282D68`) desde Room, sin servidor; Guardar imagen deja el PNG en `Imágenes/Wherever` (se leyó de vuelta: QR correcto); Escanear pide la cámara en contexto y abre el lector.
