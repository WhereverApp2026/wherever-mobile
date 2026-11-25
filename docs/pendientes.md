# Pendientes — Wherever Móvil

> Actualizado: 2026-11-25. Una rama y un PR por spec. Pruebas que esperan el teléfono: `docs/pruebas-telefono.md`.
> Métrica: pantallas que funcionan de verdad / 162 frames de la maqueta (detalle y registro de cortes en `docs/avance.md`).
> Una pantalla cuenta si tiene código, llama a la API real, maneja carga, error y vacío, tiene pruebas y se probó en el teléfono.

## % real

**101 de 162 = 62,3 %** con la API real (2026-11-25, `main` con todas las ramas con avance integradas). Tabla por área en `docs/avance.md`.

## Lo que falta, por área (2026-11-25, para repartir)

Tabla por área en `docs/avance.md`. Prioridad: primero lo que ya tiene código en `main` y solo falta probar contra staging (suma rápido al %), luego lo que falta construir. Una línea = una tarea = una rama y un PR.

### 1. Negocios/Sello (0/27): el código está en `main`, falta probarlo
- P1 · Backend (Osmar): arreglar el 503 «expediente no disponible» de `POST /negocios/{id}/documentos` en staging; bloquea enviar el alta a revisión y todo el Modo negocio. Según el contrato, ese 503 sale «sin `DATA_KEYS` o sin bucket»: es configuración de staging, no código.
- Cómo probar el alta en staging (Osmar, 2026-11-25):
  - **409 `alta_cerrada`:** solo se suben documentos a un negocio en `borrador` o `rechazado`. Con uno ya aprobado (p. ej. «Restaurante Cafe Madero» de la cuenta QA) siempre da 409: hay que crear un alta nueva.
  - **422 `consentimiento_requerido`:** la cuenta debe haber aceptado el consentimiento `datos_negocio`.
  - **401 o 403 después de crear el negocio:** la app debe refrescar el token (`requiere_refresh: true`), porque ahí se agrega el rol `comerciante`. **Bug (2026-11-25):** nv-34 lee `requiere_refresh` (`BusinessOnboardingDraft.requiresRefresh`) pero nada lo usa, y `TokenRefreshAuthenticator` solo renueva ante 401: con el 403 del rol faltante el alta se atora. Arreglo: forzar el refresh tras crear el negocio; toca `core/network` (Issue a Jorge).
- P1 · Probar nv-34 contra staging: 42_NegCuenta → 47_NegListo con una cuenta QA (sin la ubicación real del teléfono de Jorge).
- P1 · Probar nv-35 contra staging: 48_NegPanel, 49_NegReservas, 50_NegCheckin (y variantes) y 56_NegStats con un negocio aprobado y una reserva de QA.
- P2 · Probar nv-36 contra staging: 51_NegResenas, 52_NegResp (con y sin IA) y NegMomentos.
- P2 · Probar nv-37 contra staging: 53_NegCupones, 54_NegCrear y 55_NegHoras.
- P2 · Probar nv-38 contra staging: 58_NegPerfil, 87/88 galería, 57_NegCobros (Stripe), 89_NegBanco y 90_NegFiscal.
- P3 · Probar 34_Sello con un negocio que tenga Sello en staging (hoy solo sale el vacío).
- P3 · Marcar en las specs nv-34, nv-35 y nv-37 los frames ya construidos (las casillas siguen sin marcar).

### 2. Social (32/40)
- P1 · Probar 47_PuntoMedio en el teléfono con dos cuentas QA en el mismo grupo.
- P2 · 42_Compartir: hoja para compartir plan o lugar.
- P2 · Probar 07_EncuentraAmigos y su variante «solicitud enviada» con una agenda de prueba (no la de Jorge).
- P3 · Variantes `ChatAmigo · *`, `GrupoChat · *` y `PerfilAmigo · *`: revisar contra Figma y probar.

### 3. Planes/IA (14/22)
- P1 · nv-30 El día del plan: 13_Mapa, 14_MapaFull, 15_Llegar y 18_Navegando con osmdroid (D25).
- P1 · Probar 26_CheckinOk en el teléfono con un check-in real dentro de un plan.
- P2 · nv-26: variantes `Vaca · *` (saldada, sin gastos) y probarlas.
- P2 · nv-23: 4.º paso de `Generando` y probarlo contra staging.
- P3 · nv-28 sin red: etiqueta de estado y planes en Room (`nv-56`).

### 4. Detalle de lugar (15/22)
- P1 · nv-27 pago: 35_Pasarela y 36_PagoError con Stripe (falta acordar la librería y una excepción de stack).
- P2 · nv-27: 38_CambiarHora y 27_Cancelar contra staging con una reserva QA.
- P2 · nv-27: variantes `DetReserva · *`.
- P3 · nv-21: la variante que falta de `Detalle · *` con un negocio real.

### 5. Inicio/Feed (9/15)
- P1 · Probar 29_PlanAmigo, `PlanAmigo · *` y 14_MeApunto con un momento de staging que tenga `plan_id`.
- P2 · 15_Crear: las 3 opciones que dicen «Próximamente».
- P3 · 10_Inicio: corazón de favoritos y paradas en las tarjetas (falta que la API del feed los mande).

### 6. Otros (1/6)
- P1 · nv-29 Cupones: código en `feat/nv-29-cupones` (2026-11-25). 29_Cupones probado en teléfono con la cartera vacía; 47_DetCupon y `DetCupon · *` esperan un cupón real en staging (crearlo pide Modo negocio). Ver `docs/pruebas-telefono.md` #26.

### 7. Técnico (no suma frames)
- Subir a SDK 37 + AGP 9.1 para poder integrar AGP 9.5.2 (#65), coil 3.6.3 (#63) y los PR #8–#12.
- Arreglar las 16 pruebas JVM que fallan por MockK (ByteBuddy no se adjunta a la JVM local).

## Pruebas con staging en el Xiaomi (2026-11-22, domingo con staging encendido)

- **Cuentas QA A y B** (datos de prueba creados por la API: check-in en Deportivo Hidalgo, reseña de B, dos planes de B con grupo e invitación a A).
- **nv-33 Ayuda (1/1):** buscar sin acentos («cupon» → cupón), acordeón, reporte. Se llega también desde los enlaces de términos de 05_Registro.
- **nv-32 Reseñas (8/8):** 77 con la visita real → 68 → 78 (cinco aspectos) → 69 publicada; 79 editar (`If-Match`) y eliminar con confirmación y «Reseña eliminada»; 80 con la reseña de B, Útil, filtro y Reportar.
- **nv-31 Recuerdos (13/13):** 76 vacío, 71 con permiso de cámara en contexto, 72 postal (Clásica y Polaroid), 73 álbum, 74 por plan (vacío), 75 compartir, descargar (MediaStore) y eliminar, «ya no está disponible», 70 publicar un momento (B lo ve en su feed). Las variantes `PostalVer · *` son el mismo componente con otros datos.
- **nv-25 (pruebas #9 y #10):** Mis grupos con dos invitaciones reales: «No voy» → `declinado`, «Unirme» → `confirmado`; chat en los dos sentidos (sondeo de 5 s); sin red el texto se conserva y se manda al volver.
- **Bugs encontrados y arreglados** (rama `feat/boceto2-iconos-fotos`): Ayuda sin sesión mostraba «Autenticación requerida» al reportar; Mis reseñas con doble margen arriba; «1 reseñas» y «1 postales» sin plural; el álbum no se recargaba al volver de guardar o eliminar; «Por plan» vacío quedaba en blanco; el sello cortaba «Whereve / r»; el texto rechazado por la moderación solo decía «Datos inválidos» escondido bajo los botones.
- **No se pudo en el teléfono:** Deshacer de «Reseña eliminada» (cada lectura de pantalla tarda más que los 5 s del aviso; lo cubre `MyReviewsViewModelTest`) y 74 con un recuerdo de plan (hace falta check-in dentro de un plan).

## Rediseño Boceto 2 (2026-11-22)

- Rama `feat/boceto2-rediseno`, guía `docs/diseno_boceto2.md`. Hecho: tokens exactos de Figma, logo nuevo, barra de 5 con "+" y hoja 15_Crear, "Continuar con Google" (avisa "llega pronto": la API no lo tiene) y campos de 52 dp.
- **No cambia el %:** 15_Crear funciona a medias (navegan "Armar un plan con IA" y "Planear con amigos"; las otras 3 dicen "Próximamente" hasta que existan sus pantallas).
- **Probado en el Xiaomi (sin servidor, de noche):** barra, hoja con scroll en horizontal, IA → Armar un plan, amigos → Mis grupos. Suite de UI en el teléfono: 38/43 en horizontal; en vertical solo falla `GroupsContentTest.chatEnviaReintentaYAvisaSinRed` (ver Bugs).
- **10_Inicio de Figma (rama `feat/boceto2-inicio`):** portada con la foto del primer lugar recomendado (sin foto, degradado azul) y degradado de Figma; chip de ciudad y campana en círculo translúcido; "¿Qué hacemos hoy, Osmar?"; buscador "Tacos, relax, aventura…" con chispa naranja → Armar un plan con IA; "Cerca de ti" en carrusel de tarjetas-foto de 160 × 184 ("Para ti"/"Cerca", nombre, "8 min a pie · 4.8") con "Ver todo" → Explorar; "Planes de tus amigos" (momentos con plan, "Ver más" → pestaña Amigos). Mantener presionada una tarjeta = "No me interesa" (con Deshacer). Compañía y "Para este momento" bajan al final; el chip de clima sale de la portada (`docs/revision-jorge.md`).
- **Falta del Boceto 2:** corazón de favoritos y paradas en las tarjetas de Inicio (la API del feed no trae esos datos), íconos SVG de Figma en el resto de la app, fotos en el resto de las tarjetas.

## nv-17 feed de amigos (2026-11-20, noche)

- Rama `feat/nv-17-feed` (sale de `wip/pausa`), PR a `main`. Compuertas locales en verde: ktlint, detekt, lint, unitarias + capturas, kover, debug y release, critic, sdd, specs, routes, strings, coverage 79,7 %, semgrep 0. Delta 27,5 % (advisory).
- **Probado en el teléfono (Xiaomi, staging):** cuenta A ve el momento de B en Amigos con foto real; abre 17_PostDetalle. `FriendsFeedTest` (vacío; Agregar → Solicitud enviada) verde en el teléfono.
- **Arreglos de esta vuelta:** botones de 29_PlanAmigo apilados ("Hacer mi versión" se cortaba); tinte de fondo mientras carga la foto del momento (se veía un hueco gris/blanco); plurales `many`; rutas `MomentRoute` y `FriendPlanRoute` en `docs/routes.md`.
- **Falta:** 29_PlanAmigo y 14_MeApunto en el teléfono: hace falta un momento de staging con `plan_id` (B publica un momento desde un plan público). Historias, reacciones y comentarios ya con la API real y probados (2026-11-24, #68).
- **Siguiente:** nv-25 Grupos y chats (27_AmigoOpc y 25_MiCodigo ya en `main`).
- **Protección de `main`:** regla en `.github/rulesets/main.json`; solo Osmar aprueba (ver `CONTRIBUTING.md`, "Aprobación").

## Reparto del equipo (desde 2026-11-22)

Reglas: `CONTRIBUTING.md`. Cada spec es una rama y un PR a `main`, aprobado por Osmar. Frames que faltan por construir al 2026-11-22, sin contar los PR abiertos.

| Responsable | Área | Specs | Faltan | Lista de trabajo |
|---|---|---|---|---|
| Jorge | Social y Explorar; compartido (theme y `core/designsystem`, navegación, cliente de API, CI) | nv-20, nv-21, nv-25 | 14 (+4 por probar) | `docs/equipo/jorge.md` |
| Diego | Cupones del negocio; cerrar #42, #45 y #48 | nv-37 | 3 | `docs/equipo/diego.md` |
| Borre | Planes (desde el 2026-11-22) y Modo negocio | nv-23, nv-24, nv-26, nv-30, nv-56; nv-35, nv-36, nv-38 | 30 | `docs/equipo/borre.md`, `docs/itinerario-planes.md` |
| Osmar | Reserva, pago y cupones; feed social; backend que bloquea; revisor único | nv-17, nv-27, nv-29 | 19 | `docs/equipo/osmar.md` |

Reparto anterior (tres personas, 2026-11-20), con lo que funciona por área:

| Responsable | Área | Specs | Frames | Funcionan | Lista de trabajo |
|---|---|---|---|---|---|
| Jorge | Cuenta y acceso | nv-06 a nv-16 | 17 | 16 (+1 local) | `docs/pendientes.md` |
| Jorge | Descubrir: Inicio, Búsqueda, Explorar, Favoritos | nv-17, nv-19, nv-21, nv-22 | 29 | 19 | `docs/pendientes.md` |
| Jorge | Social: Notificaciones, Amigos, Grupos y chats | nv-18, nv-20, nv-25 | 31 | 18 | `docs/pendientes.md` |
| Jorge | Planes: IA, detalle, Vaca, Mis planes, día del plan | nv-23, nv-24, nv-26, nv-28, nv-30 | 22 | 9 | `docs/pendientes.md` |
| Jorge | Reserva, pago y cupones | nv-27, nv-29 | 15 | 4 | `docs/pendientes.md` |
| Jorge | Compartido: theme y `core/designsystem`, navegación, cliente de API, CI | — | — | — | Issues de Osmar y Diego |
| Diego | Recuerdos, Reseñas y Ayuda | nv-31 a nv-33 | 22 | 22 | `docs/equipo/diego.md` |
| Osmar | Modo negocio | nv-34 a nv-38 | 26 | 0 | `docs/equipo/osmar.md` |

## Estado (2026-11-20)

- **Área 1 (Cuenta y acceso): 16/17.** Solo falta `83_Insignias`, bloqueada por backend (`GET /usuarios/yo/insignias` no existe). Todo lo demás tiene captura y se probó en el teléfono (Xiaomi 2207117BPG, Android 12) contra staging.
- **Área 2 (Descubrir): 14/29.** nv-19 Búsqueda y nv-22 Favoritos completas. Lo que falta está bloqueado:
  - nv-17 Inicio (12 frames: feed de amigos, historias, publicaciones, reacciones, Me apunto, hoja Crear, plan de amigo): `/feed`, `/publicaciones` e `/historias` dan 404 en staging (2026-11-20).
  - nv-21 `32_ExpMapa`: hecho con osmdroid (D25) y probado con staging el 2026-11-23. `34_Sello`: `GET /lugares` no filtra por Sello y la tarjeta no trae `sello_excelencia`. Staging ya tiene lugares reales (fichas probadas el 2026-11-23).
- **Pruebas en teléfono que quedan** (juntas, para cuando haya con qué):
  1. Diálogo de avisos de `08_Permisos` en Android 13+ (este teléfono es Android 12).
  2. `NuevaContrasena` con un enlace real (staging no manda correos).
  3. Foto en Editar perfil (cámara y galería) tras los últimos cambios.
  4. Ficha de lugar, reservas y Explorar con negocios reales (cuando se importe la semilla).
  5. APK de release en el teléfono (aviso de R8, nv-41).
- **Cuentas QA en staging:** A (teléfono), B (`qa_b92693`, amiga de A), C (`qa_c91961`, solicitud pendiente de A). Credenciales solo en el scratchpad local, nunca en el repo.

## Área 1 — Cuenta y acceso: inventario (2026-11-20, revisado en el código y en el teléfono)

| # | Frame | Pantalla / archivo | Estado | Falta para "terminada" |
|---|---|---|---|---|
| 1 | `01_Bienvenida` | `WelcomeScreen.kt` | funciona | — (versión mínima y mantenimiento por nv-16, `GET /configuracion`) |
| 2 | `03_Login` | `LoginScreen.kt` | funciona | prueba en teléfono (doble toque, giro, sin red) |
| 3 | `03_Recuperar` | `RecoverScreen.kt` | funciona | prueba en teléfono |
| 4 | `04_RevisaCorreo` | `CheckEmailScreen.kt` | funciona | prueba en teléfono |
| 5 | `NuevaContrasena` | `NewPasswordScreen.kt` | funciona | captura lista (normal y enlace vencido); prueba en teléfono con enlace real cuando staging mande correos |
| 6 | `05_Registro` | `RegisterScreen.kt` | funciona | — (prueba en teléfono 2026-11-20: fecha, correo disponible, términos) |
| 7 | `06_Gustos` | `TastesScreen.kt` | funciona | — (prueba en teléfono 2026-11-20) |
| 8 | `08_Permisos` | `PermissionsScreen.kt` | funciona | — (probada en teléfono Android 12, 2026-11-20; falta ver el diálogo de avisos en Android 13+) |
| 9 | `06_PermisoSys` | diálogo del sistema | hecha | se dispara desde `08_Permisos` y desde Inicio |
| 10 | `85_Ajustes` | `SettingsScreen.kt` | funciona | — (captura y prueba en teléfono 2026-11-20: el cambio persiste al reabrir) |
| 11 | `81_Perfil` | `ProfileScreen.kt` | funciona | — (arreglado el corte sobre la barra, nv-55) |
| 12 | `82_EditarPerfil` | `EditProfileScreen.kt` | funciona | prueba en teléfono de la foto |
| 13 | `83_Insignias` | `BadgesScreen.kt` | **bloqueo backend** | `GET /usuarios/yo/insignias` no existe (404 en staging, no está en el backend); vista previa y captura |
| 14 | `84_DatosPers` | unido a `EditProfileScreen.kt` (D09) | funciona | — |
| 15 | `28_PrivSocial` | `PrivacyScreen.kt` | funciona | — (captura y prueba en teléfono 2026-11-20) |
| 16 | `EliminarCuenta` | `DeleteAccountScreen.kt` | funciona | — (captura y prueba en teléfono 2026-11-20: ELIMINAR → Login) |
| 17 | `89_SinConexion` | `feature/status/` (capa en `MainActivity`) | hecha (2026-11-20) | — (probada en teléfono; "Disponible sin internet" espera Room de planes/reservas) |

Bugs conocidos del área: Bienvenida en horizontal (✅ nv-55); enlaces corridos (✅ nv-55); tema oscuro sin selección visible (✅ nv-55); Perfil cortado (✅ nv-55).

## Lo que faltaba al 2026-11-20 (histórico)

### 1. Subir todo a GitHub
- [x] Búsqueda (nv-19) de `wip/pausa` a `main`, con las 7 compuertas en verde.
- [x] Pestaña Personas de Buscar en el teléfono (2026-11-20): Amigos, Agregar y Solicitud enviada; la búsqueda no trae la relación y se cruza con amigos y enviadas.
- [x] PR de Dependabot #5, #6 y #7 (GitHub Actions): integrados juntos en `a333916`, CI de `main` en verde.
- [x] PR #23 (`actions/setup-java` 6.2.0): CI en verde, se integra.

### 2. Diseño normativo (todas las pantallas)
- [x] Tokens en `core/designsystem/Dimens.kt` (espaciado 4/8, radios, formas, trazos, elevaciones, tamaños) y norma en `docs/diseno.md` (nv-55). `analisis/` no existe: se usó la maqueta y `docs/figma-inventory.md`.
- [x] Regla de detekt `HardcodedDesignValue` (módulo `detekt-rules/`): 207 `dp` sueltos en 34 archivos pasados a tokens.
- [x] Componentes únicos (diálogo, botones de texto e ícono, snackbar, progreso, skeleton) y regla de Konsist que prohíbe los de Material en pantallas.
- [ ] Revisión pantalla por pantalla contra la maqueta: comparación maqueta | render en `docs/pruebas/comparacion/` para las 8 principales; capturas del teléfono de Inicio, Perfil y Mis planes en `docs/pruebas/nv55/` (2026-11-19). Falta el resto de pantallas.
- [x] Transiciones iguales en todo el grafo (`NavMotion`) y `ListSkeleton` en las 7 listas.
- [ ] Accesibilidad: 48 dp en botones de texto e ícono y filas de diálogo, descripción en la carga ✅; falta revisar contraste y letra grande en el teléfono.
- [x] Pruebas de captura (Roborazzi): 8 pantallas × claro, oscuro y chica con letra grande.

### 3. Terminar lo que está a medias
- [x] nv-11 Permisos en contexto (2/2), probado en teléfono 2026-11-20.
- [ ] nv-21 Explorar y lugares (5/8).
- [ ] nv-23 Crear plan con IA (4/6).
- [ ] nv-24 Plan: detalle y edición (3/5).
- [ ] nv-20 Amigos (12/15): 27_AmigoOpc y 25_MiCodigo (QR con ZXing) hechos. Falta 07_EncuentraAmigos (contactos: permiso `READ_CONTACTS` en la matriz) y variantes.
- [ ] nv-27 Reserva y pago (4/10): pago.
- [ ] nv-17 Inicio y feed (3/15): feed social.
- [x] nv-19 Búsqueda (4/4).
- [ ] nv-14 Insignias con API real (cuando exista el endpoint).

### 4. Lo que está en 0 %
- [x] nv-16 Sin conexión (1), probado en teléfono 2026-11-20
- [ ] nv-25 Compartir, grupos y chats (12): 44_MisGrupos y 45_GrupoChat funcionan (2 de 12)
- [ ] nv-26 Vaca virtual y gastos (4)
- [ ] nv-29 Cupones (5)
- [ ] nv-30 El día del plan (5)
- [x] nv-31 Recuerdos y postales (13)
- [x] nv-32 Reseñas (8)
- [x] nv-33 Ayuda (1)
- [ ] nv-34 a nv-38 Modo negocio (26)

## Endpoints que faltan o fallan

| Endpoint o servicio | Problema | Afecta | Mientras tanto |
|---|---|---|---|
| Negocios en staging | `/categorias` y `/lugares` vacíos; falta `POST /admin/negocios/importar` con `docs/semilla-negocios.csv` (cuenta de administrador) | Inicio, Explorar, ficha, reservas | Estado vacío |
| SMTP en staging | No salen los correos de verificación ni de recuperación | nv-08 | Pegar el enlace en "Revisa tu correo" |
| `APP_LINK_BASE` | Necesita dominio `https` con `/.well-known/assetlinks.json` | Enlaces del correo | `wherever://restablecer` |
| `GET /usuarios/yo/insignias` | No existe | Insignias (nv-14) | `LocalBadgesRepository` |
| Tipos de push | Faltan reserva, "pide tu reseña" y "Es hora de salir" | nv-18, nv-27, nv-30 | Destino genérico Avisos |
| `GET /inicio` | Exige `lat`/`lng` (422 sin ellos) aunque `docs/endpoints.md` no lo dice | Inicio sin ubicación | Zona de respaldo |
| `PATCH /usuarios/yo` | Exige `If-Match` (400 sin ella); no documentado | QA | Leer la versión antes |
| Límite de peticiones | Logins repetidos dan 429 | QA | Caché de tokens de 10 min |

## Bugs conocidos

| Bug | Estado |
|---|---|
| Bienvenida en horizontal: los íconos tapan el texto (nv-06) | ✅ arreglado: la ilustración solo se dibuja si cabe y el contenido se desplaza (captura `01_bienvenida_horizontal`) |
| Tema oscuro: pestaña activa de la barra inferior y del selector invisibles; enlaces en blanco (primario = `RouteBlueTint`) | ✅ arreglado: `RouteBlueLight`, `InkRaised`, `InkTrack` en la paleta oscura; `RouteBlue` fijo en textos → `colorScheme.primary` |
| Enlaces de texto corridos 12 dp respecto al margen de su columna (p. ej. "Buscar lugares", "Armar un plan") | ✅ arreglado: `WhereverTextLink` sin relleno lateral |
| Foto de perfil: Editar perfil mostraba iniciales aunque hubiera foto | ✅ arreglado (con prueba en `ProfileViewModelsTest`) |
| Irregularidades de diseño entre pantallas (valores sueltos, componentes duplicados) | ✅ arreglado (nv-55) |
| Línea del traslado descentrada respecto al círculo numerado (Detalle del plan) | ✅ arreglado (nv-55) |
| Barra de progreso distinta en Insignias y en la ficha | ✅ arreglado (`WhereverProgressBar`) |
| APK de release sin probar en el teléfono (aviso de R8, nv-41) | abierto |
| Búsqueda de personas daba 422 con menos de 3 caracteres | ✅ arreglado (valida en la app) |
| Gustos con alergias daba 422 | ✅ arreglado (nv-43) |
| Fila de solicitud ya respondida en Avisos sin mensaje | ✅ arreglado (nv-18) |
| Barra inferior estirada a toda la pantalla tras agregar el "+" (Boceto 2) | ✅ arreglado antes de integrar (`BottomBarCreateTest`) |
| Hoja inferior sin scroll: en horizontal las últimas opciones quedaban fuera | ✅ arreglado (`WhereverBottomSheet` con scroll) |
| Login: "¿No tienes cuenta?Crea una" sin espacio entre texto y enlace | arreglado (espacio de 4 dp) |
| Inicio en horizontal: la barra de navegación del sistema tapaba la campana y la chispa de IA (visto en el Xiaomi) | arreglado (márgenes laterales del Scaffold) |
| Inicio sin servidor: "Cerca de ti" con "Ver todo" quedaba sin nada debajo del aviso de error | arreglado (se oculta con error y sin lugares) |
| `GroupsContentTest.chatEnviaReintentaYAvisaSinRed` falla en el Xiaomi: el teclado tapa "No se envió" en la actividad de prueba | abierto (pasa en el emulador de la CI) |
| Pruebas de UI en el teléfono en horizontal: 4 fallan porque la pantalla queda baja (Fecha, Mi código, Grupos, Insignias) | correr con el teléfono en vertical |

## Ramas y PR sin integrar

| Rama / PR | Estado | Motivo |
|---|---|---|
| Todas las ramas salvo `main` (feat, fix, docs, spec, chore, integra y las de Dependabot de #11 y #12) | borradas (local y remoto) el 2026-11-19 | `git cherry main <rama>` vacío; las de Dependabot solo tenían el bump de sus PR cerrados. Solo queda `main` |
| #12 core-ktx 1.19.1 | **cerrado** el 2026-11-19 | `checkDebugAarMetadata` falla: exige `compileSdk 37` y AGP 9.1.0; el proyecto está en SDK 36 / AGP 9.0.1. Reabrir al subir a SDK 37 |
| #8 okhttp 5.5.1, #9 hilt 1.4.0, #10 navigation 2.10.2, #11 compose-bom 2026.09 | **cerrados** el 2026-11-19 | probados uno por uno en `main`: `assembleDebug` falla en `checkDebugAarMetadata`, exigen `compileSdk 37` y AGP 9.1.0 (hilt arrastra lifecycle 2.11.0; el BOM, material-ripple 1.12.1). Sin SDK 37 en el equipo (dl.google.com no responde). Retomar todos juntos al subir a SDK 37 + AGP 9.1 |
| #5 setup-gradle 6.4.1, #6 checkout 7.0.1, #7 upload-artifact 7.0.2 | integrados en `a333916` | CI de `main` en verde |

## Entorno (para retomar rápido)

- JDK 17: `…/ceb6bb66-…/scratchpad/jdk17dl/jdk-17.0.20.1+1` (temporal; ver `docs/revision-jorge.md`).
- Teléfono Xiaomi: `adb shell appops set com.wherever.mobile 10021 allow` tras cada reinstalación; no bloquear la pantalla (tiene PIN).
- Cuentas QA en staging (`@example.com`); credenciales solo en el scratchpad, nunca en el repo.
