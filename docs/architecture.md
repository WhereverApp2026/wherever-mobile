# Arquitectura de la aplicación móvil — vivo (nv-04)

> Fuente de decisiones: `specs/nv-04/spec.md`. Este documento resume el mapa de módulos y las reglas de dependencia; no duplica la spec.
> Base: `nv-01` (foundation implementada), `nv-02` (molde MVVM `[planificado nv-02]`) y `nv-03` (calidad `[planificado nv-03]`). `nv-05` implementa auth (9 pantallas) y onboarding (preferencias, ubicación, amigos y permisos); el resto de pantallas y funcionalidades llega desde `nv-06+` `[planificado nv-06]`.
> Inventario visual: `docs/figma-inventory.md` (158 pantallas/estados y 36 etiquetas de la maqueta).

## 1. Módulos de producto

| Módulo | Paquete | Responsabilidad |
|---|---|---|
| Acceso y registro | `feature/auth` | Bienvenida, login, registro, recuperación y sesión |
| Inicio, feed y notificaciones | `feature/feed` | Feed, detalle y avisos |
| Amigos | `feature/friends` | Búsqueda, solicitudes y amigos |
| Explorar y lugares | `feature/explore` | Mapa, búsqueda y detalle de lugar |
| Crear plan con IA | `feature/plans` | Creación, ajuste, unión y detalle de plan |
| Compartir, grupos y chats | `feature/social` | Invitaciones, grupos y conversaciones |
| Reserva, pago y mis planes | `feature/booking` | Reserva, pago y planes del usuario |
| El día del plan | `feature/dayof` | Itinerario, llegada e incidencias |
| Recuerdos y reseñas | `feature/memories` | Fotos, recuerdos y reseñas |
| Perfil, ajustes y estados | `feature/profile` | Perfil, preferencias y estados |
| Negocio | `feature/business` | Panel y gestión de negocio |

Compartido: `core/{designsystem,network,database,security,sync,notifications,di,common,data,ui}`. Los modelos compartidos viven en `core/common/model`.

La maqueta confirma los 11 módulos y precisa tres límites: `13_Mapa` y `14_MapaFull` son del día del plan; cupones es booking; `La Vaca` y gastos son social. `79_MisRes` es “Mis reseñas”, no “Mis reservas”. El detalle está en el inventario.

## 2. Reglas de dependencia

```text
app
 ├─ navigation (contratos de rutas)
 ├─ feature/<nombre>
 │    ├─ ui      → domain
 │    ├─ data    → domain
 │    └─ domain
 └─ core/*
```

- `feature/*` solo depende de `core/*` y de los contratos de `navigation`.
- Prohibido `feature → feature`, `core → feature` y `ui → data`.
- `ui` no conoce Retrofit, Room ni DTO; `data` implementa las interfaces de `domain`.
- Hilt compone las dependencias; la UI nunca instancia repositorios.
- `core/common/model` no depende de Android.

## 3. MVVM por feature

```text
Composable → ViewModel → Repository (domain)
                              ↓
                    RepositoryImpl (data)
                       ↓             ↓
                     Room         Retrofit
```

- Un `UiState` inmutable por pantalla, expuesto con `StateFlow`; eventos de una sola vez.
- Flujo de datos: `DTO → Mapper → Entity → Domain → UiState`.
- Errores de API → `AppError`; listas → `PageResult` con cursor y límite máximo 50.
- Sin capa de casos de uso por defecto; se añade solo si una feature lo justifica.

## 4. Navegación

| Grafo | Rutas raíz | Notas |
|---|---|---|
| `auth` | `auth/*` | Fuera del shell autenticado |
| `feed` | `feed`, `feed/{id}`, `notifications` | Inicio autenticado |
| `friends` | `friends/*` | Búsqueda y solicitudes |
| `explore` | `explore`, `places/{id}` | Mapa y lugares |
| `plans` | `plans/*` | Crear, ajustar y detalle |
| `social` | `groups/*`, `chats/{id}` | Grupos e invitaciones |
| `booking` | `bookings/*` | Reserva, pago y mis planes |
| `dayof` | `dayof/{planId}` | Itinerario y llegada |
| `memories` | `memories/*` | Recuerdos y reseñas |
| `profile` | `profile/*` | Perfil y ajustes |
| `business` | `business/*` | Rol de negocio |

Rutas reales y parámetros: `docs/routes.md`; las 9 rutas auth y las 4 de onboarding están implementadas, el resto se agrega al crear cada pantalla `[planificado nv-06+]`. El auth gate decide entre `auth/*` y el onboarding local; al completar el paso 4 se entra a sesiones. No hay deep links con códigos.

## 5. Datos y offline

- Room es la fuente de verdad; la red actualiza Room y la UI lee de Room.
- Cada feature es dueña de sus tablas, DAOs y migraciones; la base es única y exporta esquema en `app/schemas`.
- Caché: último dato válido visible, refresco al abrir o pull-to-refresh, estado offline explícito.
- Sync: carga inicial, refresco por pantalla y trabajos programados `[planificado nv-06+]`; reintentos con backoff y sin duplicados.
- Conflicto por defecto: server-wins con versión/fecha del servidor.
- Migraciones explícitas con `MigrationTestHelper`; prohibido `fallbackToDestructiveMigration`.

## 6. Auth, seguridad y permisos

- Tokens cifrados en Keystore; refresh real implementado en `nv-05` con un único vuelo ante 401. Si el servidor rechaza, se limpia la sesión y se vuelve a `auth/*`; si falla la red, se conserva y se muestra sin conexión.
- Logout visible limpia tokens, cancela trabajos y reinicia el grafo; queda `[planificado nv-13]`.
- HTTPS obligatorio; `allowBackup` controlado; sin tokens ni datos personales en logs.
- Permisos en contexto; la app funciona si se niegan. Ver `docs/permissions-matrix.md` y `docs/masvs-checklist.md`.
- FCM, Maps, cámara, medios, pagos, chat e IA quedan tras interfaces; implementación real en `nv-06+` `[planificado nv-06]`.

## 7. Contratos REST (propuestos)

Base `API_BASE_URL` + `/api/v1`; `Authorization: Bearer`; errores `{codigo,mensaje,campos[]}`; paginación `{items,siguienteCursor,limite}`; `Idempotency-Key` en creación/reserva/pago; fechas ISO-8601.

| Grupo | Prefijo propuesto | Feature |
|---|---|---|
| Autenticación | `/auth` | `auth` |
| Feed, historias y avisos | `/feed`, `/posts`, `/stories`, `/notifications` | `feed` |
| Amigos y privacidad | `/friends`, `/friend-requests`, `/me/qr`, `/me/privacy` | `friends` |
| Lugares y favoritos | `/places`, `/categories`, `/favorites` | `explore` |
| Planes | `/plans`, `/plans/ai` | `plans` |
| Social, grupos y chat | `/groups`, `/chats`, `/plans/{id}/chat`, `/plans/{id}/split`, `/plans/{id}/votes` | `social` |
| Reservas, pagos y cupones | `/bookings`, `/payments`, `/me/plans`, `/me/reservations`, `/coupons` | `booking` |
| Día del plan | `/plans/{id}/live`, `/checkins` | `dayof` |
| Recuerdos y reseñas | `/plans/{id}/memories`, `/reviews`, `/me/reviews`, `/places/{id}/reviews` | `memories` |
| Perfil e insignias | `/me`, `/me/settings`, `/me/badges` | `profile` |
| Negocio | `/business/*` | `business` |

Los grupos no auth siguen como propuestas derivadas del inventario y pendientes del backend Golang y de su OpenAPI. `nv-04` no implementó clientes; en `nv-05` ya existen los de auth, refresh real y onboarding (`/me`, `/me/preferences`, `/me/onboarding/complete`, `/friend-suggestions`, `/friend-requests`). Los contratos concretos y las respuestas pendientes están en `specs/nv-05/spec.md`.

## 8. Pruebas y gates

Matriz para `nv-05` (auth y onboarding) y `nv-06+`; las pruebas se ejecutan con la implementación:
- Arquitectura (Konsist) 8 reglas de dependencia, capas, rutas, textos y dispatchers.
- Unit: ViewModel, repositorio y mappers por feature.
- Data: Room en memoria, DAOs, paginación y migraciones; MockWebServer por endpoint.
- Security: manifiesto, red, `TokenStore` y logs.
- UI: Compose Testing por pantalla con `testTag` o `strings.xml`.
- Contratos: un caso por grupo (11) con fixtures.
- Cobertura global ≥45 % `[planificado nv-03]`.

## 9. Enlaces
- Spec de arquitectura: `specs/nv-04/spec.md`.
- Spec de auth/onboarding: `specs/nv-05/spec.md`.
- Inventario visual: `docs/figma-inventory.md`.
- Rutas: `docs/routes.md`.
- Permisos: `docs/permissions-matrix.md`.
- Seguridad: `docs/masvs-checklist.md`.
- Molde: `specs/nv-02/spec.md`; `docs/feature-mold.md` `[planificado nv-02]`.
