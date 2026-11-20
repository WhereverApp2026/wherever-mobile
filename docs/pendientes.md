# Pendientes — Wherever Móvil

> Actualizado: 2026-11-19 (noche). Trabajo directo en `main`, un commit por parte.
> Métrica: pantallas que funcionan de verdad / 162 frames de la maqueta (detalle y registro de cortes en `docs/avance.md`).
> Una pantalla cuenta si tiene código, llama a la API real, maneja carga, error y vacío, tiene pruebas y se probó en el teléfono.

## % real

**54 de 162 = 33,3 %** con la API real, más 1 con datos locales (Insignias, sin endpoint) que no suma.

| Área | Specs | Frames | Funcionan | % |
|---|---|---|---|---|
| Cuenta y acceso | nv-06 a nv-16 | 17 | 14 (+1 local) | 82 % |
| Descubrir: Inicio, Búsqueda, Explorar, Favoritos | nv-17, nv-19, nv-21, nv-22 | 29 | 13 | 45 % |
| Social: Notificaciones, Amigos, Grupos y chats | nv-18, nv-20, nv-25 | 31 | 14 | 45 % |
| Planes: IA, detalle, Vaca, Mis planes, día del plan | nv-23, nv-24, nv-26, nv-28, nv-30 | 22 | 9 | 41 % |
| Reserva, pago y cupones | nv-27, nv-29 | 15 | 4 | 27 % |
| Después del plan: Recuerdos, Reseñas, Ayuda | nv-31 a nv-33 | 22 | 0 | 0 % |
| Modo negocio | nv-34 a nv-38 | 26 | 0 | 0 % |
| **Total** | | **162** | **54** | **33,3 %** |

## Área 1 — Cuenta y acceso: inventario (2026-11-19, revisado en el código)

| # | Frame | Pantalla / archivo | Estado | Falta para "terminada" |
|---|---|---|---|---|
| 1 | `01_Bienvenida` | `WelcomeScreen.kt` | a medias | Splash sin versión mínima ni mantenimiento (`GET /configuracion`, 426) |
| 2 | `03_Login` | `LoginScreen.kt` | funciona | prueba en teléfono (doble toque, giro, sin red) |
| 3 | `03_Recuperar` | `RecoverScreen.kt` | funciona | prueba en teléfono |
| 4 | `04_RevisaCorreo` | `CheckEmailScreen.kt` | funciona | prueba en teléfono |
| 5 | `NuevaContrasena` | `NewPasswordScreen.kt` | a medias | vista previa y captura |
| 6 | `05_Registro` | `RegisterScreen.kt` | funciona | prueba en teléfono |
| 7 | `06_Gustos` | `TastesScreen.kt` | funciona | prueba en teléfono |
| 8 | `08_Permisos` | `PermissionsScreen.kt` | hecha (2026-11-19) | prueba en teléfono |
| 9 | `06_PermisoSys` | diálogo del sistema | hecha | se dispara desde `08_Permisos` y desde Inicio |
| 10 | `85_Ajustes` | `SettingsScreen.kt` | a medias | vista previa y captura |
| 11 | `81_Perfil` | `ProfileScreen.kt` | funciona | — (arreglado el corte sobre la barra, nv-55) |
| 12 | `82_EditarPerfil` | `EditProfileScreen.kt` | funciona | prueba en teléfono de la foto |
| 13 | `83_Insignias` | `BadgesScreen.kt` | **bloqueo backend** | `GET /usuarios/yo/insignias` no existe (404 en staging, no está en el backend); vista previa y captura |
| 14 | `84_DatosPers` | unido a `EditProfileScreen.kt` (D09) | funciona | — |
| 15 | `28_PrivSocial` | `PrivacyScreen.kt` | a medias | vista previa y captura |
| 16 | `EliminarCuenta` | `DeleteAccountScreen.kt` | a medias | vista previa y captura |
| 17 | `89_SinConexion` | — | **falta** | pantalla completa con Reintentar (`GET /configuracion`) |

Bugs conocidos del área: Bienvenida en horizontal (✅ nv-55); enlaces corridos (✅ nv-55); tema oscuro sin selección visible (✅ nv-55); Perfil cortado (✅ nv-55).

## Lo que falta, en orden de prioridad

### 1. Subir todo a GitHub
- [x] Búsqueda (nv-19) de `wip/pausa` a `main`, con las 7 compuertas en verde.
- [ ] Probar en el teléfono la pestaña Personas de Buscar y la variante "solicitud enviada" (no había teléfono conectado el 2026-11-19 por la noche).
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
- [ ] nv-11 Permisos en contexto (1/2): 08_Permisos "Antes de empezar" tras Gustos, con el permiso de avisos.
- [ ] nv-21 Explorar y lugares (5/8).
- [ ] nv-23 Crear plan con IA (4/6).
- [ ] nv-24 Plan: detalle y edición (3/5).
- [ ] nv-20 Amigos (10/15): QR (requiere acordar la librería, constitución §3) y variantes.
- [ ] nv-27 Reserva y pago (4/10): pago.
- [ ] nv-17 Inicio y feed (3/15): feed social.
- [ ] nv-19 Búsqueda (3/4): variante "solicitud enviada" en el teléfono.
- [ ] nv-14 Insignias con API real (cuando exista el endpoint).

### 4. Lo que está en 0 %
- [ ] nv-16 Sin conexión (1)
- [ ] nv-25 Compartir, grupos y chats (12)
- [ ] nv-26 Vaca virtual y gastos (4)
- [ ] nv-29 Cupones (5)
- [ ] nv-30 El día del plan (5)
- [ ] nv-31 Recuerdos y postales (13)
- [ ] nv-32 Reseñas (8)
- [ ] nv-33 Ayuda (1)
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
