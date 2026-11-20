# NV-31 — Recuerdos y postales

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 9 · Recuerdos y reseñas. **Responsable: Diego.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Tomar postales de los lugares visitados, guardarlas en el álbum de recuerdos y publicarlas como momento para los amigos.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como usuario quiero tomar una postal de mi visita.

- [ ] La cámara se pide al abrirla (`docs/permissions-matrix.md`); sin permiso o sin cámara, se elige de la galería.
- [ ] Elijo estilo (clasica, polaroid o pasaporte) y texto opcional (≤ 80); se sube con `PhotoRepository` y se crea con `POST /postales`.
- [ ] Guardar en mi álbum la deja privada; Compartir postal abre la hoja del sistema con la imagen ya publicada.

**HU.** Como usuario quiero ver mis recuerdos.

- [ ] Álbum con Todas (`GET /postales`) y Por plan (`GET /recuerdos`), paginado por cursor.
- [ ] Ver una postal con lugar, plan y compañía; Eliminar pide confirmación (`DELETE /postales/{id}` con `If-Match`).
- [ ] Sin postales se ve `76_RecVacio`.

**HU.** Como usuario quiero publicar un momento.

- [ ] `70_CrearPub` pestaña Foto: elijo postal, texto (≤ 280), etiqueto amigos aceptados (≤ 10) y visibilidad (amigos o pública); Publicar = `POST /momentos`.
- [ ] Pestaña Reseña abre el flujo de nv-32 (`77_ResPend`); pestaña Plan queda fuera (ver decisiones).

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `70_CrearPub` | Nuevo momento: pestañas Foto, Plan, Reseña; texto con menciones; lugar, etiquetas, quién lo ve; Publicar. |
| `71_Camara` | Cámara con marco de postal, Postal o Normal, disparador, galería. |
| `72_Postal` | Vista previa, estilos; Guardar en mi álbum, Compartir postal. |
| `73_Album` | Recuerdos: Todas o Por plan, cuadrícula. |
| `74_RecPlan` | Recuerdos agrupados por plan. |
| `75_PostalVer` | Postal con lugar, plan, compañía, Compartir, Descargar, Eliminar postal. |
| `PostalVer · *` | Variantes por lugar (Fonda Doña Meche, Jardín del Cerro, Mirador, Parque Ecológico, Plaza Jardín, Tacos El Güero): mismo componente con otros datos. |
| `76_RecVacio` | "Aún no tienes recuerdos", Cómo tomar una postal, Ver mis planes. |

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

- Estilos: la maqueta dice Clásica, Atardecer y Minimal; la API acepta `clasica`, `polaroid` y `pasaporte`. Manda la API: se muestran sus tres estilos con los textos de `strings.xml` (D-31a en `docs/revision-jorge.md`).
- La app dibuja la postal (marco y texto) y sube el resultado por `PhotoRepository` (nv-45); el servidor quita metadatos al recodificar, pero la app tampoco manda EXIF de ubicación.
- Pestaña Plan de `70_CrearPub` fuera de alcance: no hay endpoint para publicar un plan en el feed. Se muestra deshabilitada con el texto «Pronto».
- El feed (`GET /momentos/feed`) es de Inicio (nv-17, Jorge); aquí solo se crean, editan y borran momentos.
- Descargar postal guarda la imagen publicada en la galería con `MediaStore` (sin permiso de almacenamiento en API 29+).

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Perfil → `Album` (fila nueva en `81_Perfil`, punto de entrada permitido).
- `Album` → `PostalVer`; `Album` (Por plan) → `RecPlan`.
- `Camara` → `Postal` → `Album`. La entrada desde check-in (`CheckinOk`) llega con nv-30.
- `CrearPub` se abre desde Perfil hasta que exista la hoja Crear (nv-17).

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| POST | `/imagenes/subida` | con `{tipo}` (`image/jpeg`, `image/png` o `image/webp`) → `{id, url, campos{}, vence_en, max_bytes}`: formulario POST firmado de S3 que solo acepta la clave `cuarenten… | JWT | 201/400/401/403/422/429/503 |
| POST | `/imagenes/{id}/confirmar` | → `{id, estado, motivo?, url_1080?, url_320?}` | JWT, dueño | 200/401/403/404/409/422/503 |
| DELETE | `/imagenes/{id}` | → borra el original y los tamaños en S3 y la fila | JWT, dueño | 204/401/403/404/503 |
| POST | `/postales` | con `{imagen_id (32 hex, imagen propia de nv-45 publicada o en revisión), estilo (clasica, polaroid o pasaporte), texto? (≤ 80), plan_id?, negocio_id?, visibilidad (pr… | JWT consumidor | 202/400/401/403/404/409/422/429 |
| GET | `/postales` | con `?plan_id=&negocio_id=&estilo=&mes=AAAA-MM&cursor=&limite=` → `PageResult[postal]` propias del más nuevo al más viejo | JWT, dueño | 200/401/403/422 |
| GET | `/postales/{id}` | → la postal con `ETag` | JWT, dueño o con permiso | 200/401/404 |
| PATCH | `/postales/{id}` | con `If-Match` y `{imagen_id?, estilo?, texto?, visibilidad?}` → la postal | JWT, dueño | 200/400/401/403/404/409/422 |
| DELETE | `/postales/{id}` | con `If-Match` → 204 | JWT, dueño | 204/400/401/403/404/409 |
| GET | `/recuerdos` | con `?mes=AAAA-MM&cursor=&limite=` → `PageResult[recuerdo]` propios por fecha del plan | JWT | 200/401/403/422 |
| POST | `/planes/{id}/recuerdo` | con `{tarjeta_imagen_id?}` (imagen propia publicada | JWT, dueño o integrante confirmado | 200/201/401/403/404/422 |
| GET | `/planes/{id}/recuerdo` | → el recuerdo propio del plan | JWT, dueño o integrante confirmado | 200/401/403/404 |
| DELETE | `/recuerdos/{id}` | → 204 | JWT, dueño | 204/401/403/404 |
| POST | `/momentos` | con `{postal_id, texto? (≤ 280), visibilidad (amigos o publica), etiquetados[]? (≤ 10, solo amigos aceptados | JWT consumidor | 201/400/401/403/404/409/422/429 |
| GET | `/momentos/{id}` | → el momento con `ETag` | opcional | 200/404 |
| PATCH | `/momentos/{id}` | con `If-Match` y `{texto?, etiquetados[]?, visibilidad?}` → el momento | JWT, autor | 200/400/401/403/404/409/422 |
| DELETE | `/momentos/{id}` | con `If-Match` → 204 | JWT, autor | 204/400/401/403/404/409 |

## 4. Modelo de datos

Sin Room nuevo: el álbum se lee de la API con `PageResult`. Si se agrega caché, `PostcardEntity` con migración y esquema exportado.

## 5. Seguridad y privacidad

- Cámara en contexto; la app funciona sin el permiso (galería).
- Sin EXIF de ubicación en lo que se sube.
- Postal ajena o sin permiso = 404 de la API: mostrar «Esta postal ya no está disponible».
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `MemoriesViewModel`: paginación, vacío, error y reintentar.
- Unit: `PostcardViewModel`: estilo, texto ≤ 80, guardar, compartir y eliminar con confirmación.
- Unit: `CreateMomentViewModel`: etiquetados ≤ 10, visibilidad y doble toque en Publicar.
- Data: MockWebServer: `POST /postales` + `GET /postales` con cursor; `DELETE` manda `If-Match`; 404 de postal ajena → `AppError.NotFound`.
- Security: El archivo que se sube no trae EXIF GPS (prueba sobre un JPEG con ubicación).
- UI: Compose: `76_RecVacio` con Ver mis planes; Eliminar abre la confirmación.
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

Rama `feat/nv-31-memories` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Responsable: Diego.
