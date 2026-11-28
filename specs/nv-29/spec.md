# NV-29 — Cupones

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 7 · Reserva, pago y mis planes.
> **Contrato conciliado el 2026-11-25** con `wherever-backend/docs/endpoints.md` (backend nv-61, hoja J6): ya no es "Pendiente de backend". Comprobado contra staging: `GET /cupones/cerca` → 200 y `GET /mis-cupones` sin sesión → 401. Rama `feat/nv-29-cupones`.

## 1. Objetivo

Ver los cupones guardados y usarlos en el negocio.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero usar mis cupones.

- [x] Veo mis cupones disponibles y los usados.
- [x] Se distingue lo que vence pronto (hoy o en 3 días o menos).
- [x] Para usar un cupón pido su código de canje y lo puedo copiar o mostrar en caja; vive 5 minutos.
- [x] Desde el cupón puedo reservar en ese lugar o abrir su ficha.

### UI

| Frame | Contenido |
|---|---|
| `29_Cupones` | Título y aviso, Disponibles (N) y Usados; tarjetas tipo boleto con el valor a la izquierda (color por tipo), título, lugar, vigencia y Usar. |
| `47_DetCupon` | "Tu cupón": boleto, código con Copiar, Cómo usarlo, Condiciones, Reservar con este cupón, Ver lugar. |
| `DetCupon · *` | Variantes: $50 en tu consumo, 15% en tu cuenta, Postre de cortesía. Mismo componente con otro tipo. |

Colores por tipo desde `core/designsystem`: 2x1 `RouteBlue`, monto `Sunset`, porcentaje `Mint`, regalo `Gold` (texto `Ink`). Textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | `ListSkeleton`. |
| Vacío | Sin cupones en la pestaña: mensaje e invitación a explorar (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Error de red con Reintentar (D-29b). |
| Éxito | Lista y detalle; código de canje con su cuenta regresiva. |

### Validaciones

- La vigencia se calcula con reloj inyectado: "Vence hoy", "Vence en N días" (N ≤ 3, en naranja) o "Vence el d de mes".
- El código vive hasta `expira_en`; al vencer se oculta y se puede pedir otro.

### Decisiones (2026-11-25)

- **D-29a «Se aplican solos al reservar».** `POST /reservas` no tiene campo de cupón: la API no aplica descuentos al reservar. El cupón se usa con el código de canje (`POST /mis-cupones/{id}/canjear`) que el negocio valida (`POST /negocios/{id}/cupones/validar`, nv-37). El aviso de `29_Cupones` y "Cómo usarlo" de `47_DetCupon` dicen eso en lugar de "se aplican solos".
- **D-29b Sin Room.** Como nv-23, nv-24 y nv-26: la cartera se lee de la API y queda en memoria para abrir el detalle. Sin red se muestra el error con Reintentar. Guardar en Room queda junto con `nv-56` si el equipo lo pide.
- **D-29c Código.** La maqueta muestra un código fijo ("CAFE2X1-48K"); la API da uno de 6 caracteres que vive 5 minutos y repite el mismo mientras vive. `47_DetCupon` muestra "Mostrar código" y, al pedirlo, el código, Copiar y el tiempo restante.
- **D-29d Condiciones.** La cartera no trae condiciones: el detalle las pide a `GET /cupones/{id}` (público). Si da 404 (cupón ya no vigente), el detalle se muestra sin condiciones.
- **D-29e Entrada.** El atajo "Cupones" de Perfil, que decía "llegan muy pronto", abre `29_Cupones`. La barra de la maqueta con la pestaña Cupones no se copia (D22).

### Dudas abiertas

- **D22** Barra inferior inconsistente (`specs/README.md`).
- Guardar cupones (`POST /cupones/{id}/guardar`) desde la ficha del lugar o "Cupones cerca": no tiene frame en esta spec; sin eso, la cartera solo se llena desde otras entradas.

### No-alcance

- Guardar cupones y "Cupones cerca" (sin frame en esta sección).
- Quitar un cupón de la cartera (`DELETE /mis-cupones/{id}`): sin frame.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Perfil (atajo Cupones) → `CouponsRoute` → `CouponDetailRoute(id)` → `ReserveRoute(placeId, placeName)` o `PlaceRoute(placeId, origin = "cupon")`.
- Rutas registradas en `docs/routes.md`.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md` (nv-61).

| Método | Ruta | Uso |
|---|---|---|
| GET | `/mis-cupones` | `?filtro=vigentes` (Disponibles) o `usados`, con cursor → `PageResult[cupón de la cartera]` `{id, cupon_id, negocio_id, titulo, tipo, valor, negocio_nombre, estado, vence_en, creado_en}` |
| GET | `/cupones/{id}` | Cupón público con `condiciones`; no vigente = 404 |
| POST | `/mis-cupones/{id}/canjear` | `{codigo, qr, expira_en}` que vive 5 min; 409 `vencido` (la app lo mapea a `Gone`), `fuera_de_horario`, `agotado` o `ya_canjeado`; 429 (10 por hora) |

Tipos: `dos_por_uno`, `monto` (`valor` en centavos), `porcentaje` (`valor` 1–100) y `regalo`.

## 4. Modelo de datos

Sin Room (D-29b). Dominio en `feature/coupons/domain/Coupons.kt`: `CouponKind`, `WalletCoupon`, `Expiry`, `RedeemCode`, `CouponsTab` y `CouponRepository`.

## 5. Seguridad y privacidad

- El código de canje es personal: no se guarda en disco ni aparece en logs (el interceptor solo registra método, ruta y código HTTP).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `test/.../feature/coupons/ui/CouponsViewModelsTest.kt`: textos de vigencia con reloj fijo; pestañas, vacío, error y datos conservados en `CouponsViewModel`; detalle desde memoria o la cartera, código con cuenta regresiva, 409 y cupón usado en `CouponDetailViewModel`.
- Data: `test/.../feature/coupons/data/CouponRepositoryImplTest.kt` con MockWebServer: `filtro` por pestaña, cursor, condiciones (y 404 sin condiciones), canje 200, `vencido` → `Gone` y `agotado` → `Conflict`.
- Security: `test/.../security/CouponsSecurityTest.kt`: el código de canje no aparece en lo que registra `RedactedLoggingInterceptor`.
- UI: capturas de Roborazzi `29_cupones`, `29_cupones_vacio`, `29_cupones_usados`, `47_det_cupon`, `det_cupon_codigo` y las tres variantes `det_cupon_*`, en claro, oscuro y letra grande.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Un PR: "feat(nv-29): cupones con la API real" (#75). Probado en teléfono contra staging el 2026-11-26 con la cuenta QA B, que tiene guardados los cupones de Restaurante Cafe Madero (`docs/pruebas-telefono.md` #26). Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-25): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Contrato conciliado e implementado el 2026-11-25, pendiente de Osmar.
