# NV-04 — Arquitectura de la aplicación móvil

## Contexto
Arquitectura de producto que se apoya en la base de `nv-01`, el molde de `nv-02` y la puerta de calidad de `nv-03`: módulos, MVVM, navegación, datos offline, contratos REST, autenticación, seguridad, UX y matriz de pruebas. No implementa pantallas ni código; las funcionalidades y pantallas llegan desde `nv-05+`; auth/onboarding ya está especificado en `specs/nv-05/spec.md`. **Depende de**: nv-03.

La maqueta Figma fue inventariada en `docs/figma-inventory.md`: 158 pantallas/estados y 36 etiquetas que confirman los 11 módulos. Este spec fija la arquitectura por módulos y deja los flujos de pantalla para las specs de `nv-05+`, que partirán del inventario. El backend Golang no está disponible: los contratos de §Contratos son propuestas y no se consideran definitivos hasta su OpenAPI.

## Requisitos
1. R1 — Fijar el mapa de 11 módulos de producto y sus paquetes `feature/<nombre>`, con reglas de dependencia verificables: `feature → core` y contratos de navegación; nunca `feature → feature`, `core → feature` ni `ui → data`.
2. R2 — Fijar el contrato MVVM por feature: un `UiState` por pantalla, `StateFlow`, eventos de una sola vez, Composable sin lógica de negocio, ViewModel sin Retrofit/Room y repositorio como interfaz de dominio.
3. R3 — Fijar la navegación tipada por grafos de módulo, con auth gate, destinos iniciales y deep links; las rutas reales de pantalla se registran en `docs/routes.md` al crear cada spec (auth ya en `nv-05`; resto `[planificado nv-06+]`).
4. R4 — Fijar la arquitectura offline-first: Room como fuente de verdad, propiedad de tablas por feature, flujo `DTO → Mapper → Entity → Domain → UiState`, caché, paginación por cursor, sincronización y migraciones explícitas sin `fallbackToDestructiveMigration`.
5. R5 — Fijar las convenciones de contrato REST del cliente y el catálogo propuesto de grupos de endpoints por módulo, marcados como pendientes del backend Golang; ningún endpoint es definitivo hasta la revisión de OpenAPI.
6. R6 — Fijar autenticación y sesión: tokens cifrados con Keystore, refresh único ante 401, logout/reset, permisos en contexto y degradación funcional si se niegan; refresh real en `nv-05` y FCM, mapas, cámara y medios como interfaces diferidas `[planificado nv-06+]`.
7. R7 — Fijar las reglas transversales de UX: design system, textos en `strings.xml`, accesibilidad, estados de carga/vacío/error/sin red, formatos de fecha/moneda y observabilidad con `request_id` y logs redactados.
8. R8 — Fijar la matriz de pruebas y los gates para `nv-05+`: Arquitectura, Unit, Data, Security, UI, contratos y migraciones; cobertura global del 45 % provisional y evidencia SDD por feature.

## Diseño

### Mapa de módulos y propiedad
| Módulo Figma | Paquete | Flujo principal | Contratos propuestos |
|---|---|---|---|
| Acceso y registro | `feature/auth` | Bienvenida, login, registro, recuperación | `/auth/*` |
| Inicio, feed y notificaciones | `feature/feed` | Feed, historias, detalle, notificaciones | `/feed`, `/posts`, `/stories`, `/notifications` |
| Amigos | `feature/friends` | Búsqueda, solicitudes, QR, privacidad | `/friends`, `/friend-requests`, `/me/qr`, `/me/privacy` |
| Explorar y lugares | `feature/explore` | Lugares, mapa, favoritos y sello | `/places`, `/categories`, `/favorites` |
| Crear plan con IA | `feature/plans` | Crear, ajustar y unirse a plan | `/plans`, `/plans/ai` |
| Compartir, grupos y chats | `feature/social` | Invitar, grupos, chat, votos, vaca | `/groups`, `/chats`, `/plans/{id}/chat`, `/plans/{id}/split`, `/plans/{id}/votes` |
| Reserva, pago y mis planes | `feature/booking` | Reserva, pago, cupones, mis planes | `/bookings`, `/payments`, `/me/plans`, `/me/reservations`, `/coupons` |
| El día del plan | `feature/dayof` | Ruta, llegada, navegación, check-in | `/plans/{id}/live`, `/checkins` |
| Recuerdos y reseñas | `feature/memories` | Fotos, postales, recuerdos, reseñas | `/plans/{id}/memories`, `/reviews`, `/me/reviews`, `/places/{id}/reviews` |
| Perfil, ajustes y estados | `feature/profile` | Perfil, insignias, ajustes, estados | `/me`, `/me/settings`, `/me/badges` |
| Negocio | `feature/business` | Panel y gestión de negocio | `/business/*` |

Paquetes compartidos: los de `nv-01` (`core/designsystem`, `core/network`, `core/database`, `core/security`, `core/sync`, `core/notifications`, `core/di`) más `core/common`, `core/data` y `core/ui` de `nv-02`. Los modelos compartidos entre features viven en `core/common/model`; no se crea una capa de casos de uso salvo que una feature lo justifique en su spec.

### Reglas de dependencia
- `app` compone `navigation`, features y módulos `core`; no contiene lógica de negocio.
- `feature/<nombre>` puede depender de `core/*` y de los contratos de `navigation`; no puede depender de otra feature.
- Dentro de una feature: `ui → domain`; `data → domain`; `ui` no conoce `data`, Retrofit ni Room.
- `core/*` no depende de `feature/*`; `core/common/model` no depende de Android.
- La composición de dependencias se hace con Hilt; prohibido el acceso directo a implementaciones desde la UI.

### Contrato MVVM por feature
```text
Composable → ViewModel → Repository (interfaz de domain)
                              ↓
                    RepositoryImpl (data)
                       ↓             ↓
                     Room         Retrofit
```
- `UiState` inmutable por pantalla; el ViewModel lo expone con `StateFlow` y emite eventos de una sola vez.
- Los Composables solo pintan y envían eventos; no calculan reglas de negocio.
- Los DTO viven en `data`; los mappers convierten a entidad Room y a modelo de dominio. La UI nunca consume DTO ni entidad.
- El repositorio devuelve `Result<T>`/`PageResult<T>` de `nv-02`; los errores se mapean a `AppError`.

### Navegación
| Grafo | Rutas raíz previstas | Notas |
|---|---|---|
| `auth` | `auth/*` | Bienvenida, login, registro y recuperación; fuera del shell principal |
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

Las rutas concretas y sus parámetros se agregan a `docs/routes.md` con cada spec de pantalla; las de auth ya están en `nv-05` y el resto `[planificado nv-06+]`. Los deep links no llevan tokens ni datos sensibles, salvo el código de reset de un solo uso documentado en `nv-05`. El auth gate decide entre `auth/*` y el shell principal.

### Datos y offline
- Room es la fuente de verdad; cada feature declara sus entidades, DAOs y migraciones. La base de datos es única y su esquema se exporta en `app/schemas`.
- Cada feature es dueña de sus tablas; las lecturas cruzadas usan IDs estables y modelos de `core/common/model`, no joins entre features.
- Política de caché por recurso: se muestra lo último válido de Room, se refresca al abrir o al hacer pull-to-refresh y se marca el estado offline. Las escrituras offline se limitan a las que el backend pueda reconciliar; el resto se bloquea con mensaje.
- Sincronización: carga inicial, refresco por pantalla y trabajos programados `[planificado nv-06+]`; reintentos con backoff y sin duplicados. El conflicto por defecto es server-wins con versión/fecha del servidor.
- Paginación por cursor `PageResult{items, siguienteCursor, limite}` con límite máximo 50; sin offset.
- Migraciones explícitas y probadas con `MigrationTestHelper`; prohibido `fallbackToDestructiveMigration`.

### Autenticación, sesión y permisos
- Tokens en `TokenStore` cifrado con Keystore; el access token nunca se registra en logs ni se guarda en texto plano.
- `Authenticator` reintenta una sola vez con el refresh real de `nv-05`; si el servidor rechaza, limpia sesión y navega a `auth/*`; si falla la red, conserva sesión y muestra sin conexión.
- Logout revoca/localiza la sesión, limpia tokens, cancela trabajos y reinicia el grafo.
- Permisos en contexto: notificaciones, ubicación y cámara se piden al usar la función; la app funciona si se niegan. La matriz vive en `docs/permissions-matrix.md`.
- FCM, Maps, cámara y subida de medios se consumen tras interfaces; sus implementaciones reales quedan para `nv-06+` `[planificado nv-06]`.

### UX, design system y observabilidad
- Colores, tipografía, formas y componentes solo desde `core/designsystem`; textos visibles en `strings.xml`.
- Cada pantalla cubre carga, contenido, vacío, error y sin red; un solo botón principal naranja por pantalla.
- Accesibilidad: contraste suficiente, áreas táctiles de 48 dp, `contentDescription` en iconos informativos.
- Fechas ISO-8601 en el contrato; la UI las formatea en la zona del dispositivo. Moneda y locale se confirman con el backend.
- Logs con `request_id` y redacción; sin Crashlytics ni analítica en esta etapa.

## Contratos
Convenciones del cliente, todas propuestas y pendientes del backend Golang:
- Base `API_BASE_URL` + `/api/v1`; autenticación `Authorization: Bearer <access>`.
- Errores `{codigo, mensaje, campos[]}` → `AppError` (`Network`, `Timeout`, `Unauthorized`, `Validation`, `RateLimited`, `Server`, `Unknown`).
- Paginación `{items, siguienteCursor, limite}` con límite máximo 50.
- `Idempotency-Key` en POST de creación, reserva y pago; el backend debe confirmar soporte.
- Fechas ISO-8601 UTC; identificadores opacos en rutas; sin datos sensibles en query strings.
- Subida de medios, transporte de chat y creación de plan con IA quedan como interfaces `[planificado nv-12]`; no se asume multipart, WebSocket ni streaming.

| Grupo | Prefijo propuesto | Feature | Estado |
|---|---|---|---|
| Autenticación | `/auth` | `auth` | Propuesto; contratos y refresh real en `nv-05` |
| Feed, historias y avisos | `/feed`, `/posts`, `/stories`, `/notifications` | `feed` | Propuesto |
| Amigos y privacidad | `/friends`, `/friend-requests`, `/me/qr`, `/me/privacy` | `friends` | Propuesto |
| Lugares y favoritos | `/places`, `/categories`, `/favorites` | `explore` | Propuesto |
| Planes | `/plans`, `/plans/ai` | `plans` | Propuesto; IA `[planificado nv-08]` |
| Social, grupos y chat | `/groups`, `/chats`, `/plans/{id}/chat`, `/plans/{id}/split`, `/plans/{id}/votes` | `social` | Propuesto; chat `[planificado nv-12]` |
| Reservas, pagos y cupones | `/bookings`, `/payments`, `/me/plans`, `/me/reservations`, `/coupons` | `booking` | Propuesto; pago `[planificado nv-09]` |
| Día del plan | `/plans/{id}/live`, `/checkins` | `dayof` | Propuesto |
| Recuerdos y reseñas | `/plans/{id}/memories`, `/reviews`, `/me/reviews`, `/places/{id}/reviews` | `memories` | Propuesto |
| Perfil e insignias | `/me`, `/me/settings`, `/me/badges` | `profile` | Propuesto |
| Negocio | `/business/*` | `business` | Propuesto |

Rutas: ver `docs/routes.md`. Inventario: ver `docs/figma-inventory.md`. Permisos: ver `docs/permissions-matrix.md`. Seguridad: ver `docs/masvs-checklist.md`. Un cambio de contrato con la API exige spec y revisión; estos grupos no autorizan implementación ni pruebas contra el backend.

## Tests
En `nv-04` no hay pruebas ejecutables: es una spec documental. La matriz obligatoria para `nv-05+` es:
- Arquitectura (Konsist) 8: dependencias entre features, `core` sin features, ViewModel sin Retrofit/Room, Composable sin repositorio, rutas tipadas, textos fuera de `strings.xml`, sin `Dispatchers.IO` fijo y DTO fuera de UI.
- Unit: ViewModel, repositorio y mappers por feature; estados y eventos con Turbine.
- Data: Room en memoria, DAOs, paginación y migraciones con `MigrationTestHelper`; MockWebServer para cada endpoint.
- Security: manifiesto, `networkSecurityConfig`, `TokenStore` y ausencia de tokens en logs.
- UI: Compose Testing por pantalla, búsqueda por `testTag` o texto de `strings.xml`.
- Contratos: un caso por grupo de endpoint (11) con fixtures; sin backend real.
- Cobertura global ≥ 45 % con `scripts/check_coverage.sh` `[planificado nv-03]`.

## Criterios
- `docs/architecture.md` existe y cubre los 11 módulos, dependencias, MVVM, navegación, datos, auth y contratos.
- `docs/figma-inventory.md` cubre los 158 frames y los 11 módulos; las variantes están agrupadas y los solapamientos documentados.
- Las reglas R1-R8 están mapeadas en `plan.md`, `task.md` y `checklists/gates.md`.
- No hay referencias a `nv-04` como funcionalidad o pantalla; auth está especificado en `nv-05` y las demás implementaciones apuntan a `nv-06+` `[planificado nv-06]`.
- Los contratos están marcados como propuestos y pendientes del backend; no se declaran definitivos.
- `AGENTS.md`, `README.md`, `CLAUDE.md` y `Memoria.md` reflejan la nueva fase y el mapa `nv-04` arquitectura / `nv-05+` producto.
- `docs/routes.md`, `docs/permissions-matrix.md` y `docs/masvs-checklist.md` quedan alineados.
- `git diff --check` sin errores y revisión manual de las 8 secciones.
- `scripts/check_sdd.sh` `[planificado nv-01]` se ejecutará cuando exista; hoy la evidencia es la revisión manual.

## Límites
Sin pantallas, código Kotlin, tests ejecutables, CI, scripts, FCM real, Maps real, cámara, pagos, chat en tiempo real ni IA implementada. Sin contratos definitivos con el backend Golang. Sin analítica ni Crashlytics. La validación visual de la maqueta y los flujos de pantalla quedan para `nv-05+`; auth ya está especificado en `nv-05` y el resto en `nv-06+` `[planificado nv-06]`.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador con evidencia (documental; sin código)
- **Revisores:** (pendiente de nombre)
- **Fecha:** 2026-11-02
- **Detalle:** especificada y verificada documentalmente; sin código, tests ni pantallas. Incluye el inventario de 158 frames en `docs/figma-inventory.md`. La evidencia está en `specs/nv-04/checklists/gates.md` + `Memoria.md`.
