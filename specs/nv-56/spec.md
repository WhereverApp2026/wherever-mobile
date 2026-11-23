# NV-56 — Planes sin conexión (Room)

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Fase 1 de `docs/itinerario-planes.md`. Cierra la deuda de `specs/nv-48` ("Sin cambios en Room") y lo que piden `nv-23`, `nv-24` y `nv-28` sobre el uso sin red. Base de `nv-30`.
> Responsable: Borre. Rama: `feat/nv-56-planes-room`, desde `main` actualizado. Revisada contra el código el 2026-11-22 (ver "Revisión" al final).

## 1. Objetivo

Que los planes guardados se vean sin internet. Room pasa a ser la fuente de verdad de Planes, como pide la constitución: el repositorio guarda lo que llega de la API y la UI lee de Room. Los planes de hoy y de mañana se guardan completos con su paquete sin conexión, para el día del plan.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU.** Como usuario quiero ver mis planes aunque no tenga internet.

- [ ] Mis planes muestra las tres pestañas desde Room, en el orden de la API, y las actualiza detrás cuando hay red.
- [ ] El detalle de un plan ya abierto se ve sin red, con paradas, horarios, traslados y costos.
- [ ] Sin red, una acción que escribe (editar, quitar, agregar, compartir, repetir, borrar, generar) se intenta igual; si falla, se muestra el error y el plan en pantalla no cambia. No se encola.
- [ ] Los planes de hoy y mañana guardan también la dirección y las coordenadas de cada parada (`paquete-offline`).
- [ ] El último plan generado sin guardar sobrevive a cerrar la app y se recupera sin red.
- [ ] Al cerrar sesión no queda ningún plan en el teléfono.
- [ ] Desde `89_SinConexion` se llega a los planes guardados.

### UI

Sin pantallas nuevas. Cambian:

| Pantalla | Cambio |
|---|---|
| `24_MisPlanes` (`MyPlansScreen`) | Lee de Room; aviso "Sin conexión: ves lo último guardado" con la hora de la última actualización |
| `39_Plan` (`PlanDetailScreen`) | Lee de Room; mismo aviso; las acciones siguen visibles |
| Resultado del asistente (`CreatePlanScreen`) | Recupera el último borrador guardado |
| `89_SinConexion` (`AppStatusScreens`) | Botón "Ver mis planes guardados": cierra la capa (como "Atrás" hoy) y abre Mis planes |

Textos en `strings.xml`; colores desde `core/designsystem`.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Primero lo que hay en Room; la red actualiza detrás sin vaciar la lista. |
| Vacío | Room vacío y sin red: `41_PlanesVacio` con el aviso de sin conexión. |
| Error | Falla la actualización o una edición: se conserva lo de Room y se muestra el error con Reintentar. |
| Sin red | Lectura desde Room con el aviso; las escrituras se intentan y fallan con su error. |
| Éxito | Room actualizado con lo que devolvió la API. |

### Validaciones

- Cada respuesta de `GET /planes` para una pestaña reemplaza la lista de esa pestaña (qué planes y en qué orden). No borra los planes ni sus paradas: un plan que sale de una pestaña sigue en Room si se abrió o está en otra.
- La lista puede traer las paradas vacías o incompletas: al guardarla solo se actualizan los datos generales del plan (nombre, fecha, costos, `version`); las paradas y traslados se reemplazan solo con `GET /planes/{id}`, con el paquete sin conexión o con la respuesta de una edición.
- El paquete sin conexión solo se pide para planes de hoy o mañana en la hora de México (`America/Mexico_City`, como la API; reloj inyectado). Un 422 `fuera_de_ventana` no es error para la persona.
- El paquete se pide como máximo una vez por plan cada 30 minutos (la API limita a 30 por hora por usuario) y con `If-None-Match`; un 304 no reescribe nada.
- Tras cada edición exitosa se guarda en Room el plan que devolvió la API, con su `version`. `DELETE` exitoso y 404 al abrirlo lo borran de Room. `clonar` guarda el plan nuevo.
- Las tablas en Room borran planes que ya no aparecen en ninguna pestaña y no se abrieron en 30 días, para no crecer sin límite.

### No-alcance

- Cola de escrituras sin red: la API pide `If-Match` en cada edición y un 409 obliga a recargar; encolar cambios daría conflictos.
- Apagar botones con el monitor de red: la decisión de nv-16/nv-40 es que el monitor solo informa y que siempre se intenta la llamada (en datos móviles daba falsos "sin conexión").
- Paginación de Mis planes: nv-48 pide 30 por pestaña sin cursor; se mantiene. Ver más de 30 queda para otra spec.
- Descarga en segundo plano (WorkManager) del paquete la noche anterior: va con `nv-30` si se necesita.
- Reservas y su código QR sin red: los trae `paquete-offline`, pero son de `nv-27`; esta spec no los guarda.
- Mapas sin conexión (D25).

## 3. Contratos (API y navegación)

### Navegación

Sin rutas nuevas. `89_SinConexion` navega a `MyPlansRoute` (ya registrada); es el botón de entrada que permite la excepción de `CONTRIBUTING.md`.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md`. Los mismos que usa nv-48, más el paquete:

| Método | Ruta | Uso en esta spec |
|---|---|---|
| GET | `/planes` | `?pestana=&limite=30` → orden y datos generales por pestaña |
| GET | `/planes/{id}` | Plan completo con paradas, traslados y `version`; 404 lo borra de Room |
| GET | `/planes/{id}/paquete-offline` | Plan y fichas de lugares de hoy o mañana; `ETag` `"{version}-{sync_seq}"`, 304 con `If-None-Match`, 422 `fuera_de_ventana`, 429 |
| POST / PATCH / PUT / DELETE | `/planes`, `/planes/{id}`, `/planes/{id}/paradas…`, `/planes/{id}/clonar` | Sin cambios de contrato; la respuesta se guarda en Room |

Errores → `AppError` como hoy (`safeApiCall`).

## 4. Modelo de datos

Room de la versión 5 a la 6 con `MIGRATION_5_6`; esquema `app/schemas/com.wherever.mobile.core.database.WhereverDatabase/6.json` exportado. Solo tablas nuevas: no toca `user_profile` ni `group_message`.

| Tabla | Columnas | Notas |
|---|---|---|
| `plan` | `id` (PK), `name`, `date`, `people`, `budget`, `mobility` (valor de la API), `totalCost`, `costPerPerson`, `isPublic`, `version`, `detailLoaded`, `updatedAt`, `openedAt` (nullable), `offlineEtag` (nullable), `offlineFetchedAt` (nullable) | `detailLoaded` dice si ya hay paradas completas |
| `plan_tab_entry` | `tab`, `planId` (FK → `plan`, cascada), `position`; PK (`tab`, `planId`) | Orden de cada pestaña; un plan puede estar en más de una |
| `plan_stop` | `id` (PK), `planId` (FK → `plan`, cascada, índice), `position`, `placeId`, `name`, `category`, `arrives`, `minutes`, `price`, `why`, `address`, `lat`, `lng` | Dirección y coordenadas solo con paquete sin conexión |
| `plan_transfer` | `planId` (FK, cascada), `fromStopId`, `toStopId`, `minutes`, `km`; PK (`planId`, `fromStopId`) | |
| `plan_draft` | `id` (PK, siempre `last`), `json`, `savedAt` | El último `PlanDraft` como JSON con `ApiJson`; se borra al guardarlo como plan |

- `PlanDao` con `Flow` por pestaña (join con `plan_tab_entry` ordenado por `position`) y por id, y `@Transaction` para reemplazar una pestaña o un plan completo.
- `PlansRepository` expone `observeTab(tab)` y `observe(id)` como `Flow`, más `refreshTab(tab)` y `refresh(id)`; las ediciones existentes devuelven `Result<Plan>` y guardan en Room antes de responder.
- Los montos siguen en pesos enteros (`Int`), como en nv-48 y en la API.
- `MIGRATION_5_6` solo hace `CREATE TABLE` e índices; su SQL se copia del `6.json` generado para que `runMigrationsAndValidate` coincida.

## 5. Seguridad y privacidad

- Los planes son dato personal: `PlansCleaner` (un `SessionCleaner`, como `GroupMessagesCleaner`) borra las cinco tablas. Corre en todo `clearLocalSession()`: cerrar sesión, cerrar todas las sesiones, eliminar la cuenta y sesión vencida.
- El borrador guardado no incluye la frase escrita ni el ánimo: `PlanDraft` no los tiene y `ParametersDto` no trae `animo`. Si el contrato los agrega, no se guardan.
- `paquete-offline` responde `Cache-Control: private, no-store`: ninguna caché HTTP lo guarda (OkHttp no tiene caché configurada). Room sí lo guarda, que es el propósito del endpoint; queda dentro del almacenamiento de la app, sin respaldo (`allowBackup` controlado, constitución §6).
- No se guarda la ubicación de la persona: las coordenadas son de los negocios.
- Sin cuerpos de planes, tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- **Unit:** `MyPlansViewModel` y `PlanDetailViewModel` muestran lo de Room y luego lo actualizado; con error (también sin red) conservan los datos; el aviso sale del monitor sin frenar llamadas; mapeos entidad ↔ dominio (orden de paradas, traslados, movilidad).
- **Data (JVM, MockWebServer y DAO falso, como el resto del proyecto):** `refreshTab` reemplaza orden y datos generales sin borrar paradas de un plan ya abierto; una edición guarda la respuesta con su `version`; `DELETE` y 404 borran el plan; paquete con 200 guarda direcciones, 304 no reescribe, 422 `fuera_de_ventana` se ignora y no se pide dos veces en 30 minutos; el borrador se guarda y se borra al guardar el plan.
- **Data (instrumentada, Room real):** `PlanDao` por pestaña en orden, un plan en dos pestañas, cascada al borrar, `@Transaction` de reemplazo y limpieza de planes viejos.
- **Migración (instrumentada):** `MigrationTest` 5 → 6 con `runMigrationsAndValidate`, sin perder `user_profile` ni `group_message`.
- **Security:** `clearLocalSession()` deja vacías las cinco tablas; `plan_draft` no contiene la frase ni el ánimo; ninguna petición de planes se registra con su cuerpo en logs; `ConnectionSecurityTest` sigue verde (el monitor no entra en `feature/plans/data`).
- **UI (opcional):** Mis planes sin red muestra la lista y el aviso.
- **Arquitectura:** `ArchitectureTest`: `core/database` solo importa `*Entity`/`*Dao` de `feature/plans/data`.
- Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Un PR: "feat(nv-56): planes sin conexión con Room".

**Antes de la rama, por `CONTRIBUTING.md` ("Lo que no se toca"):** abrir un Issue para Jorge, porque esta spec necesita una línea en `core/di/AppModule.kt` (`.addMigrations(MIGRATION_5_6)`) y subir la versión en `app/build.gradle.kts`. Se pide que lo haga Jorge o que autorice el cambio en el PR. `MainActivity` (cerrar la capa y navegar) también se le avisa.

Compuertas una por una con JDK 17 (detekt) y el JBR (pruebas). Verificación en teléfono: abrir Mis planes y un plan con red, activar modo avión, cerrar y abrir la app, cerrar la capa de Sin conexión con "Ver mis planes guardados" y ver lo mismo con el aviso; cerrar sesión y comprobar que no queda nada. Requiere un plan real en staging; si staging sigue sin negocios, queda anotado en `docs/pruebas-telefono.md`. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Revisión (2026-11-22)

Cambios tras cotejar el borrador con el código:

- Las escrituras ya no se apagan con el monitor de red: contradecía la decisión de nv-16/nv-40 (el monitor solo informa y siempre se intenta la llamada).
- `tab` como columna perdía el orden y no dejaba un plan en dos pestañas: pasa a la tabla `plan_tab_entry`.
- `GET /planes` devuelve `PlanDto` sin garantía de paradas: la lista ya no reemplaza paradas de un plan abierto (`detailLoaded`).
- `89_SinConexion` es una capa que tapa toda la app y solo se cierra con "Atrás": el botón nuevo la cierra antes de navegar.
- Registrar la migración toca `core/di/AppModule.kt` y la versión toca `app/build.gradle.kts`, que no se tocan sin Issue: agregado a §7.
- La limpieza cubre todos los caminos de `clearLocalSession()`, no solo cerrar sesión.
- La lección de `@ColumnInfo(defaultValue)` era para columnas agregadas; con tablas nuevas, el SQL se copia del `6.json`.
- Se fija la zona `America/Mexico_City` para "hoy o mañana", como la API, y un límite de crecimiento de 30 días.

## Aprobación
- [ ] Revisor: Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** Borrador revisado (Borre, 2026-11-22), pendiente de Osmar.
