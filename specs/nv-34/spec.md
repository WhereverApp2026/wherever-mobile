# NV-34 — Negocio: registro

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 11 · Negocio. **Responsable: Osmar.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Dar de alta un negocio en cinco pasos y mandarlo a revisión.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como dueño quiero registrar mi negocio.

- [ ] Paso 1 `42_NegCuenta`: sin sesión, crea la cuenta (`POST /usuarios`, mismas reglas que Registro); con sesión, se salta.
- [ ] Paso 2 `43_NegRegistro`: nombre (3–60), giro (de `GET /categorias`), descripción (≤ 300; la maqueta dice 200, manda la API) → `POST /negocios`; si responde `requiere_refresh`, la app renueva el token antes de seguir.
- [ ] Pasos 3 a 5 guardan con `PATCH /negocios/{id}/alta/{paso}` e `If-Match`; Guardar y salir conserva el borrador y al volver se retoma en `paso_alta`.

**HU.** Como dueño quiero completar la verificación.

- [ ] `46_NegVerif`: mínimo 3 fotos (`PhotoRepository` + `POST /negocios/{id}/fotos`), INE frente y reverso y comprobante (`POST /negocios/{id}/documentos`, ≤ 5 MB), datos fiscales y CLABE opcionales.
- [ ] Enviar solicitud = `POST /negocios/{id}/enviar-revision`; si falta algo, la API responde 422 con la lista y se muestra tal cual.
- [ ] `47_NegListo` con el estado de la revisión; Editar mi solicitud vuelve al paso que falte.

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `42_NegCuenta` | Paso 1: administrador, correo, teléfono, contraseña y confirmación, términos, Continuar, Guardar y salir. |
| `43_NegRegistro` | Paso 2: nombre comercial, giro, tipo de experiencia, descripción, año de apertura. |
| `44_NegUbic` | Paso 3: calle, colonia, municipio, C.P., pin en mapa, WhatsApp, Instagram o sitio. |
| `45_NegOper` | Paso 4: horario, mesas, máximo de personas, rango de precios, servicios, anticipo por persona. |
| `46_NegVerif` | Paso 5: logo, mínimo 3 fotos, identificación, comprobante, constancia fiscal, CLABE, Enviar solicitud. |
| `47_NegListo` | "Solicitud enviada": revisión en 24 a 48 horas, avance, Ir al panel, Editar mi solicitud. |

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
- El negocio activo es el primero de `GET /negocios/mios` (hasta 3 altas); si hay más de uno, selector arriba del Panel.
- Pin en mapa de `44_NegUbic`: no hay proveedor de mapa (D25) ni `GET /geo/direcciones` (diferido). La ubicación se toma con «Estoy en mi negocio» (ubicación del teléfono, permiso en contexto) y se muestra la dirección escrita; el mapa llega cuando se decida D25 (D-34a).
- «Tipo de experiencia» y «año de apertura» de `43_NegRegistro` no existen en la API: no se dibujan.
- «Que la IA me ayude» en la descripción usa `POST /negocios/{id}/ia/descripcion` (3 palabras); solo después de crear el borrador.
- Los documentos se mandan como multipart directo a la API (no por nv-45), como pide el contrato.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Ajustes → «Registrar mi negocio» → `NegCuenta` (fila nueva en `85_Ajustes`).
- `NegCuenta` → `NegRegistro` → `NegUbic` → `NegOper` → `NegVerif` → `NegListo` → `BusinessHomeRoute` (Panel, nv-35).

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| POST | `/usuarios` | → `{nombre, fecha_nacimiento (AAAA-MM-DD), correo, password, acepta_terminos, version_terminos, promociones_correo, dispositivo?}` → 201 con tokens | no | 201/400/409/422/429 |
| GET | `/categorias` | → `{categorias[{id,nombre,conteo}], etiquetas[{id,nombre,conteo}], servicios[{id,nombre,conteo}], filtros{nivel_precio[], opciones_dieta[], accesibilidad[]}}` | no | 200/503 |
| POST | `/negocios` | con `{nombre (3–60), categoria (uno de los 6 giros), descripcion (≤ 300)}` → 201 negocio en `borrador` `{id, nombre, categoria, descripcion, horarios[], fotos[], nivel… | JWT consumidor o comerciante | 201/400/401/403/409/422 |
| GET | `/negocios/mios` | → `{items[negocio del alta]}` del más nuevo al más viejo, sin dados de baja | JWT comerciante | 200/401/403 |
| PATCH | `/negocios/{id}/alta/{paso}` | con `If-Match` → el negocio con `paso_alta = max(actual, paso)` | JWT dueño | 200/400/401/403/404/409/422 |
| POST | `/negocios/{id}/fotos` | con `{imagen_id, tipo (fachada, interior o producto)}` → 201 el negocio | JWT dueño | 201/401/403/404/409/422/503 |
| PUT | `/negocios/{id}/fotos/orden` | con `{fotos_ids[]}` (todas las de la galería, sin repetir | JWT dueño | 200/401/403/404/409/422 |
| DELETE | `/negocios/{id}/fotos/{foto_id}` | → 204 | JWT dueño | 204/401/403/404/409/422 |
| POST | `/negocios/{id}/documentos` | multipart `tipo` (`ine_frente`, `ine_reverso`, `comprobante_domicilio`) y `archivo` (JPEG, PNG o PDF por sus primeros bytes, ≤ 5 MB) → 201 `{tipo, content_type, tamano… | JWT dueño | 201/401/403/404/409/413/422/429/503 |
| GET | `/negocios/{id}/documentos` | → `{items[{tipo, content_type, tamano, subido_en, estado, borrar_despues_de?}]}` | JWT dueño | 200/401/403/404 |
| PUT | `/negocios/{id}/datos-fiscales` | con `{rfc, razon_social, regimen_fiscal, uso_cfdi, cp_fiscal}` → 200 metadatos | JWT dueño | 200/401/403/404/422/429/503 |
| PUT | `/negocios/{id}/cuenta-deposito` | con `{clabe, titular}` → `{clabe_ultimos4, banco, titular, estado: sin_verificar}` | JWT dueño | 200/401/403/404/422/429/503 |
| POST | `/negocios/{id}/enviar-revision` | → el negocio en `en_revision` con `revision.enviado_en` | JWT dueño | 200/401/403/404/409/422 |
| POST | `/negocios/{id}/ia/descripcion` | con `{palabras: [3]}` (cada una 2–30 letras, números, espacios o guiones) → `{descripcion, fuente}`: «"Que la IA me ayude": redacta la descripción a partir de 3 palabr… | JWT dueño del negocio (comerciante), cuenta activa | 200/400/401/403/404/422/429 |
| POST | `/imagenes/subida` | con `{tipo}` (`image/jpeg`, `image/png` o `image/webp`) → `{id, url, campos{}, vence_en, max_bytes}`: formulario POST firmado de S3 que solo acepta la clave `cuarenten… | JWT | 201/400/401/403/422/429/503 |
| POST | `/imagenes/{id}/confirmar` | → `{id, estado, motivo?, url_1080?, url_320?}` | JWT, dueño | 200/401/403/404/409/422/503 |
| POST | `/usuarios/yo/modo` | → `{modo: explorar\|negocio}` | JWT | 200/400/401/403/422 |

## 4. Modelo de datos

Borrador en la API (`paso_alta`); la app no guarda el expediente en disco. Lo escrito y sin enviar de cada paso vive en `SavedStateHandle`.

## 5. Seguridad y privacidad

- INE, comprobante, RFC y CLABE nunca en logs ni en caché; los archivos temporales se borran al subir.
- Documentos solo JPEG, PNG o PDF ≤ 5 MB (validado antes de subir).
- Permiso de ubicación solo al tocar «Estoy en mi negocio».
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `BusinessOnboardingViewModel`: avance por pasos, retomar en `paso_alta`, Guardar y salir, 422 con lista de faltantes.
- Unit: Validaciones: nombre 3–60, descripción ≤ 300, CLABE de 18 dígitos, documento ≤ 5 MB.
- Data: MockWebServer: `POST /negocios` con `requiere_refresh` dispara la renovación; `PATCH alta/{paso}` manda `If-Match`; multipart de documentos con `tipo` y `archivo`.
- Security: Ningún log contiene RFC, CLABE ni nombres de archivo del expediente (prueba sobre el interceptor de logs).
- UI: Compose: el botón Continuar se habilita solo con el paso válido; 422 de enviar-revision lista lo que falta.
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

Rama `feat/nv-34-business` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Responsable: Osmar.
