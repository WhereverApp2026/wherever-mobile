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

## Lo que falta, en orden de prioridad

### 1. Subir todo a GitHub
- [x] Búsqueda (nv-19) de `wip/pausa` a `main`, con las 7 compuertas en verde.
- [ ] Probar en el teléfono la pestaña Personas de Buscar y la variante "solicitud enviada" (no había teléfono conectado el 2026-11-19 por la noche).
- [ ] PR de Dependabot #5, #6 y #7 (GitHub Actions): pedir `@dependabot rebase` y fusionarlos si la CI pasa. No pude comentar en los PR desde la sesión.
- [x] PR #23 (`actions/setup-java` 6.2.0): CI en verde, se integra.

### 2. Diseño normativo (todas las pantallas)
- [ ] Tokens en `core/designsystem`: colores, tipografía, espaciado (escala 4/8 dp), radios, elevaciones y formas, sacados de `analisis/guia_estilo_wherever.md` y de la maqueta. Norma en `docs/diseno.md`.
- [ ] Regla de detekt que falle con colores hex, `dp` o `sp` escritos a mano fuera de `core/designsystem`.
- [ ] Componentes únicos: botones, campos, tarjetas, barra superior, carga/error/vacío, diálogos y snackbars.
- [ ] Revisión pantalla por pantalla contra la maqueta, con la captura del teléfono junto a la de Figma en `docs/pruebas/`.
- [ ] Transiciones iguales en todo el grafo, skeletons al cargar y Atrás correcto.
- [ ] Accesibilidad: 48 dp, `contentDescription`, contraste y letra grande.
- [ ] Pruebas de captura (Roborazzi) de las pantallas principales.

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
| Bienvenida en horizontal: los íconos tapan el texto (nv-06) | abierto |
| Irregularidades de diseño entre pantallas (valores sueltos, componentes duplicados) | abierto: parte 2 |
| APK de release sin probar en el teléfono (aviso de R8, nv-41) | abierto |
| Búsqueda de personas daba 422 con menos de 3 caracteres | ✅ arreglado (valida en la app) |
| Gustos con alergias daba 422 | ✅ arreglado (nv-43) |
| Fila de solicitud ya respondida en Avisos sin mensaje | ✅ arreglado (nv-18) |

## Ramas y PR sin integrar

| Rama / PR | Estado | Motivo |
|---|---|---|
| `wip/pausa` | integrada en `main` (Búsqueda) | se puede borrar cuando Jorge lo confirme |
| #8 okhttp 5.5.1, #9 hilt 1.4.0, #10 navigation 2.10.2, #11 compose-bom 2026.09, #12 core-ktx 1.19.1 | no se integran | piden `compileSdk 37`; no hay SDK 37 en el equipo (AGENTS.md §5) |
| #5 setup-gradle 6.4.1, #6 checkout 7.0.1, #7 upload-artifact 7.0.2 | pendientes | CI roja sobre un `main` viejo; falta rebase |

## Entorno (para retomar rápido)

- JDK 17: `…/ceb6bb66-…/scratchpad/jdk17dl/jdk-17.0.20.1+1` (temporal; ver `docs/revision-jorge.md`).
- Teléfono Xiaomi: `adb shell appops set com.wherever.mobile 10021 allow` tras cada reinstalación; no bloquear la pantalla (tiene PIN).
- Cuentas QA en staging (`@example.com`); credenciales solo en el scratchpad, nunca en el repo.
