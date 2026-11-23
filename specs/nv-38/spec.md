# NV-38 — Negocio: perfil, fotos y cobros

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 11 · Negocio. **Responsable: Osmar.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Mantener al día el perfil y las fotos del negocio, cobrar los anticipos y volver al modo explorar.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como dueño quiero editar mi negocio.

- [x] `58_NegPerfil`: datos y horario con `PATCH /negocios/{id}` (`If-Match`); Recibir reservaciones = `acepta_reservas` en `PUT reservas-config`; Pausar con `POST /negocios/{id}/pausa`.
- [x] `87_NegGaleria`: hasta 10 fotos, arrastrar para ordenar (`PUT fotos/orden`), borrar (mínimo 3 en revisión o aprobado).
- [x] `88_NegAgregarFoto`: Tomar foto o Elegir de la galería → `PhotoRepository` + `POST /negocios/{id}/fotos`.

**HU.** Como dueño quiero cobrar.

- [x] `57_NegCobros`: `GET saldo` (disponible, en espera, próximo depósito) y `GET liquidaciones` paginado; Conectar cobros abre la URL de Stripe (`POST cobros/conectar`) en Custom Tabs.
- [x] `89_NegBanco`: titular y CLABE de 18 dígitos (dígito verificador) → `PUT cuenta-deposito`.
- [x] `90_NegFiscal`: RFC, razón social, régimen, uso CFDI, C.P. → `PUT datos-fiscales` (422 `rfc_invalido` explicado).

**HU.** Como dueño quiero salir del modo negocio.

- [x] Volver a Wherever = `POST /usuarios/yo/modo` con `explorar` → Inicio.
- [x] Eliminar negocio: escribir el nombre exacto → `DELETE /negocios/{id}`; Cerrar sesión reutiliza la de Ajustes.

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `58_NegPerfil` | Portada, Sello, datos y horario, fotos, cobros, Recibir reservaciones, Volver a Wherever, facturación, Cerrar sesión, Eliminar. |
| `87_NegGaleria` | Portada, galería con arrastrar, consejo, Guardar cambios. |
| `88_NegAgregarFoto` | Hoja: Tomar foto, Elegir de la galería, Importar de Instagram, Cancelar. |
| `57_NegCobros` | Disponible para depositar, en espera, depósitos, Descargar estado de cuenta. |
| `89_NegBanco` | Titular, banco, CLABE con validación, depósito de prueba, cuenta principal, Guardar cuenta. |
| `90_NegFiscal` | Razón social, RFC, C.P. fiscal, régimen, correo, constancia, factura mensual, Guardar datos. |

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
- Importar de Instagram, Descargar estado de cuenta, facturación y depósito de prueba están diferidos en el backend: esas opciones no se dibujan (D-38a).

### Decisiones al implementar (2026-11-23, Diego)

- **D-38b Pestaña Negocio** en la barra (quinta, como en la maqueta). «Datos y horario» no tiene frame: se arma con los componentes del sistema y la fila de horario del alta (nv-34). Nombre y dirección no se editan aquí (la API los manda a `cambios_pendientes`).
- **D-38c Stripe:** `androidx.browser` no está en el stack; el registro se abre con `ACTION_VIEW` en el navegador. Queda pendiente el Issue para acordar Custom Tabs.
- **D-38d Perfil:** se lee de `GET /negocios/mios` (no hay `GET /negocios/{id}` del dueño) y se completa con `cuenta_deposito` y `datos_fiscales_completos` del panel. `acepta_reservas` se guarda con el mismo `PUT reservas-config` de nv-37, sin perder los demás campos.
- **D-38e Galería:** mantener y arrastrar reordena; el menú «⋮» de cada foto agrega «Usar como portada» y «Eliminar» (alternativa accesible al arrastre). El orden se manda completo con «Guardar cambios»; borrar es inmediato. Las fotos nuevas se registran como `interior`.
- **D-38f Catálogos del SAT:** la API no tiene un catálogo de régimen ni de uso CFDI; se ofrecen los códigos oficiales más comunes (601, 603, 605, 606, 612, 621, 625, 626 y G03, G01, S01). La API valida contra el catálogo completo.
- **D-38g Pausa:** fila «Pausar mi negocio» (no está en el frame) con hoja de 1 semana, 2 semanas, 1 mes o sin fecha, y motivo opcional; reanudar es directo. Eliminar llama después a `modo explorar` y vuelve a Inicio.
- **D-38h Cerrar sesión** usa `AuthRepository.logout()` como Ajustes y vuelve a Login. «Volver a Wherever», cerrar sesión y eliminar salen del modo negocio con dos callbacks en `BusinessHomeRoute` (una línea en `WhereverNavHost`).
- Custom Tabs para Stripe: si `androidx.browser` no está en el stack, se abre con `ACTION_VIEW` y Osmar abre un Issue para acordarlo.
- Los montos llegan en centavos y se muestran en pesos con el formato de México.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Barra de negocio → `NegPerfil` → `NegGaleria` → hoja `NegAgregarFoto`.
- `NegPerfil` → `NegCobros`, `NegBanco`, `NegFiscal`, `NegHoras` (nv-37).
- Volver a Wherever → Inicio del modo explorar.

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| GET | `/negocios/{id}/panel` | → `{negocio_id, nombre, estado, pausado{desde, hasta, motivo}?, cambios_pendientes{campos, enviado_en}?, datos_fiscales_completos?, cuenta_deposito{clabe_ultimos4, ban… | JWT dueño | 200/401/403/404 |
| PATCH | `/negocios/{id}` | con `If-Match` y los campos a cambiar (`descripcion`, `categoria`, `horarios`, `nivel_precio`, `precio_promedio`, `servicios`, `opciones_dieta`, `alergenos`, `accesibi… | JWT dueño | 200/400/401/403/404/409/422 |
| DELETE | `/negocios/{id}` | con `{confirmacion}` (nombre exacto | JWT dueño | 204/401/403/404/409/422 |
| POST | `/negocios/{id}/pausa` | con `{pausado, hasta? (futura, ≤ 90 días), motivo? (≤ 120)}` → el perfil | JWT dueño | 200/401/403/404/409/422 |
| PUT | `/negocios/{id}/reservas-config` | con `If-Match` y `{acepta_reservas, capacidad_por_horario (1–500), duracion_turno_min (15–240, múltiplo de 15), anticipo{tipo: fijo, porcentaje o por_persona, valor},… | JWT dueño | 200/400/401/403/404/409/422 |
| POST | `/negocios/{id}/fotos` | con `{imagen_id, tipo (fachada, interior o producto)}` → 201 el negocio | JWT dueño | 201/401/403/404/409/422/503 |
| PUT | `/negocios/{id}/fotos/orden` | con `{fotos_ids[]}` (todas las de la galería, sin repetir | JWT dueño | 200/401/403/404/409/422 |
| DELETE | `/negocios/{id}/fotos/{foto_id}` | → 204 | JWT dueño | 204/401/403/404/409/422 |
| POST | `/imagenes/subida` | con `{tipo}` (`image/jpeg`, `image/png` o `image/webp`) → `{id, url, campos{}, vence_en, max_bytes}`: formulario POST firmado de S3 que solo acepta la clave `cuarenten… | JWT | 201/400/401/403/422/429/503 |
| POST | `/imagenes/{id}/confirmar` | → `{id, estado, motivo?, url_1080?, url_320?}` | JWT, dueño | 200/401/403/404/409/422/503 |
| POST | `/negocios/{id}/cobros/conectar` | → `{url}` (registro de Stripe) | JWT dueño del negocio | 200/401/403/404/503 |
| GET | `/negocios/{id}/saldo` | → `{disponible, en_espera, proximo_deposito}` en centavos | JWT dueño del negocio | 200/401/403/404 |
| GET | `/negocios/{id}/liquidaciones` | con `?cursor=&limite=` → `PageResult[{id, semana_inicio, pagos, total_anticipos, comision, reembolsos, neto, estado}]` de la más nueva a la más vieja | JWT dueño del negocio | 200/401/403/404/422 |
| PUT | `/negocios/{id}/cuenta-deposito` | con `{clabe, titular}` → `{clabe_ultimos4, banco, titular, estado: sin_verificar}` | JWT dueño | 200/401/403/404/422/429/503 |
| PUT | `/negocios/{id}/datos-fiscales` | con `{rfc, razon_social, regimen_fiscal, uso_cfdi, cp_fiscal}` → 200 metadatos | JWT dueño | 200/401/403/404/422/429/503 |
| POST | `/usuarios/yo/modo` | → `{modo: explorar\|negocio}` | JWT | 200/400/401/403/422 |

## 4. Modelo de datos

Sin Room nuevo. La CLABE y el RFC no se guardan en el teléfono.

## 5. Seguridad y privacidad

- CLABE y RFC solo viajan en el `PUT`; en pantalla se muestran los últimos 4 dígitos que devuelve la API.
- Eliminar exige el nombre exacto; nada del expediente queda en caché.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `BusinessProfileViewModel`: `If-Match`, 409 → recargar, pausa hasta ≤ 90 días.
- Unit: `GalleryViewModel`: orden, mínimo 3 fotos y máximo 10.
- Unit: `BankAccountViewModel`: CLABE de 18 dígitos con dígito verificador 3-7-1.
- Data: MockWebServer: `PUT cuenta-deposito` (422 `clabe_invalida`), `GET liquidaciones` con cursor, `POST cobros/conectar` devuelve `url`.
- Security: Ningún log ni archivo guarda CLABE completa ni RFC.
- UI: Compose: Eliminar negocio habilita el botón solo con el nombre exacto.
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

Rama `feat/nv-38-business` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Implementada el 2026-11-23 por Diego (a pedido suyo; reparto: Borre) en `feat/nv-38-business`, sobre nv-36; sin probar contra staging.
