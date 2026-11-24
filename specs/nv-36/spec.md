# NV-36 — Negocio: reseñas y momentos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 11 · Negocio. **Responsable: Osmar.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Leer y responder las reseñas del negocio, ver el avance del Sello y agradecer o destacar los momentos de los clientes.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como dueño quiero responder reseñas.

- [x] `51_NegResenas`: resumen (`GET /negocios/{id}/resenas/resumen?periodo=mes|trimestre`) con promedio, aspectos y distribución; lista con `GET /lugares/{id}/resenas`.
- [x] `52_NegResp`: Respuesta rápida = `POST .../sugerir-respuesta` (IA); texto 1–500 → `PUT .../respuesta`; moderación rechazada = 422 explicado.

**HU.** Como dueño quiero ver el Sello y los momentos.

- [x] Avance del Sello con `avance_sello` del panel (criterios con meta y si cumple).
- [x] `NegMomentos`: Nuevos y Destacados (`GET /negocios/{id}/momentos?destacados=`); Agradecer y Destacar con `PATCH`.

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `51_NegResenas` | Sello con requisitos, Descargar insignia digital, rubros, reseñas, Momentos de clientes. |
| `52_NegResp` | Reseña, Respuesta rápida, texto, aviso de Sello, Publicar respuesta. |
| `NegMomentos` | Nuevos y Destacados; tarjetas con Agradecer; aviso de privacidad. |

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
- Descargar insignia digital: `GET /negocios/{id}/sello/insignia` está diferido en el backend; el botón no se dibuja (D-36a).
- La maqueta limita la respuesta a 300; la API a 500. Se usa 500.

### Decisiones al implementar (2026-11-23, Diego)

- **D-36b Pestaña Reseñas** en la barra del negocio (después de Cupones). 52_NegResp y NegMomentos son destinos internos; 52 recibe la reseña como argumentos de la ruta porque no hay `GET` de una sola reseña.
- **D-36c Respuestas rápidas:** los chips de la maqueta ponen una plantilla editable en el campo; «Sugerir con IA» llama a `sugerir-respuesta` y deja el texto para revisarlo (la IA nunca publica sola). Sin IA (503) se avisa y se escribe a mano.
- **D-36d Lista de reseñas:** `GET /lugares/{id}/resenas?recientes` (la del negocio es la de su ficha). Resumen, Sello y momentos son complementos: si fallan, se ocultan y la lista se muestra igual.
- **D-36e Aviso del Sello en 52:** usa el criterio de `avance_sello` cuyo nombre habla de responder; si el panel no lo manda, el aviso va sin porcentaje.
- **D-36f Momentos:** la respuesta trae `agradecido_por_negocio` y `destacado_por_negocio`; el `PATCH` manda solo `agradecido` o `destacado`. «Agradecer» se apaga tras usarse (avisa al autor una vez) y un segundo toque mientras viaja se ignora.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Barra de negocio → `NegResenas` → `NegResp`, `NegMomentos`.

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| GET | `/negocios/{id}/resenas/resumen` | con `?periodo=mes o trimestre` → `{periodo, desde, actual{total, promedio, aspectos{…}}, anterior{…}, distribucion{1..5}}` (P-50) de las publicadas | JWT dueño del negocio | 200/401/403/404/422 |
| GET | `/lugares/{id}/resenas` | con `?estrellas=&aspecto=&con_fotos=&q=&orden=&cursor=&limite=` → `PageResult[{id, autor{nombre_usuario, foto_url?}, calificacion, aspectos, chips, comentario, fotos,… | no | 200/404/422/429 |
| PUT | `/negocios/{id}/resenas/{rid}/respuesta` | con `{texto (1–500)}` → `{id, respuesta_negocio{texto, fecha, estado}}` | JWT dueño del negocio | 200/400/401/403/404/422 |
| POST | `/negocios/{id}/resenas/{rid}/sugerir-respuesta` | → `{texto}` (≤ 500, sin datos personales) con la `ia.Guardia` de nv-26 a nv-28 | JWT dueño del negocio | 200/401/403/404/429/503 |
| GET | `/negocios/{id}/panel` | → `{negocio_id, nombre, estado, pausado{desde, hasta, motivo}?, cambios_pendientes{campos, enviado_en}?, datos_fiscales_completos?, cuenta_deposito{clabe_ultimos4, ban… | JWT dueño | 200/401/403/404 |
| GET | `/negocios/{id}/momentos` | con `?destacados=&cursor=&limite=` → `PageResult[momento]` públicos y publicados del negocio con `autor{id, nombre_usuario, foto_url}` | JWT comerciante, propietario | 200/401/403/404/422 |
| PATCH | `/negocios/{id}/momentos/{mid}` | con `{agradecido?, destacado?}` (al menos uno | JWT comerciante, propietario | 200/400/401/403/404/422 |

## 4. Modelo de datos

Sin Room nuevo.

## 5. Seguridad y privacidad

- De los clientes solo se muestra `nombre_usuario` y foto, como entrega la API.
- La respuesta sugerida por IA se puede editar antes de publicar; nunca se publica sola.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `BusinessReviewsViewModel`: periodo mes/trimestre, respuesta 1–500, 422 de moderación.
- Unit: `BusinessMomentsViewModel`: Agradecer y Destacar sin duplicar al tocar dos veces.
- Data: MockWebServer: `PUT respuesta`, `POST sugerir-respuesta` (sin IA → aviso), `PATCH momentos/{mid}` con al menos un campo.
- Security: La sugerencia de IA no manda datos del autor (solo `rid`).
- UI: Compose: Publicar respuesta deshabilitado con texto vacío.
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

Rama `feat/nv-36-business` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Implementada el 2026-11-23 por Diego (a pedido suyo; reparto: Borre) en `feat/nv-36-business`, sobre `integracion/todo`; sin probar contra staging.
