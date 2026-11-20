# NV-33 — Ayuda

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. **Responsable: Diego.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Resolver dudas sin salir de la app y reportar un problema.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como usuario quiero encontrar respuestas rápido.

- [ ] Preguntas frecuentes en acordeón con buscador que filtra mientras escribo (sin acentos ni mayúsculas).
- [ ] Sin coincidencias: «No encontramos esa pregunta» y los contactos.

**HU.** Como usuario quiero contactar o reportar un problema.

- [ ] Correo abre el cliente de correo (`ACTION_SENDTO`); WhatsApp abre el chat. Ambos datos salen de `BuildConfig` (ver decisiones).
- [ ] Reportar un problema: texto 1–500 → `POST /errores` con `modulo = reporte_usuario`, `version` de la app y `traza_id` si hay; confirma con un aviso.

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `86_Ayuda` | Buscador, preguntas frecuentes en acordeón, WhatsApp, correo, reportar un problema. |

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

- No hay endpoint de preguntas frecuentes: son textos de la app en `strings.xml` (no son datos simulados; se editan como cualquier texto). Lista inicial en `plan.md`.
- Reportar un problema usa `POST /errores` (único canal publicado); el backend quita correos y teléfonos del mensaje (D-33a).
- `SUPPORT_EMAIL` y `SUPPORT_WHATSAPP` van en `BuildConfig` desde `local.properties`. Diego abre un Issue para que Jorge los agregue a `app/build.gradle.kts` (configuración compartida). Mientras no existan, esas filas no se muestran.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Ajustes (`85_Ajustes`) → `Ayuda` (fila nueva, punto de entrada permitido).

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| POST | `/errores` | con `{modulo, mensaje, version, traza_id?}` → 202 sin cuerpo | JWT | 202/400/401/422/429/503 |

## 4. Modelo de datos

Sin datos persistentes.

## 5. Seguridad y privacidad

- El reporte no incluye datos personales: se avisa en pantalla y el backend los filtra.
- Los intents de correo y WhatsApp no llevan datos del usuario.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `HelpViewModel`: filtro sin acentos, vacío y envío del reporte (éxito, 422, sin red).
- Data: MockWebServer: `POST /errores` manda `modulo`, `mensaje` y `version`; 202 sin cuerpo.
- Security: El mensaje se recorta a 500 y no agrega correo ni teléfono del perfil.
- UI: Compose: el acordeón abre y cierra; sin resultados muestra contactos.
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

Rama `feat/nv-33-help` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Responsable: Diego.
