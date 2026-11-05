# Specs de Wherever Móvil

> Rector: `.specify/memory/constitution.md`. Todas las specs de producto están en 🚧 Borrador: falta el nombre del revisor. Salen del análisis de `maqueta/figma-frames/` (local, no se sube a Git) y de `docs/contexto_api_sprint1.md`.

## Índice

| Spec | Tema | Sprint 1 | Frames |
|---|---|---|---|
| [`nv-00…nv-03`](nv-00/spec.md) | Constitución, foundation, molde y puerta de calidad | Base | — |
| [`000-arquitectura`](000-arquitectura/spec.md) | Capas, red, tokens, DI, paquetes | Base | — |
| [`001-flujo-general`](001-flujo-general/spec.md) | Mapa de navegación, flujos de usuario y negocio | — | Todos |
| [`002-splash-arranque`](002-splash-arranque/spec.md) | Splash y bienvenida | Sí | 1 |
| [`003-login`](003-login/spec.md) | Iniciar sesión | Sí | 1 |
| [`004-recuperar-contrasena`](004-recuperar-contrasena/spec.md) | Recuperar contraseña | Sí | 3 |
| [`005-registro`](005-registro/spec.md) | Registro con correo | Sí | 1 |
| [`006-onboarding-gustos`](006-onboarding-gustos/spec.md) | Onboarding de gustos | Sí | 1 |
| [`007-permisos`](007-permisos/spec.md) | Permisos en contexto | — | 2 |
| [`008-sesion-tokens`](008-sesion-tokens/spec.md) | Sesión y tokens | Sí | 0 |
| [`009-ajustes-cerrar-sesion`](009-ajustes-cerrar-sesion/spec.md) | Ajustes y cerrar sesión | Sí | 1 |
| [`010-perfil`](010-perfil/spec.md) | Perfil, edición e insignias | Sí | 4 |
| [`011-privacidad-datos`](011-privacidad-datos/spec.md) | Privacidad, mis datos y eliminar cuenta | Sí | 2 |
| [`012-sin-conexion`](012-sin-conexion/spec.md) | Sin conexión | Sí | 1 |
| [`013-inicio-feed`](013-inicio-feed/spec.md) | Inicio y feed | — | 15 |
| [`014-notificaciones`](014-notificaciones/spec.md) | Notificaciones | — | 4 |
| [`015-busqueda`](015-busqueda/spec.md) | Búsqueda | — | 4 |
| [`016-amigos`](016-amigos/spec.md) | Amigos | — | 15 |
| [`017-explorar-lugares`](017-explorar-lugares/spec.md) | Explorar y lugares | — | 8 |
| [`018-favoritos`](018-favoritos/spec.md) | Favoritos | — | 2 |
| [`019-crear-plan-ia`](019-crear-plan-ia/spec.md) | Crear plan con IA | — | 6 |
| [`020-plan-detalle-edicion`](020-plan-detalle-edicion/spec.md) | Plan: detalle y edición | — | 5 |
| [`021-grupos-chat`](021-grupos-chat/spec.md) | Compartir, grupos y chats | — | 12 |
| [`022-vaca-gastos`](022-vaca-gastos/spec.md) | Vaca virtual y gastos | — | 4 |
| [`023-reserva-pago`](023-reserva-pago/spec.md) | Reserva y pago | — | 10 |
| [`024-mis-planes`](024-mis-planes/spec.md) | Mis planes | — | 2 |
| [`025-cupones`](025-cupones/spec.md) | Cupones | — | 5 |
| [`026-dia-del-plan`](026-dia-del-plan/spec.md) | El día del plan | — | 5 |
| [`027-recuerdos-postales`](027-recuerdos-postales/spec.md) | Recuerdos y postales | — | 13 |
| [`028-resenas`](028-resenas/spec.md) | Reseñas | — | 8 |
| [`029-ayuda`](029-ayuda/spec.md) | Ayuda | — | 1 |
| [`030-negocio-registro`](030-negocio-registro/spec.md) | Negocio: registro | — | 6 |
| [`031-negocio-panel-reservas`](031-negocio-panel-reservas/spec.md) | Negocio: panel, reservas y check-in | — | 8 |
| [`032-negocio-resenas`](032-negocio-resenas/spec.md) | Negocio: reseñas y momentos | — | 3 |
| [`033-negocio-cupones-horas`](033-negocio-cupones-horas/spec.md) | Negocio: cupones y cupo | — | 3 |
| [`034-negocio-perfil-cobros`](034-negocio-perfil-cobros/spec.md) | Negocio: perfil, fotos y cobros | — | 6 |
| [`035-ci-cd`](035-ci-cd/spec.md) | CI/CD: gates del pull request | — | — |

Inventario: **162 frames** de pantalla, que son 135 pantallas distintas al agrupar variantes, y 36 anotaciones `label · …` que no son pantallas.

## Pantallas que ya consumen la API

Implementadas el 2026-11-04 y probadas ese día contra staging, con una cuenta de prueba, en un teléfono Samsung SM-A346M (Android 16), en tema claro y oscuro. **No se han probado en emulador.** Lo que faltó probar está en cada spec.

| Pantalla | Frame | Spec | Endpoints | Estado |
|---|---|---|---|---|
| Bienvenida y arranque | `01_Bienvenida` | 002 | `GET /usuarios/yo` (si hay sesión) | ✅ |
| Iniciar sesión | `03_Login` | 003 | `POST /auth/login`, `GET /usuarios/yo` | ✅ |
| Registro | `05_Registro` | 005 | `GET /usuarios/correo-disponible`, `POST /usuarios`, `GET /usuarios/yo` | ✅ |
| Recuperar contraseña | `03_Recuperar` | 004 | `POST /auth/recuperar` | ✅ |
| Revisa tu correo | `04_RevisaCorreo` | 004 | `POST /auth/recuperar`, `POST /auth/reenviar-verificacion` | ✅ |
| Gustos | `06_Gustos` | 006 | `PUT /usuarios/yo/gustos`, `PATCH /usuarios/yo/preferencias`, `PATCH /usuarios/yo`, `GET /ciudades/autocompletar` | ✅ |
| Perfil | `81_Perfil` | 010 | `GET /usuarios/yo` | ✅ |
| Editar perfil | `82_EditarPerfil`, `84_DatosPers` | 010 | `PATCH /usuarios/yo` con `If-Match`, `GET /ciudades/autocompletar` | ✅ |
| Ajustes | `85_Ajustes` | 009 | `PATCH /usuarios/yo/preferencias`, `PATCH /usuarios/yo/privacidad`, `POST /auth/logout` | ✅ |
| Sesiones abiertas | Sin frame | 009 | `GET /auth/sesiones`, `DELETE /auth/sesiones/{id}`, `POST /auth/logout-todos` | ✅ |
| Privacidad | `28_PrivSocial` | 011 | `PATCH /usuarios/yo/privacidad` | ✅ |
| Eliminar cuenta | `EliminarCuenta` | 011 | `DELETE /usuarios/yo` | ✅ |
| Eliminación pendiente | Sin frame | 011 | `POST /usuarios/yo/cancelar-eliminacion`, `POST /auth/logout` | ✅ |
| Inicio provisional | Parte de `10_Inicio` | 013 | Lee el perfil de Room | Provisional |
| Sesión y tokens | Sin pantalla | 008 | `POST /auth/refresh` | ✅ |

Pendiente del Sprint 1: `NuevaContrasena` y verificar correo por enlace (D11), Sin conexión (012), Descargar datos y Borrar historial (D35).

## Pantalla → endpoints

"Pendiente de backend" significa que no hay endpoint publicado en la API v1. El detalle de cada caso está en la spec.

| Frame | Spec | Endpoints |
|---|---|---|
| `01_Bienvenida` | 002 | `GET /usuarios/yo` |
| `03_Login` | 003 | `POST /auth/login` |
| `03_Recuperar` | 004 | `POST /auth/recuperar` |
| `04_RevisaCorreo` | 004 | `POST /auth/recuperar` |
| `NuevaContrasena` | 004 | `POST /auth/restablecer` |
| `05_Registro` | 005 | `GET /usuarios/correo-disponible`<br>`POST /usuarios`<br>`GET /ciudades/autocompletar` |
| `06_Gustos` | 006 | `PUT /usuarios/yo/gustos`<br>`PATCH /usuarios/yo/preferencias`<br>`GET /ciudades/autocompletar`<br>`PATCH /usuarios/yo` |
| `08_Permisos` | 007 | `PATCH /usuarios/yo/privacidad` |
| `06_PermisoSys` | 007 | Pendiente de backend |
| `85_Ajustes` | 009 | `POST /auth/logout`<br>`PATCH /usuarios/yo/preferencias`<br>`PATCH /usuarios/yo/privacidad`<br>`PATCH /usuarios/yo`<br>`GET /ciudades/autocompletar` |
| `81_Perfil` | 010 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `82_EditarPerfil` | 010 | `PATCH /usuarios/yo`<br>Parte pendiente de backend |
| `83_Insignias` | 010 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `84_DatosPers` | 010 | `PATCH /usuarios/yo`<br>`PUT /usuarios/yo/correo`<br>`PUT /usuarios/yo/telefono`<br>`GET /ciudades/autocompletar`<br>Parte pendiente de backend |
| `28_PrivSocial` | 011 | `PATCH /usuarios/yo/privacidad`<br>`POST /usuarios/yo/exportar`<br>`DELETE /usuarios/yo/historial`<br>Parte pendiente de backend |
| `EliminarCuenta` | 011 | `DELETE /usuarios/yo`<br>`POST /usuarios/yo/cancelar-eliminacion`<br>Parte pendiente de backend |
| `89_SinConexion` | 012 | `GET /health` |
| `10_Inicio` | 013 | Pendiente de backend |
| `11_InicioAmigos` | 013 | Pendiente de backend |
| `12_InicioMas` | 013 | Pendiente de backend |
| `InicioMas · solicitud enviada` | 013 | Pendiente de backend |
| `13_Reacciones` | 013 | Pendiente de backend |
| `Reacciones · solicitud enviada` | 013 | Pendiente de backend |
| `14_MeApunto` | 013 | Pendiente de backend |
| `15_Crear` | 013 | Pendiente de backend |
| `16_Historia` | 013 | Pendiente de backend |
| `Historia · *` | 013 | Pendiente de backend |
| `17_PostDetalle` | 013 | Pendiente de backend |
| `29_PlanAmigo` | 013 | Pendiente de backend |
| `PlanAmigo · *` | 013 | Pendiente de backend |
| `18_Notif` | 014 | Pendiente de backend |
| `Notif · solicitud aceptada` | 014 | Pendiente de backend |
| `87_PushBloqueo` | 014 | Pendiente de backend |
| `88_PushBanner` | 014 | Pendiente de backend |
| `19_Buscar` | 015 | Pendiente de backend |
| `20_BuscarPersonas` | 015 | Pendiente de backend |
| `BuscarPersonas · solicitud enviada` | 015 | Pendiente de backend |
| `21_SinResultados` | 015 | Pendiente de backend |
| `07_EncuentraAmigos` | 016 | Pendiente de backend |
| `EncuentraAmigos · solicitud enviada` | 016 | Pendiente de backend |
| `22_AmigosLista` | 016 | Pendiente de backend |
| `23_AmigosSolic` | 016 | Pendiente de backend |
| `24_AgregarAmigo` | 016 | Pendiente de backend |
| `AgregarAmigo · solicitud enviada` | 016 | Pendiente de backend |
| `25_MiCodigo` | 016 | `GET /usuarios/yo`<br>Parte pendiente de backend |
| `26_PerfilAmigo` | 016 | Pendiente de backend |
| `PerfilAmigo · *` | 016 | Pendiente de backend |
| `27_AmigoOpc` | 016 | Pendiente de backend |
| `30_Explorar` | 017 | Pendiente de backend |
| `31_Filtros` | 017 | Pendiente de backend |
| `32_ExpMapa` | 017 | Pendiente de backend |
| `33_Detalle` | 017 | Pendiente de backend |
| `Detalle · *` | 017 | Pendiente de backend |
| `34_Sello` | 017 | Pendiente de backend |
| `35_Favoritos` | 018 | Pendiente de backend |
| `36_FavVacio` | 018 | Pendiente de backend |
| `IAPalabra` | 019 | Pendiente de backend |
| `17_AjustaPlan` | 019 | Pendiente de backend |
| `Generando *` | 019 | Pendiente de backend |
| `39_Plan` | 020 | Pendiente de backend |
| `Plan · *` | 020 | Pendiente de backend |
| `40_Cambiar` | 020 | Pendiente de backend |
| `41_AgregarParada` | 020 | Pendiente de backend |
| `CrearPlan` | 020 | Pendiente de backend |
| `42_Compartir` | 021 | Pendiente de backend |
| `43_CrearGrupo` | 021 | Pendiente de backend |
| `44_MisGrupos` | 021 | Pendiente de backend |
| `45_GrupoChat` | 021 | Pendiente de backend |
| `GrupoChat · *` | 021 | Pendiente de backend |
| `46_Grupo` | 021 | Pendiente de backend |
| `47_PuntoMedio` | 021 | Pendiente de backend |
| `48_Votar` | 021 | Pendiente de backend |
| `ChatAmigo` | 021 | Pendiente de backend |
| `ChatAmigo · *` | 021 | Pendiente de backend |
| `InvitarPlan` | 021 | Pendiente de backend |
| `28_Vaca` | 022 | Pendiente de backend |
| `Vaca · *` | 022 | Pendiente de backend |
| `45_AgregarGasto` | 022 | Pendiente de backend |
| `22_Reserva` | 023 | Pendiente de backend |
| `35_Pasarela` | 023 | Pendiente de backend |
| `36_PagoError` | 023 | Pendiente de backend |
| `23_Confirmacion` | 023 | Pendiente de backend |
| `25_MisReservas` | 023 | Pendiente de backend |
| `26_DetReserva` | 023 | Pendiente de backend |
| `DetReserva · *` | 023 | Pendiente de backend |
| `27_Cancelar` | 023 | Pendiente de backend |
| `38_CambiarHora` | 023 | Pendiente de backend |
| `24_MisPlanes` | 024 | Pendiente de backend |
| `41_PlanesVacio` | 024 | Pendiente de backend |
| `29_Cupones` | 025 | Pendiente de backend |
| `47_DetCupon` | 025 | Pendiente de backend |
| `DetCupon · *` | 025 | Pendiente de backend |
| `13_Mapa` | 026 | Pendiente de backend |
| `14_MapaFull` | 026 | Pendiente de backend |
| `15_Llegar` | 026 | Pendiente de backend |
| `18_Navegando` | 026 | Pendiente de backend |
| `26_CheckinOk` | 026 | Pendiente de backend |
| `70_CrearPub` | 027 | Pendiente de backend |
| `71_Camara` | 027 | Pendiente de backend |
| `72_Postal` | 027 | Pendiente de backend |
| `73_Album` | 027 | Pendiente de backend |
| `74_RecPlan` | 027 | Pendiente de backend |
| `75_PostalVer` | 027 | Pendiente de backend |
| `PostalVer · *` | 027 | Pendiente de backend |
| `76_RecVacio` | 027 | Pendiente de backend |
| `68_PostVisita` | 028 | Pendiente de backend |
| `69_Publicado` | 028 | Pendiente de backend |
| `77_ResPend` | 028 | Pendiente de backend |
| `78_ResCom` | 028 | Pendiente de backend |
| `79_MisRes` | 028 | Pendiente de backend |
| `MisRes · *` | 028 | Pendiente de backend |
| `80_ResLugar` | 028 | Pendiente de backend |
| `86_Ayuda` | 029 | Pendiente de backend |
| `42_NegCuenta` | 030 | `POST /usuarios`<br>Parte pendiente de backend |
| `43_NegRegistro` | 030 | Pendiente de backend |
| `44_NegUbic` | 030 | Pendiente de backend |
| `45_NegOper` | 030 | Pendiente de backend |
| `46_NegVerif` | 030 | Pendiente de backend |
| `47_NegListo` | 030 | Pendiente de backend |
| `48_NegPanel` | 031 | Pendiente de backend |
| `49_NegReservas` | 031 | Pendiente de backend |
| `50_NegCheckin` | 031 | Pendiente de backend |
| `NegCheckin · *` | 031 | Pendiente de backend |
| `56_NegStats` | 031 | Pendiente de backend |
| `51_NegResenas` | 032 | Pendiente de backend |
| `52_NegResp` | 032 | Pendiente de backend |
| `NegMomentos` | 032 | Pendiente de backend |
| `53_NegCupones` | 033 | Pendiente de backend |
| `54_NegCrear` | 033 | Pendiente de backend |
| `55_NegHoras` | 033 | Pendiente de backend |
| `58_NegPerfil` | 034 | `POST /usuarios/yo/modo`<br>`POST /auth/logout`<br>Parte pendiente de backend |
| `87_NegGaleria` | 034 | Pendiente de backend |
| `88_NegAgregarFoto` | 034 | Pendiente de backend |
| `57_NegCobros` | 034 | Pendiente de backend |
| `89_NegBanco` | 034 | Pendiente de backend |
| `90_NegFiscal` | 034 | Pendiente de backend |
| Sin pantalla (sesión) | 008 | `POST /auth/refresh` |

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
- **D16 · Refresh con fallo transitorio.** Constitución §6: "si el refresh falla, se limpia la sesión". `specs/000-arquitectura §3.2` lo interpreta como rechazo del servidor (401/422), no como falta de red. Requiere confirmación.
- **D17 · Backend fuera de horario.** Staging se apaga a las 22:00 y los fines de semana. La API local es `http://localhost:8080`, y la constitución prohíbe HTTP en claro. `AGENTS.md` cita HTTPS en `10.0.2.2:8443` con certificado de desarrollo, lo que exige confiar en esa CA solo en debug. Falta acordar cómo.
- **D18 · Permisos.** `08_Permisos` marca Ubicación como "Necesario", pero la constitución §6 dice que la app funciona sin ella. La navegación y `06_PermisoSys` necesitan ubicación precisa, y `docs/permissions-matrix.md` solo lista `ACCESS_COARSE_LOCATION`. Los contactos (`07`, `24`) usan `READ_CONTACTS`, que tampoco está en la matriz.
- **D19 · Ciudad al registrarse.** `05_Registro` muestra la ciudad con "Cambiar", pero `POST /usuarios` no la acepta. Propuesta: mandarla después con `PATCH /usuarios/yo` (`ciudad_id`). US-021 la pide en el paso de gustos si no hay ubicación: falta decidir en cuál de los dos va.
- **D20 · Correo sin verificar.** La API deja entrar con `correo_verificado:false` y la maqueta no muestra aviso ni bloqueo. Falta definir qué se restringe, y dónde se ofrece el reenvío.
- **D34 · Subir fotos.** `PATCH /usuarios/yo` recibe `foto_url` que empiece con https://, pero no hay endpoint para subir archivos. "Cambiar foto" (US-017) no se puede implementar así, ni las postales ni las fotos del negocio.
- **D35 · Descargar mis datos.** `POST /usuarios/exportaciones/descargar` devuelve JSON con un token de un solo uso. Falta decidir si se guarda con el selector de archivos del sistema (propuesta) o se abre en la web. Sin frame.

### Proceso

- **D21 · Nombres de carpeta de specs.** La constitución §11 pide `/specs/nv-0X`; el encargo pide `specs/000-…` y `specs/001-…` (formato Spec Kit). Se siguió el encargo, y `nv-00…nv-03` quedan como están. Hay que enmendar §11 o renombrar.

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
