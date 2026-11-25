# NV-32 — Reseñas

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 9 · Recuerdos y reseñas. **Responsable: Diego.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Reseñar las visitas con check-in, ver y administrar mis reseñas y leer las de cada lugar.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como usuario quiero reseñar un lugar que visité.

- [x] `77_ResPend` lista `GET /resenas/pendientes` (check-ins válidos sin reseña, 30 días).
- [x] `68_PostVisita`: estrellas 1–5, comentario ≤ 500, hasta 3 fotos (`PhotoRepository`); `78_ResCom` califica sabor, atención, ambiente, espera y precio-calidad (1–5) y chips (≤ 5).
- [ ] Publicar = `POST /resenas`; al terminar se ve `69_Publicado`. Texto rechazado por moderación (422) se explica sin perder lo escrito.

**HU.** Como usuario quiero administrar mis reseñas.

- [x] `79_MisRes`: Publicadas (`GET /resenas/mias`) y Por reseñar; Editar solo si `editable` (48 h), con `If-Match`.
- [ ] Eliminar pide confirmación y deja «Reseña eliminada» con Deshacer durante 5 s; el `DELETE` se manda al vencer el plazo.

**HU.** Como usuario quiero leer las reseñas de un lugar.

- [x] `80_ResLugar`: promedio y distribución, filtros (estrellas, aspecto, con fotos, orden) y lista paginada.
- [ ] Útil (`PUT /resenas/{id}/util`) y Reportar (`POST /resenas/{id}/reportes`) con motivo.

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `68_PostVisita` | ¿Cómo estuvo?: foto, estrellas, texto, con quién, quién lo ve, calificar a detalle, Publicar. |
| `69_Publicado` | Confirmación, vista previa, ¿Repetimos?, Ver mi publicación, Volver al inicio. |
| `77_ResPend` | Por reseñar: aviso, visitas pendientes, ya reseñadas. |
| `78_ResCom` | Calificar a detalle: Sabor, Atención, Ambiente, Precio justo; ¿Qué fue lo mejor?; Listo. |
| `79_MisRes` | Publicadas y Por reseñar; reseña con respuesta del negocio; Editar, Eliminar. |
| `MisRes · confirmar eliminar` | Diálogo de confirmación. |
| `MisRes · reseña eliminada` | Fila con Deshacer. |
| `80_ResLugar` | Reseñas del lugar: promedio, distribución, filtros, reseñas, Útil, Reportar. |

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

- «Publicar en el feed» y «Con quién» / «Quién lo ve» de `68_PostVisita` no existen en `POST /resenas`: la reseña es pública en la ficha. Esos controles no se dibujan; `69_Publicado` dice «Tu reseña ya está publicada» (D-32a).
- `78_ResCom` muestra los cinco aspectos de la API (la maqueta trae cuatro): se agrega Espera.
- Deshacer al eliminar se resuelve en la app (espera 5 s antes del `DELETE`), porque la API no restaura.
- `80_ResLugar` es pantalla nueva en `feature/reviews`; `PlacesApi.reviews` y `useful` de `feature/places` se dejan como están (los usa la ficha). Diego declara su propia interfaz Retrofit en `feature/reviews/data`.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Perfil → `MisRes` y `ResPend` (filas nuevas en `81_Perfil`).
- Ficha del lugar (`33_Detalle`) → `ResLugar` (botón «Ver reseñas»).
- Aviso push `resena_pendiente` → `ResPend` (cuando el backend lo mande; hoy cae en Avisos).
- `ResPend` → `PostVisita` → `ResCom` → `Publicado`.

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| GET | `/resenas/pendientes` | → `{items[{checkin_id, negocio_id, negocio_nombre, dia, vence_en}]}` («Por reseñar»: check-ins válidos sin reseña de los últimos 30 días, hasta 50) | JWT consumidor | 200/401/403 |
| GET | `/resenas/mias` | con `?cursor=&limite=` → `PageResult[reseña propia con estado, editable y version]` sin las eliminadas | JWT consumidor | 200/401/403/422 |
| POST | `/resenas` | con `{checkin_id, calificacion (1–5), aspectos{sabor, atencion, ambiente, espera, precio_calidad} (1–5), chips? (≤ 5 del catálogo), comentario? (≤ 500), fotos? (≤ 3 id… | JWT consumidor | 201/400/401/403/404/409/422/429 |
| PATCH | `/resenas/{id}` | con `If-Match` y cualquiera de `{calificacion, aspectos, chips, comentario, fotos}` → la reseña | JWT autor | 200/400/401/403/404/409/422 |
| DELETE | `/resenas/{id}` | → 204 sin cuerpo | JWT autor | 204/401/403/404 |
| GET | `/lugares/{id}/resenas` | con `?estrellas=&aspecto=&con_fotos=&q=&orden=&cursor=&limite=` → `PageResult[{id, autor{nombre_usuario, foto_url?}, calificacion, aspectos, chips, comentario, fotos,… | no | 200/404/422/429 |
| PUT | `/resenas/{id}/util` | con `{util: true o false}` → `{util, total}` | JWT | 200/400/401/403/404/422/429 |
| POST | `/resenas/{id}/reportes` | con `{motivo (ofensiva, falsa, spam, datos_personales u otro)}` → 201 `{reportada: true}` | JWT | 201/400/401/403/404/409/422/429 |
| GET | `/checkins` | con `?cursor=&limite=` → `PageResult[check-in]` del más reciente al más viejo, válidos o no | JWT consumidor | 200/401/403/422 |
| POST | `/imagenes/subida` | con `{tipo}` (`image/jpeg`, `image/png` o `image/webp`) → `{id, url, campos{}, vence_en, max_bytes}`: formulario POST firmado de S3 que solo acepta la clave `cuarenten… | JWT | 201/400/401/403/422/429/503 |
| POST | `/imagenes/{id}/confirmar` | → `{id, estado, motivo?, url_1080?, url_320?}` | JWT, dueño | 200/401/403/404/409/422/503 |

## 4. Modelo de datos

Sin Room nuevo; listas por cursor desde la API.

## 5. Seguridad y privacidad

- Sin datos personales de otros autores más allá de `nombre_usuario` y foto.
- Las fotos se suben por nv-45 (sin EXIF).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `ReviewFormViewModel`: validaciones (estrellas obligatorias, ≤ 500, ≤ 3 fotos, ≤ 5 chips) y 422 de moderación.
- Unit: `MyReviewsViewModel`: editable, eliminar con Deshacer (reloj inyectado).
- Data: MockWebServer: `POST /resenas`, `PATCH` con `If-Match` (409 → recargar), `GET /lugares/{id}/resenas` con filtros en la query.
- Security: La reseña no manda campos que la API no pide (sin ubicación ni ids ajenos).
- UI: Compose: Útil cambia el contador; Reportar abre los motivos.
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

Rama `feat/nv-32-reviews` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Responsable: Diego.
