# NV-37 — Negocio: cupones y cupo

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 11 · Negocio. **Responsable: Osmar.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Crear y administrar cupones y ajustar el cupo de reservaciones por horario.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como dueño quiero manejar mis cupones.

- [x] `53_NegCupones`: Activos, Programados, Terminados (`GET /negocios/{id}/cupones?estado=`), interruptor activo/pausado (`PATCH` con `If-Match`) y usos.
- [x] `54_NegCrear`: tipo (2x1, porcentaje, monto, regalo), valor, vigencia, días y horas, límite de usos y vista previa → `POST /negocios/{id}/cupones`; Sugerir título con IA.
- [ ] Con canjes, solo se cambian vigencia, límite y estado (409 `con_canjes` explicado); eliminar con canjes lo pausa.

**HU.** Como dueño quiero ajustar el cupo.

- [x] `55_NegHoras`: disponibilidad por hora del día elegido (`GET /lugares/{id}/disponibilidad`) en barras.
- [ ] Cambiar el cupo por horario y la duración del turno con `PUT /negocios/{id}/reservas-config` (`If-Match`); cerrar horas con bloqueos (nv-35).

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `53_NegCupones` | Activos, Programados, Terminados; tarjetas con interruptor y usos; Crear cupón. |
| `54_NegCrear` | Tipo, descuento, cuándo aplica, límite de usos, vista previa, Publicar cupón. |
| `55_NegHoras` | Día de la semana; cupo por hora con barras; leyenda; Guardar cambios. |

Colores, tipografía, espacios y componentes solo desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | `ListSkeleton` en listas; botón con `loading` en envíos. |
| Vacío | Texto y acción propios de la pantalla (nunca una lista en blanco). |
| Error | `ErrorBanner` con el mensaje de `AppError` y Reintentar; lo escrito no se pierde. |
| Sin red | La pantalla muestra su error de red; la capa global `89_SinConexion` (nv-16) avisa sola. |
| Éxito | Datos de la API; envíos con confirmación (`WhereverSnackbar`). |

### Decisiones tomadas (revisables en `docs/revision-jorge.md`)

- Modo negocio vive en `feature/business` con su propio `NavHost` interno y su barra (Panel, Reservas, Cupones, Reseñas, Perfil) armada con componentes de `core/designsystem`. En el grafo principal solo se registra una ruta, `BusinessHomeRoute` (una línea en `WhereverNavHost`, revisada por Jorge).
- La API tiene un solo `capacidad_por_horario` para todos los turnos: `55_NegHoras` muestra la ocupación por hora (lectura) y edita ese valor único; un cupo distinto por hora no existe (D-37a).
- Validar un cupón en mostrador (`POST .../cupones/validar`) va en la tarjeta de check-in de nv-35 como «Validar cupón» por código.

### Decisiones al implementar (2026-11-22, Diego)

- **D-37b Pestaña Cupones** en la barra del negocio (nv-35). 55_NegHoras entra desde «Horas y cupo» en el Panel mientras no exista 58_NegPerfil (nv-38).
- **D-37c Pestañas por estado del cupón:** se pide la lista una vez y se agrupa en la app (activo y pausado = Activos; programado = Programados; lo demás = Terminados), porque la doc no lista los valores de `?estado=`.
- **D-37d Título:** la API lo pide y el frame no tiene campo; 54 agrega «Título del cupón» con «Sugerir título con IA». La vigencia empieza hoy y termina el día elegido (30 días por omisión).
- **D-37e Configuración de reservas:** no hay un `GET`; se lee `reservas_config` de `GET /negocios/mios` y el `PUT` devuelve los demás campos tal cual (anticipo, políticas). Sin configuración, 55 lo explica.
- **D-37f Validar cupón** (`POST cupones/validar`) no se agrega aquí: según la spec va en la tarjeta de check-in de nv-35.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Barra de negocio → `NegCupones` → `NegCrear`.
- `NegPerfil` o `NegPanel` → `NegHoras`.

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| POST | `/negocios/{id}/cupones` | con `{titulo (1–40), tipo (porcentaje, monto, dos_por_uno o regalo), valor?, condiciones? (≤ 200), vigencia_inicio, vigencia_fin (RFC 3339), relampago?, dias_validos?… | JWT dueño del negocio | 201/400/401/403/404/422/429 |
| GET | `/negocios/{id}/cupones` | con `?estado=&cursor=&limite=` → `PageResult[cupón con limite_usos, usos_actuales, vistas, guardados, version]` del más nuevo al más viejo | JWT dueño del negocio | 200/401/403/404/422 |
| GET | `/negocios/{id}/cupones/{cid}/estadisticas` | con `?desde=&hasta=` (AAAA-MM-DD, días UTC, 30 por omisión, 90 máximo) → `{cupon_id, dias[{dia, vistas, guardados, canjes}], totales}` de `cupones_metricas` | JWT dueño del negocio | 200/401/403/404/422 |
| PATCH | `/negocios/{id}/cupones/{cid}` | con `If-Match` y los campos a cambiar (`estado` solo activo o pausado) → cupón | JWT dueño del negocio | 200/400/401/403/404/409/422 |
| DELETE | `/negocios/{id}/cupones/{cid}` | con `If-Match` → 204 sin cuerpo | JWT dueño del negocio | 204/400/401/403/404/409 |
| POST | `/negocios/{id}/cupones/sugerir-titulo` | con `{tipo, valor?, descripcion? (≤ 200)}` → `{titulo}` (1–40, sin correos, teléfonos ni enlaces) con la `ia.Guardia` de nv-26 a nv-28 | JWT dueño del negocio | 200/400/401/403/404/422/429/503 |
| POST | `/negocios/{id}/cupones/validar` | con `{codigo}` → 200 `{valido: true, cupon, repetido?}` o 409 `{valido: false, motivo}` (`ya_usado`, `vencido`, `otro_negocio`, `fuera_de_horario`, `agotado` o `no_exi… | JWT dueño del negocio | 200/400/401/403/404/409/422/429 |
| GET | `/lugares/{id}/disponibilidad` | con `?fecha=AAAA-MM-DD&personas=1..20` → `{fecha, horarios[{hora, disponible, lugares_libres}]}` | no | 200/404/422 |
| PUT | `/negocios/{id}/reservas-config` | con `If-Match` y `{acepta_reservas, capacidad_por_horario (1–500), duracion_turno_min (15–240, múltiplo de 15), anticipo{tipo: fijo, porcentaje o por_persona, valor},… | JWT dueño | 200/400/401/403/404/409/422 |
| POST | `/negocios/{id}/bloqueos` | con `{desde, hasta, motivo? (evento_privado, lleno, otro)}` → 201 `{id, desde, hasta, motivo}` | JWT dueño del negocio | 201/400/401/403/404/422 |

## 4. Modelo de datos

Sin Room nuevo.

## 5. Seguridad y privacidad

- El código de canje solo se muestra; nunca se guarda ni se registra en logs.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `CouponFormViewModel`: reglas por tipo (porcentaje 1–100, monto > 0), vigencia fin > inicio, título 1–40.
- Unit: `CouponsViewModel`: interruptor con 409 `con_canjes`.
- Unit: `CapacityViewModel`: múltiplos de 15 y 1–500.
- Data: MockWebServer: `POST cupones`, `PATCH` con `If-Match`, `PUT reservas-config` con `If-Match` (422 `cobros_no_activos`).
- Security: La validación de cupón no registra el código en logs.
- UI: Compose: la vista previa del cupón refleja el tipo y el valor.
- Capturas Roborazzi de cada frame (claro, oscuro, chica con letra grande). Cobertura ≥ 45 %.

### Definición de "terminado" (DoD)

Una pantalla cuenta como terminada solo si cumple todo (`docs/avance.md`):

- [ ] Se ve igual al frame de Figma en claro y oscuro: tokens de `core/designsystem` (sin `dp`, colores ni tamaños sueltos; la regla `HardcodedDesignValue` lo vigila) y componentes `Wherever*`.
- [ ] Llama a la API real de la tabla de endpoints; nada de datos locales ni simulados.
- [ ] Tiene los estados cargando, vacío, error con Reintentar y sin red (`89_SinConexion` lo maneja la app; la pantalla muestra su propio error).
- [ ] Textos en `strings.xml`; `testTag` en lo que se prueba; áreas táctiles de 48 dp; funciona con letra grande y en pantalla chica.
- [ ] Pruebas unitarias del ViewModel, de datos (MockWebServer) y de UI (Compose), y su captura en `ScreenshotTest` (claro, oscuro, chica con letra grande).
- [ ] Ruta en `docs/routes.md`; si pide un permiso, fila en `docs/permissions-matrix.md`.
- [ ] Las seis compuertas en verde, una por una (`CONTRIBUTING.md`), y probada en un teléfono o emulador contra staging.

## 7. Plan de entrega

Rama `feat/nv-37-business` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Implementada el 2026-11-22 por Diego (reparto nuevo) en `feat/nv-37-business`, sobre nv-35; sin probar contra staging.
