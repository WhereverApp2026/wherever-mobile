# NV-35 — Negocio: panel, reservas y check-in

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sección de la maqueta: 11 · Negocio. **Responsable: Osmar.** Lista para ejecutar (2026-11-20). Reglas de trabajo: `CONTRIBUTING.md`.

## 1. Objetivo

Ver el día del negocio de un vistazo, administrar reservaciones y registrar llegadas.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como dueño quiero ver el panel.

- [x] `48_NegPanel` con `GET /negocios/{id}/panel`: reservas hoy, próxima, cupones, llegadas, calificación de la semana, reseñas por responder y avance del Sello; cada dato nulo se oculta.
- [x] `56_NegStats` con `GET /negocios/{id}/estadisticas` (Semana, Mes; Año = 90 días máx. de la API, ver decisiones) y Descargar reporte mensual (`GET /negocios/{id}/reportes/mensual`, PDF).

**HU.** Como dueño quiero administrar mis reservaciones.

- [x] `49_NegReservas`: Hoy, Próximas, Pasadas con `GET /negocios/{id}/reservas?fecha=&estado=&q=`.
- [ ] Confirmar (desde pendiente), Llegó o No llegó con `PATCH /negocios/{id}/reservas/{rid}`; Contactar manda mensaje (≤ 500).
- [ ] Cerrar horarios con `POST /negocios/{id}/bloqueos` y reabrir con `DELETE`.

**HU.** Como dueño quiero registrar una llegada.

- [x] `50_NegCheckin`: escribir el código de la reserva → `POST /negocios/{id}/reservas/escanear`; ya usado = 409 «Este código ya se usó».
- [ ] Detalle del cliente con personas, nota y anticipo (`GET /negocios/{id}/reservas/{rid}/cobro` si hubo pago).

### UI (frames de Figma)

Fuente: Figma `9IjBujkogqaUPAkDbluclQ`, página «Boceto 2» (canvas `96:130`). Cada frame se busca por su nombre. Si tienes la carpeta local `maqueta/figma-frames/` (no está en Git), el PNG se llama `<frame> 1.png`.

| Frame | Contenido |
|---|---|
| `48_NegPanel` | Portada, Sello, Reservas hoy, Anticipos, Ocupación, gráfica semanal, Próximas hoy, barra de negocio. |
| `49_NegReservas` | Hoy, Próximas, Pasadas; filas con hora, cliente, personas y estado. |
| `50_NegCheckin` | Búsqueda por código, QR, cliente, detalle, Confirmar llegada, Reprogramar. |
| `NegCheckin · *` | Variantes por cliente y estado de pago (Ana y amigos, Familia Ortega, Jorge M., Luis Hernández). |
| `56_NegStats` | Semana, Mes, Año; reservas, anticipos, asistencia, cupones; por día; de dónde llegan. |

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
- Escanear QR necesita una librería de cámara/QR fuera del stack (constitución §3): hoy el check-in es por código escrito. Osmar abre un Issue para acordar la librería (D-35a).
- Reprogramar desde el negocio no existe en la API: el botón se reemplaza por Contactar (mensaje al cliente).
- `56_NegStats` «Año»: la API da 90 días como máximo; se muestran Semana, Mes y Trimestre.
- El consejo de la IA del día (`GET /negocios/{id}/consejo`) va en el Panel si responde; si no, se oculta.

### No-alcance

- Nada con datos simulados o locales (constitución §2.7): si falta un endpoint, el control no se dibuja y se anota.
- No se tocan theme, navegación compartida ni cliente de API (`CONTRIBUTING.md`); lo que haga falta se pide con un Issue.

## 3. Contratos (API y navegación)

### Navegación

- Grafo principal → `BusinessHomeRoute` (Panel). Barra interna: Panel, Reservas, Cupones (nv-37), Reseñas (nv-36), Perfil (nv-38).
- `NegReservas` → `NegCheckin`. `NegPanel` → `NegStats`.
- Entrar al modo negocio: `POST /usuarios/yo/modo` con `negocio` (403 sin rol → ir a nv-34).

Rutas tipadas nuevas en `docs/routes.md`. Puntos de entrada permitidos: una fila o botón en la pantalla indicada y una línea en `WhereverNavHost` (`CONTRIBUTING.md`).

### Endpoints

Base `BuildConfig.API_BASE_URL` (`/api/v1`). Contrato completo y vivo: `wherever-backend/docs/endpoints.md` (manda ante cualquier diferencia). Errores `{codigo, mensaje, campos[], traza_id}` → `AppError`; listas `PageResult{items, siguiente_cursor, limite}`.

| Método | Ruta | Qué hace (resumen) | Auth | Códigos |
|---|---|---|---|---|
| GET | `/negocios/mios` | → `{items[negocio del alta]}` del más nuevo al más viejo, sin dados de baja | JWT comerciante | 200/401/403 |
| GET | `/negocios/{id}/panel` | → `{negocio_id, nombre, estado, pausado{desde, hasta, motivo}?, cambios_pendientes{campos, enviado_en}?, datos_fiscales_completos?, cuenta_deposito{clabe_ultimos4, ban… | JWT dueño | 200/401/403/404 |
| GET | `/negocios/{id}/estadisticas` | con `?desde=&hasta=&agrupar=` (AAAA-MM-DD, días locales de America/Mexico_City | JWT dueño del negocio (comerciante), cuenta activa; avanzadas solo premium | 200/401/403/404/422/429 |
| GET | `/negocios/{id}/reportes/mensual` | con `?mes=` (AAAA-MM, obligatorio: el mes en curso o uno cerrado de los últimos 12, no anterior al alta | JWT dueño del negocio (comerciante), cuenta activa | 200/401/403/404/422/429 |
| GET | `/negocios/{id}/consejo` | → `{texto, accion, fuente, dia}`: «Consejo de la IA del día (US-142), p. ej. "Los martes de 4 a 6 tienes poca gente. ¿Lanzas un cupón?". Se genera una vez al día.» Un… | JWT dueño del negocio (comerciante), cuenta activa | 200/401/403/404/429/503 |
| GET | `/negocios/{id}/reservas` | con `?fecha=&estado=&q=` → `{fecha, items[{id, codigo, fecha_hora, cliente, personas, nota?, anticipo, estado, version}]}` por hora (hoy por defecto) | JWT dueño del negocio | 200/401/403/404/422 |
| PATCH | `/negocios/{id}/reservas/{rid}` | con `{estado: confirmada, asistio o no_asistio}` → reserva | JWT dueño del negocio | 200/400/401/403/404/409/422 |
| POST | `/negocios/{id}/reservas/{rid}/mensajes` | con `{texto}` (1 a 500 caracteres, recortado, sin HTML: rechaza los signos menor y mayor que) → 201 `{id, reserva_id, texto, creado_en}` («Contactar»: mensaje del nego… | JWT dueño del negocio (comerciante), cuenta activa | 201/400/401/403/404/409/422/429 |
| POST | `/negocios/{id}/reservas/escanear` | con `{codigo}` → reserva `asistio` | JWT dueño del negocio | 200/400/401/403/404/409 |
| POST | `/negocios/{id}/bloqueos` | con `{desde, hasta, motivo? (evento_privado, lleno, otro)}` → 201 `{id, desde, hasta, motivo}` | JWT dueño del negocio | 201/400/401/403/404/422 |
| DELETE | `/negocios/{id}/bloqueos/{bid}` | → 204 sin cuerpo | JWT dueño del negocio | 204/401/403/404 |
| GET | `/negocios/{id}/reservas/{rid}/cobro` | → `{anticipo, reembolsos, comision, neto, estado, liquidacion_semana?}` en centavos | JWT dueño del negocio | 200/401/403/404 |
| POST | `/usuarios/yo/modo` | → `{modo: explorar\|negocio}` | JWT | 200/400/401/403/422 |

## 4. Modelo de datos

Sin Room nuevo. El panel se pide al abrir y al volver al primer plano (sin caché: `no-store`).

## 5. Seguridad y privacidad

- Datos de clientes solo en pantalla; nunca en logs.
- Descargar el PDF usa `MediaStore` (Descargas) sin permisos extra.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `BusinessPanelViewModel`: campos nulos ocultos, error y reintentar.
- Unit: `BusinessReservationsViewModel`: cambiar de pestaña, confirmar, asistió/no asistió (409 → recargar).
- Unit: `CheckinViewModel`: código válido, 404 y 409 `codigo_ya_usado`.
- Data: MockWebServer: `GET /negocios/{id}/reservas` con `fecha`, `estado` y `q`; `POST escanear`; PDF del reporte se guarda completo.
- Security: El nombre del cliente no aparece en logs (prueba sobre `RedactedLoggingInterceptor`).
- UI: Compose: barra de negocio cambia de pestaña; check-in con código vacío deshabilita Confirmar.
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

Rama `feat/nv-35-business` desde `main` actualizado → `plan.md` → `task.md` → PR a `main` con Jorge como revisor y el CI en verde.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06 (actualizada para ejecución el 2026-11-20)
- [ ] CHECKLIST_PR.md OK
- [x] Sin conflicto con constitución

**Estado:** ✅ Aprobada y lista para ejecutar. Responsable: Osmar. Implementada el 2026-11-22 por Diego en `feat/nv-35-business`, ya en `main`; sin probar contra staging: espera el 503 de `POST /negocios/{id}/documentos` (backend).
