# Specs de Wherever Móvil

> Rector: `.specify/memory/constitution.md`. Todas las specs de producto están en 🚧 Borrador: falta el nombre del revisor. Salen del análisis de `maqueta/figma-frames/` (local, no se sube a Git) y de `docs/contexto_api_sprint1.md`.

## Índice

| Spec | Tema | Sprint 1 | Frames |
|---|---|---|---|
| [`nv-00…nv-03`](nv-00/spec.md) | Constitución, foundation, molde y puerta de calidad | Base | — |
| [`nv-04`](nv-04/spec.md) | Capas, red, tokens, DI, paquetes | Base | — |
| [`nv-05`](nv-05/spec.md) | Mapa de navegación, flujos de usuario y negocio | — | Todos |
| [`nv-06`](nv-06/spec.md) | Splash y bienvenida | Sí | 1 |
| [`nv-07`](nv-07/spec.md) | Iniciar sesión | Sí | 1 |
| [`nv-08`](nv-08/spec.md) | Recuperar contraseña | Sí | 3 |
| [`nv-09`](nv-09/spec.md) | Registro con correo | Sí | 1 |
| [`nv-10`](nv-10/spec.md) | Onboarding de gustos | Sí | 1 |
| [`nv-11`](nv-11/spec.md) | Permisos en contexto | — | 2 |
| [`nv-12`](nv-12/spec.md) | Sesión y tokens | Sí | 0 |
| [`nv-13`](nv-13/spec.md) | Ajustes y cerrar sesión | Sí | 1 |
| [`nv-14`](nv-14/spec.md) | Perfil, edición e insignias | Sí | 4 |
| [`nv-15`](nv-15/spec.md) | Privacidad, mis datos y eliminar cuenta | Sí | 2 |
| [`nv-16`](nv-16/spec.md) | Sin conexión | Sí | 1 |
| [`nv-17`](nv-17/spec.md) | Inicio y feed | — | 15 |
| [`nv-18`](nv-18/spec.md) | Notificaciones | — | 4 |
| [`nv-19`](nv-19/spec.md) | Búsqueda | — | 4 |
| [`nv-20`](nv-20/spec.md) | Amigos | — | 15 |
| [`nv-21`](nv-21/spec.md) | Explorar y lugares | — | 8 |
| [`nv-22`](nv-22/spec.md) | Favoritos | — | 2 |
| [`nv-23`](nv-23/spec.md) | Crear plan con IA | — | 6 |
| [`nv-24`](nv-24/spec.md) | Plan: detalle y edición | — | 5 |
| [`nv-25`](nv-25/spec.md) | Compartir, grupos y chats | — | 12 |
| [`nv-26`](nv-26/spec.md) | Vaca virtual y gastos | — | 4 |
| [`nv-27`](nv-27/spec.md) | Reserva y pago | — | 10 |
| [`nv-28`](nv-28/spec.md) | Mis planes | — | 2 |
| [`nv-29`](nv-29/spec.md) | Cupones | — | 5 |
| [`nv-30`](nv-30/spec.md) | El día del plan | — | 5 |
| [`nv-31`](nv-31/spec.md) | Recuerdos y postales | — | 13 |
| [`nv-32`](nv-32/spec.md) | Reseñas | — | 8 |
| [`nv-33`](nv-33/spec.md) | Ayuda | — | 1 |
| [`nv-34`](nv-34/spec.md) | Negocio: registro | — | 6 |
| [`nv-35`](nv-35/spec.md) | Negocio: panel, reservas y check-in | — | 8 |
| [`nv-36`](nv-36/spec.md) | Negocio: reseñas y momentos | — | 3 |
| [`nv-37`](nv-37/spec.md) | Negocio: cupones y cupo | — | 3 |
| [`nv-38`](nv-38/spec.md) | Negocio: perfil, fotos y cobros | — | 6 |
| [`nv-39`](nv-39/spec.md) | CI/CD: gates del pull request | — | — |

Inventario: **162 frames** de pantalla, que son 135 pantallas distintas al agrupar variantes, y 36 anotaciones `label · …` que no son pantallas.

## Pantallas que ya consumen la API

Implementadas el 2026-11-04 y probadas ese día contra staging, con una cuenta de prueba, en un teléfono Samsung SM-A346M (Android 16), en tema claro y oscuro. **No se han probado en emulador.** Lo que faltó probar está en cada spec.

| Pantalla | Frame | Spec | Endpoints | Estado |
|---|---|---|---|---|
| Bienvenida y arranque | `01_Bienvenida` | nv-06 | `GET /usuarios/yo` (si hay sesión) | ✅ |
| Iniciar sesión | `03_Login` | nv-07 | `POST /auth/login`, `GET /usuarios/yo` | ✅ |
| Registro | `05_Registro` | nv-09 | `GET /usuarios/correo-disponible`, `POST /usuarios`, `GET /usuarios/yo` | ✅ |
| Recuperar contraseña | `03_Recuperar` | nv-08 | `POST /auth/recuperar` | ✅ |
| Revisa tu correo | `04_RevisaCorreo` | nv-08 | `POST /auth/recuperar`, `POST /auth/reenviar-verificacion` | ✅ |
| Gustos | `06_Gustos` | nv-10 | `PUT /usuarios/yo/gustos`, `PATCH /usuarios/yo/preferencias`, `PATCH /usuarios/yo`, `GET /ciudades/autocompletar` | ✅ |
| Perfil | `81_Perfil` | nv-14 | `GET /usuarios/yo` | ✅ |
| Editar perfil | `82_EditarPerfil`, `84_DatosPers` | nv-14 | `PATCH /usuarios/yo` con `If-Match`, `GET /ciudades/autocompletar` | ✅ |
| Ajustes | `85_Ajustes` | nv-13 | `PATCH /usuarios/yo/preferencias`, `PATCH /usuarios/yo/privacidad`, `POST /auth/logout` | ✅ |
| Sesiones abiertas | Sin frame | nv-13 | `GET /auth/sesiones`, `DELETE /auth/sesiones/{id}`, `POST /auth/logout-todos` | ✅ |
| Privacidad | `28_PrivSocial` | nv-15 | `PATCH /usuarios/yo/privacidad` | ✅ |
| Eliminar cuenta | `EliminarCuenta` | nv-15 | `DELETE /usuarios/yo` | ✅ |
| Eliminación pendiente | Sin frame | nv-15 | `POST /usuarios/yo/cancelar-eliminacion`, `POST /auth/logout` | ✅ |
| Inicio provisional | Parte de `10_Inicio` | nv-17 | Lee el perfil de Room | Provisional |
| Sesión y tokens | Sin pantalla | nv-12 | `POST /auth/refresh` | ✅ |

Pendiente del Sprint 1: `NuevaContrasena` y verificar correo por enlace (D11), Sin conexión (nv-16), Descargar datos y Borrar historial (D35).

## Pantalla → endpoints

"Pendiente de backend" significa que no hay endpoint publicado en la API v1. El detalle de cada caso está en la spec.

| Frame | Spec | Endpoints |
|---|---|---|
| `01_Bienvenida` | nv-06 | `GET /usuarios/yo` |
| `03_Login` | nv-07 | `POST /auth/login` |
| `03_Recuperar` | nv-08 | `POST /auth/recuperar` |
| `04_RevisaCorreo` | nv-08 | `POST /auth/recuperar` |
| `NuevaContrasena` | nv-08 | `POST /auth/restablecer` |
| `05_Registro` | nv-09 | `GET /usuarios/correo-disponible`<br>`POST /usuarios`<br>`GET /ciudades/autocompletar` |
| `06_Gustos` | nv-10 | `PUT /usuarios/yo/gustos`<br>`PATCH /usuarios/yo/preferencias`<br>`GET /ciudades/autocompletar`<br>`PATCH /usuarios/yo` |
| `08_Permisos` | nv-11 | `PATCH /usuarios/yo/privacidad` |
| `06_PermisoSys` | nv-11 | Pendiente de backend |
| `85_Ajustes` | nv-13 | `POST /auth/logout`<br>`PATCH /usuarios/yo/preferencias`<br>`PATCH /usuarios/yo/privacidad`<br>`PATCH /usuarios/yo`<br>`GET /ciudades/autocompletar` |
| `81_Perfil` | nv-14 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `82_EditarPerfil` | nv-14 | `PATCH /usuarios/yo`<br>Parte pendiente de backend |
| `83_Insignias` | nv-14 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `84_DatosPers` | nv-14 | `PATCH /usuarios/yo`<br>`PUT /usuarios/yo/correo`<br>`PUT /usuarios/yo/telefono`<br>`GET /ciudades/autocompletar`<br>Parte pendiente de backend |
| `28_PrivSocial` | nv-15 | `PATCH /usuarios/yo/privacidad`<br>`POST /usuarios/yo/exportar`<br>`DELETE /usuarios/yo/historial`<br>Parte pendiente de backend |
| `EliminarCuenta` | nv-15 | `DELETE /usuarios/yo`<br>`POST /usuarios/yo/cancelar-eliminacion`<br>Parte pendiente de backend |
| `89_SinConexion` | nv-16 | `GET /health` |
| `10_Inicio` | nv-17 | Pendiente de backend |
| `11_InicioAmigos` | nv-17 | Pendiente de backend |
| `12_InicioMas` | nv-17 | Pendiente de backend |
| `InicioMas · solicitud enviada` | nv-17 | Pendiente de backend |
| `13_Reacciones` | nv-17 | Pendiente de backend |
| `Reacciones · solicitud enviada` | nv-17 | Pendiente de backend |
| `14_MeApunto` | nv-17 | Pendiente de backend |
| `15_Crear` | nv-17 | Pendiente de backend |
| `16_Historia` | nv-17 | Pendiente de backend |
| `Historia · *` | nv-17 | Pendiente de backend |
| `17_PostDetalle` | nv-17 | Pendiente de backend |
| `29_PlanAmigo` | nv-17 | Pendiente de backend |
| `PlanAmigo · *` | nv-17 | Pendiente de backend |
| `18_Notif` | nv-18 | Pendiente de backend |
| `Notif · solicitud aceptada` | nv-18 | Pendiente de backend |
| `87_PushBloqueo` | nv-18 | Pendiente de backend |
| `88_PushBanner` | nv-18 | Pendiente de backend |
| `19_Buscar` | nv-19 | Pendiente de backend |
| `20_BuscarPersonas` | nv-19 | Pendiente de backend |
| `BuscarPersonas · solicitud enviada` | nv-19 | Pendiente de backend |
| `21_SinResultados` | nv-19 | Pendiente de backend |
| `07_EncuentraAmigos` | nv-20 | Pendiente de backend |
| `EncuentraAmigos · solicitud enviada` | nv-20 | Pendiente de backend |
| `22_AmigosLista` | nv-20 | Pendiente de backend |
| `23_AmigosSolic` | nv-20 | Pendiente de backend |
| `24_AgregarAmigo` | nv-20 | Pendiente de backend |
| `AgregarAmigo · solicitud enviada` | nv-20 | Pendiente de backend |
| `25_MiCodigo` | nv-20 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `26_PerfilAmigo` | nv-20 | Pendiente de backend |
| `PerfilAmigo · *` | nv-20 | Pendiente de backend |
| `27_AmigoOpc` | nv-20 | Pendiente de backend |
| `30_Explorar` | nv-21 | Pendiente de backend |
| `31_Filtros` | nv-21 | Pendiente de backend |
| `32_ExpMapa` | nv-21 | Pendiente de backend |
| `33_Detalle` | nv-21 | Pendiente de backend |
| `Detalle · *` | nv-21 | Pendiente de backend |
| `34_Sello` | nv-21 | Pendiente de backend |
| `35_Favoritos` | nv-22 | Pendiente de backend |
| `36_FavVacio` | nv-22 | Pendiente de backend |
| `IAPalabra` | nv-23 | Pendiente de backend |
| `17_AjustaPlan` | nv-23 | Pendiente de backend |
| `Generando *` | nv-23 | Pendiente de backend |
| `39_Plan` | nv-24 | Pendiente de backend |
| `Plan · *` | nv-24 | Pendiente de backend |
| `40_Cambiar` | nv-24 | Pendiente de backend |
| `41_AgregarParada` | nv-24 | Pendiente de backend |
| `CrearPlan` | nv-24 | Pendiente de backend |
| `42_Compartir` | nv-25 | Pendiente de backend |
| `43_CrearGrupo` | nv-25 | Pendiente de backend |
| `44_MisGrupos` | nv-25 | Pendiente de backend |
| `45_GrupoChat` | nv-25 | Pendiente de backend |
| `GrupoChat · *` | nv-25 | Pendiente de backend |
| `46_Grupo` | nv-25 | Pendiente de backend |
| `47_PuntoMedio` | nv-25 | Pendiente de backend |
| `48_Votar` | nv-25 | Pendiente de backend |
| `ChatAmigo` | nv-25 | Pendiente de backend |
| `ChatAmigo · *` | nv-25 | Pendiente de backend |
| `InvitarPlan` | nv-25 | Pendiente de backend |
| `28_Vaca` | nv-26 | Pendiente de backend |
| `Vaca · *` | nv-26 | Pendiente de backend |
| `45_AgregarGasto` | nv-26 | Pendiente de backend |
| `22_Reserva` | nv-27 | Pendiente de backend |
| `35_Pasarela` | nv-27 | Pendiente de backend |
| `36_PagoError` | nv-27 | Pendiente de backend |
| `23_Confirmacion` | nv-27 | Pendiente de backend |
| `25_MisReservas` | nv-27 | Pendiente de backend |
| `26_DetReserva` | nv-27 | Pendiente de backend |
| `DetReserva · *` | nv-27 | Pendiente de backend |
| `27_Cancelar` | nv-27 | Pendiente de backend |
| `38_CambiarHora` | nv-27 | Pendiente de backend |
| `24_MisPlanes` | nv-28 | Pendiente de backend |
| `41_PlanesVacio` | nv-28 | Pendiente de backend |
| `29_Cupones` | nv-29 | Pendiente de backend |
| `47_DetCupon` | nv-29 | Pendiente de backend |
| `DetCupon · *` | nv-29 | Pendiente de backend |
| `13_Mapa` | nv-30 | Pendiente de backend |
| `14_MapaFull` | nv-30 | Pendiente de backend |
| `15_Llegar` | nv-30 | Pendiente de backend |
| `18_Navegando` | nv-30 | Pendiente de backend |
| `26_CheckinOk` | nv-30 | Pendiente de backend |
| `70_CrearPub` | nv-31 | Pendiente de backend |
| `71_Camara` | nv-31 | Pendiente de backend |
| `72_Postal` | nv-31 | Pendiente de backend |
| `73_Album` | nv-31 | Pendiente de backend |
| `74_RecPlan` | nv-31 | Pendiente de backend |
| `75_PostalVer` | nv-31 | Pendiente de backend |
| `PostalVer · *` | nv-31 | Pendiente de backend |
| `76_RecVacio` | nv-31 | Pendiente de backend |
| `68_PostVisita` | nv-32 | Pendiente de backend |
| `69_Publicado` | nv-32 | Pendiente de backend |
| `77_ResPend` | nv-32 | Pendiente de backend |
| `78_ResCom` | nv-32 | Pendiente de backend |
| `79_MisRes` | nv-32 | Pendiente de backend |
| `MisRes · *` | nv-32 | Pendiente de backend |
| `80_ResLugar` | nv-32 | Pendiente de backend |
| `86_Ayuda` | nv-33 | Pendiente de backend |
| `42_NegCuenta` | nv-34 | `POST /usuarios`<br>Parte pendiente de backend |
| `43_NegRegistro` | nv-34 | Pendiente de backend |
| `44_NegUbic` | nv-34 | Pendiente de backend |
| `45_NegOper` | nv-34 | Pendiente de backend |
| `46_NegVerif` | nv-34 | Pendiente de backend |
| `47_NegListo` | nv-34 | Pendiente de backend |
| `48_NegPanel` | nv-35 | Pendiente de backend |
| `49_NegReservas` | nv-35 | Pendiente de backend |
| `50_NegCheckin` | nv-35 | Pendiente de backend |
| `NegCheckin · *` | nv-35 | Pendiente de backend |
| `56_NegStats` | nv-35 | Pendiente de backend |
| `51_NegResenas` | nv-36 | Pendiente de backend |
| `52_NegResp` | nv-36 | Pendiente de backend |
| `NegMomentos` | nv-36 | Pendiente de backend |
| `53_NegCupones` | nv-37 | Pendiente de backend |
| `54_NegCrear` | nv-37 | Pendiente de backend |
| `55_NegHoras` | nv-37 | Pendiente de backend |
| `58_NegPerfil` | nv-38 | `POST /usuarios/yo/modo`<br>`POST /auth/logout`<br>Parte pendiente de backend |
| `87_NegGaleria` | nv-38 | Pendiente de backend |
| `88_NegAgregarFoto` | nv-38 | Pendiente de backend |
| `57_NegCobros` | nv-38 | Pendiente de backend |
| `89_NegBanco` | nv-38 | Pendiente de backend |
| `90_NegFiscal` | nv-38 | Pendiente de backend |
| Sin pantalla (sesión) | nv-12 | `POST /auth/refresh` |

### Endpoints publicados sin pantalla dibujada

Con pantalla propia ya implementada: sesiones y `logout-todos`, en Sesiones abiertas; `reenviar-verificacion`, en Revisa tu correo.

- `POST /auth/logout-todos`
- `GET /auth/sesiones`
- `DELETE /auth/sesiones/{id}`
- `POST /auth/verificar-correo`
- `POST /auth/reenviar-verificacion`
- `PUT /auth/password`
- `POST /usuarios/exportaciones/descargar`

## Dudas encontradas

### Cómo quedaron en el código (a validar)

Las dudas siguen abiertas hasta que diseño o backend las confirmen. Mientras tanto, el código hace esto:

- **D01:** se agregó la fecha de nacimiento al registro.
- **D02:** el texto dice 1 hora.
- **D03:** se sigue a la API: escribir ELIMINAR, 30 días para cancelar.
- **D04:** Privacidad muestra los interruptores de la API, no los del frame.
- **D05:** 15 chips con emoji y deslizador, como piden las historias. **Las claves de categorías son propuestas:** staging aceptó `tacos`, `cafes` y `aire_libre`; las demás no se han enviado.
- **D06:** sin interruptor de recordatorios de reservación.
- **D07:** agregados tema y sesiones.
- **D09:** una sola pantalla de edición, con botón Guardar.
- **D10:** sin botón de Google.
- **D12:** alta de 2 pasos y marca local al omitir gustos.
- **D13:** se agregaron textos de 423, 409 y eliminación pendiente, sin validación de diseño.
- **D14:** versión de términos en `BuildConfig` y `promociones_correo=false`.
- **D16:** el refresh con fallo transitorio conserva la sesión.
- **D19:** la ciudad se pide en Gustos.
- **D20:** aviso en Perfil con opción de reenviar.
- **D22:** barra provisional con Inicio y Perfil.

### Encontradas al implementar

- **D38 · Catálogo de ciudades.** En staging, `GET /ciudades/autocompletar` devolvió `[]` con `q=tula` y con `q=guada`, que es el ejemplo de la documentación. Parece que el catálogo está vacío: hasta que tenga datos, en la app no se puede elegir ciudad. La pantalla muestra "No encontramos esa ciudad". Seguía vacío el 2026-11-04 a las 18:29 con `q=tula`.
- **D39 · Permisos de WorkManager.** Al agregar WorkManager (`nv-01`) hay que registrar sus permisos en `docs/permissions-matrix.md`.
- **D40 · Tema que no cambia, intermitente.** Al elegir Oscuro, el `PATCH /usuarios/yo/preferencias` respondió 200 y la app siguió en Claro. Pasó 1 de 8 veces y no se reprodujo. Hipótesis sin confirmar: el `GET /usuarios/yo` posterior devolvió el valor anterior y sobrescribió Room. Falta saber si staging puede leer un valor viejo justo después de escribir.
- **D41 · Pesos de fuente en Xiaomi.** En un Xiaomi 2311DRK48G con Android 16, títulos y botones se vieron sin negrita; en el Samsung sí salen bien. No se sabe si es la configuración de ese teléfono o algo general de HyperOS. No se corrigió y falta repetir la prueba allí.
- **D42 · Fecha de nacimiento y accesibilidad.** La capa que abre el selector oculta el valor del campo en el árbol de accesibilidad. Falta probarlo con TalkBack.

### Afectan al Sprint 1 (3 al 16 nov 2026)

- **D01 · Registro sin fecha de nacimiento.** US-012 y `POST /usuarios` exigen `fecha_nacimiento` (18+), pero `05_Registro` no tiene ese campo. Sin él la API responde 422. Propuesta: agregar el campo al diseño.
- **D02 · Vigencia del enlace de recuperación.** `04_RevisaCorreo` y `NuevaContrasena` dicen "30 minutos". US-014 y la API dicen 1 h. Propuesta: el texto dice 1 h.
- **D03 · Eliminar cuenta: la maqueta contradice a la API.** `EliminarCuenta` dice "no se puede deshacer" y pide una casilla. US-007 y `DELETE /usuarios/yo` piden escribir ELIMINAR y dan 30 días para cancelar. Propuesta: seguir la API y corregir el texto.
- **D04 · Privacidad: dos pantallas distintas.** US-007 y US-236 piden interruptores de ubicación, aprendizaje, ánimo, datos de uso, buscable por teléfono, descargar datos y borrar historial, y citan `28_PrivSocial`. Ese frame muestra otra cosa (quién ve mi actividad, qué comparto, sugerencias) y nada de eso existe en la API. No hay frame para lo que pide la historia.
- **D05 · Gustos: maqueta, historia y API no coinciden.** US-020 pide 15 chips con emoji; `06_Gustos` tiene 8 sin emoji. US-021 pide deslizador de 0 a 1,000+; el frame tiene $ / $$ / $$$. US-022 (dieta, alergias) no aparece. "Cómo me muevo" permite varios, y `movilidad_default` es uno solo. "Suelo salir" no tiene campo en la API. Tampoco hay catálogo de `categorias`: faltan sus claves.
- **D06 · Recordatorios de reservación.** `85_Ajustes` tiene un interruptor "Recordatorios de reservación", pero `notificaciones.reservas:false` responde 422 porque no se pueden apagar. Propuesta: quitar el interruptor.
- **D07 · Funciones de API sin pantalla.** Sin frame: tema (US-017), sesiones abiertas, cerrar todas las sesiones, cambiar contraseña, cambiar correo con contraseña, horario silencioso y unidades. `85_Ajustes` tiene las filas Correo y Contraseña, pero sin destino dibujado.
- **D08 · Perfil: campos que la API no tiene.** `81_Perfil` y `82_EditarPerfil` muestran `@usuario` y bio, que no existen en la API. `84_DatosPers` muestra el teléfono en claro, y la API nunca lo devuelve (solo `telefono_registrado`). Tampoco hay contadores de planes, amigos y reseñas.
- **D09 · Guardar al instante o con botón.** US-017 dice que los cambios se guardan al instante; `82_EditarPerfil` tiene "Listo" y `84_DatosPers` "Guardar cambios". Además, ambas editan nombre y foto. Propuesta: guardar con botón (encaja con `If-Match`) y fusionar las dos pantallas.
- **D10 · Google Sign-In.** `03_Login` y `05_Registro` muestran "Continuar con Google", pero `POST /auth/google` está diferido. Propuesta: ocultar el botón hasta que exista.
- **D11 · Enlaces profundos.** Falta el dominio de los enlaces de correo (verificar, restablecer, exportar) y su `assetlinks.json` para App Links. `42_Compartir` sugiere `wherever.app`. Sin esto no se cierra US-014.
- **D12 · Onboarding: paso de amigos y cómo saber si terminó.** El paso 3 de 4 (`07_EncuentraAmigos`) depende de amigos, sin endpoints (US-232/233 fuera de alcance). Propuesta: en Sprint 1 son 3 pasos. Además, la API no indica si se completó el onboarding; si se omiten los gustos, `gustos` queda `null` igual que en una cuenta nueva. Propuesta: marca local por usuario.
- **D13 · Estados sin frame.** Sin dibujo: login bloqueado (423), correo ya registrado (409), verificación de correo, cuenta con eliminación pendiente, confirmación de cerrar sesión, carga genérica y error genérico. Estas specs los definen con textos propuestos; falta que los valide diseño.
- **D14 · Términos y aviso de privacidad.** `POST /usuarios` pide `version_terminos`, y no hay endpoint para saber la vigente. Tampoco hay URL ni frame de los textos, ni casilla para `promociones_correo`. Propuesta: versión en `BuildConfig`, `promociones_correo=false` por defecto.
- **D15 · Cuenta con eliminación pendiente.** El ejemplo solo muestra `estado: "activo"`: falta el valor para pendiente. `DELETE /usuarios/yo` cierra todas las sesiones, así que cancelar exige volver a entrar: hay que confirmar que `/auth/login` funciona en ese estado. **Confirmado en staging el 2026-11-04:** responde 200 y `GET /usuarios/yo` permite mostrar la pantalla de eliminación pendiente.
- **D16 · Refresh con fallo transitorio.** Constitución §6: "si el refresh falla, se limpia la sesión". `specs/nv-04 §3.2` lo interpreta como rechazo del servidor (401/422), no como falta de red. Requiere confirmación.
- **D17 · Backend fuera de horario.** Staging se apaga a las 22:00 y los fines de semana (en CloudFront responde 503 con HTML). La API local es `http://localhost:8080`, y la constitución prohíbe HTTP en claro. `AGENTS.md` cita HTTPS en `10.0.2.2:8443` con certificado de desarrollo, lo que exige confiar en esa CA solo en debug. Falta acordar cómo.
- **D18 · Permisos.** `08_Permisos` marca Ubicación como "Necesario", pero la constitución §6 dice que la app funciona sin ella. La navegación y `06_PermisoSys` necesitan ubicación precisa, y `docs/permissions-matrix.md` solo lista `ACCESS_COARSE_LOCATION`. Los contactos (`07`, `24`) usan `READ_CONTACTS`, que tampoco está en la matriz.
- **D19 · Ciudad al registrarse.** `05_Registro` muestra la ciudad con "Cambiar", pero `POST /usuarios` no la acepta. Propuesta: mandarla después con `PATCH /usuarios/yo` (`ciudad_id`). US-021 la pide en el paso de gustos si no hay ubicación: falta decidir en cuál de los dos va.
- **D20 · Correo sin verificar.** La API deja entrar con `correo_verificado:false` y la maqueta no muestra aviso ni bloqueo. Falta definir qué se restringe, y dónde se ofrece el reenvío.
- **D34 · Subir fotos.** `PATCH /usuarios/yo` recibe `foto_url` que empiece con https://, pero no hay endpoint para subir archivos. "Cambiar foto" (US-017) no se puede implementar así, ni las postales ni las fotos del negocio.
- **D35 · Descargar mis datos.** `POST /usuarios/exportaciones/descargar` devuelve JSON con un token de un solo uso. Falta decidir si se guarda con el selector de archivos del sistema (propuesta) o se abre en la web. Sin frame.

### Proceso

- **D21 · Nombres de carpeta de specs. Resuelta (2026-11-05).** La constitución §11 pide `/specs/nv-0X`. Se renombró `NNN-nombre` → `nv-(NNN+4)`: `000-arquitectura` → `nv-04`, `001-flujo-general` → `nv-05`, `002…034` → `nv-06…nv-38` y `035-ci-cd` → `nv-39`, y se actualizaron todas las referencias. Cada spec tiene `spec.md`, `plan.md`, `task.md` y `checklists/gates.md`. Choque pendiente con `main`: allí `nv-05` es "Acceso, registro y onboarding" (de Diego), aquí es "Flujo general"; `nv-04` coincide en tema (arquitectura). Se resuelve al fusionar.

### Resto de la app

- **D22 · Barra inferior inconsistente.** Hay tres versiones: Inicio/Explorar/+/Planes/Perfil (la mayoría), Inicio/Explorar/Planes/Amigos/Perfil (`24_MisPlanes`, `25_MisReservas`, `41_PlanesVacio`) e Inicio/Explorar/Planes/Cupones/Perfil (`29_Cupones`). `22_AmigosLista` resalta Inicio. Propuesta: la primera.
- **D23 · Numeración de frames.** Varios números se repiten con pantallas sin relación (`13_Mapa`/`13_Reacciones`, `45_NegOper`/`45_GrupoChat`, `87_NegGaleria`/`87_PushBloqueo`, entre otros), y faltan 02, 09, 37 y 59-67. `Memoria.md` ya tenía pendiente esta numeración única. Hay 162 frames con variantes frente a las 115 pantallas que cita `Memoria.md`.
- **D24 · Un solo botón naranja por pantalla.** Constitución §4. El botón + de la barra convive con otro naranja en `10_Inicio` (botón de IA) y `11_InicioAmigos` (Me apunto). Hay que aclarar si el + cuenta.
- **D25 · Mapas y navegación.** `13_Mapa`, `14_MapaFull`, `15_Llegar` y `18_Navegando` dibujan navegación paso a paso dentro de la app. Falta proveedor de mapas y de rutas (pendiente en `Memoria.md`) y sus licencias. Alternativa más barata: abrir la app de mapas del teléfono.
- **D26 · Pasarela de pago.** `35_Pasarela` captura la tarjeta en una pantalla propia, y `22_Reserva` dice "pasarela externa". Falta el proveedor. Capturar tarjeta en UI propia amplía el alcance PCI: propuesta, usar el SDK o la página del proveedor. Tampoco está clara la fórmula del anticipo ($74 con consumo de $420 y cupón de $50).
- **D27 · Cuenta de negocio.** `42_NegCuenta` crea una cuenta nueva y `85_Ajustes` tiene "Tengo un negocio". La API crea solo rol consumidor y `POST /usuarios/yo/modo` exige rol comerciante. Falta saber si es otra cuenta o la misma con otro rol, y cómo se otorga.
- **D28 · Tiempo real en chats.** Chats, votaciones y "En línea" necesitan WebSocket o sondeo. No está definido en la API, y el stack no incluye cliente propio más allá de OkHttp.
- **D29 · Generación del plan con IA.** `Generando 1…4` dibuja cuatro pasos. Falta saber si son reales (requieren sondeo o eventos del backend) o una animación, y si "Cancelar" detiene el trabajo en el servidor.
- **D30 · Deshacer al eliminar reseña.** `MisRes · confirmar eliminar` dice "no se puede deshacer", y `MisRes · reseña eliminada` ofrece "Deshacer".
- **D31 · Qué funciona sin red.** `89_SinConexion` lista planes y reservas guardados, con "Código QR guardado". Falta decidir qué se guarda en Room, y si el QR se genera en el teléfono o viene del servidor.
- **D32 · Lugares con y sin reserva.** `33_Detalle` ofrece "Reservar mesa", y `Detalle · Jardín del Cerro` y `Mercado de Tula` ofrecen "Cómo llegar". Hace falta un campo que indique si el lugar acepta reservas.
- **D33 · Datos fiscales y bancarios.** `46_NegVerif`, `89_NegBanco` y `90_NegFiscal` manejan CLABE, RFC, identificación y comprobantes. Falta definir almacenamiento, retención y si la app guarda algo. Propuesta: nada en Room, solo los últimos 4 dígitos.
- **D36 · Detalles de texto en la maqueta.** "más de 24 horas de anticipo" (`27_Cancelar`, `86_Ayuda`) parece "anticipación". Hay textos pegados ("Jorgeestuvo aquí", "términosy") y encimados (`17_PostDetalle`) que parecen artefactos de exportación. `DetCupon · Postre de cortesía` recorta "Gratis".
- **D37 · Check-in con pago pendiente.** `NegCheckin · Familia Ortega` permite "Confirmar llegada" con el anticipo sin cobrar. Falta la regla.
