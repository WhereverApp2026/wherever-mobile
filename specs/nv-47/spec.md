# NV-47 — Inicio con recomendaciones, Explorar y ficha del lugar

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Reemplaza el "Inicio provisional" de `specs/nv-17` y cubre la parte de lista de `specs/nv-19`, `nv-21` y `nv-22`. Rama `feat/inicio-recomendaciones`.

## 1. Objetivo

La app publicada solo tenía cuenta y perfil: el Inicio era un saludo sin contenido. Con la API terminada, la persona ya puede descubrir lugares: Inicio con recomendaciones reales, clima, compañía y eventos; Explorar con búsqueda y filtros; la ficha completa de cada negocio; y sus favoritos.

Historias: US-025, US-032, US-037, US-047, US-053, US-062, US-030 (Inicio); US-004, US-009 (ubicación); US-027, US-028, US-034, US-187 (Explorar); US-067, US-068, US-069, US-070, US-071, US-072, US-073, US-074, US-111, US-121 (ficha); US-029 (favoritos).

## 2. Alcance / No-alcance

### Alcance
- **Inicio (10_Inicio)** con `GET /inicio`:
  - Encabezado con ciudad, clima (temperatura y estado) y aviso si el clima es adverso.
  - Caja "¿Qué se te antoja?", que abre Explorar.
  - Chips de compañía (solo, en pareja, con amigos, en familia) que se mandan en `?compania=`. Tocar la activa la quita.
  - Chips de antojo según la hora. Cada uno abre Explorar con su giro.
  - "Para ti": 5 recomendaciones con su "Por qué" y el botón "No me interesa", con "Deshacer" en un snackbar (`POST`/`DELETE /recomendaciones/{id}/no-me-interesa`).
  - "Esta semana": eventos, ferias y tianguis de 7 días (`GET /eventos`), solo con ubicación, porque la API la pide.
  - Deslizar hacia abajo recarga.
- **Ubicación aproximada (US-004, US-009)**:
  - Una tarjeta en Inicio explica para qué se usa antes del diálogo del sistema. Solo se pide `ACCESS_COARSE_LOCATION`, mientras se usa la app.
  - "Ahora no" la oculta. Tras 2 rechazos queda solo un aviso que abre Ajustes del sistema. Al volver de Ajustes con el permiso, Inicio recarga con ubicación.
  - Sin permiso, Inicio pide recomendaciones generales y Explorar no muestra el filtro de distancia.
  - La ubicación sale de `LocationManager` del sistema, sin Play Services.
- **Explorar (18_Explorar)**:
  - Búsqueda por nombre o palabra (`GET /lugares/buscar`), con espera de 400 ms al escribir y mínimo 2 letras.
  - Filtros: abierto ahora, a menos de 2 km (con ubicación), precio hasta $ a $$$, mejor calificados y giro (`GET /categorias`). Con búsqueda, los filtros se aplican sobre los resultados, porque `/buscar` no los recibe.
  - Lista paginada por cursor (`GET /lugares`), con "Ver más lugares".
  - Búsquedas recientes, con Borrar, y lugares vistos recientemente (`/busquedas/recientes`, `/lugares/recientes`).
  - Vacío útil (US-187): "No encontramos lugares", con "Quitar filtros".
- **Ficha del lugar (20_FichaNegocio)** con `GET /lugares/{id}`:
  - Fotos, nombre, giro, precio, abierto o cerrado, calificación, verificado y Sello de Excelencia, gasto promedio y dirección.
  - Cómo llegar (abre la app de mapas con `geo:`), Llamar (marcador del teléfono) y Compartir (texto con nombre y dirección).
  - Corazón de favoritos (US-029): cambia al instante y vuelve si la API falla.
  - Acerca de, horario de la semana empezando por hoy, calificación por aspecto (sabor, atención, ambiente, espera y precio-calidad).
  - Afluencia de hoy de 7:00 a 23:00 (`/afluencia`), con la hora actual resaltada.
  - Menú con precios (`/menu`), servicios, accesibilidad y opciones de dieta.
  - Reseñas paginadas (`/resenas`), filtrables por estrellas y "Más recientes", con la insignia "Visitó el lugar" (todas vienen de un check-in verificado, backend nv-62), la respuesta del negocio y "Útil" (`PUT /resenas/{id}/util`).
  - "Reportar información incorrecta" (`POST /lugares/{id}/reportes`) con motivo y detalle, obligatorio en "Otro".
  - Al abrirla se registra la vista (`POST /lugares/{id}/vistas`) con su origen (inicio o búsqueda), para "Vistos recientemente".
- **Mis favoritos**: desde Perfil, lista con quitar (`GET`/`DELETE /favoritos`).
- **Barra inferior**: Inicio, Explorar y Perfil. Al cambiar de pestaña se guarda su estado.
- Versión `versionCode 2`, `versionName 0.2.0`.

### No-alcance
- Mapa con pines (US-026): falta decidir el proveedor de mapas y su licencia (D25). Explorar queda como lista.
- Cupones en la ficha (US-196): la API no tiene una ruta pública de cupones por negocio.
- Reservar desde la ficha: llega con su spec de reservas.
- Caché sin conexión de Inicio (US-031): llega con la mejora de modo sin conexión.
- Detalle de evento: la tarjeta solo informa.

## 3. Contratos (API y navegación)

Contratos en `wherever-backend` `docs/endpoints.md` (nv-20 a nv-25, nv-54, nv-56, nv-62, nv-65 a nv-67). Rutas nuevas en `docs/routes.md`: `ExploreRoute(query, category)`, `PlaceRoute(id, origin)` y `FavoritesRoute`. `HomeRoute` deja de ser provisional.

| Pantalla | Endpoints |
|---|---|
| Inicio | `GET /inicio`, `GET /eventos`, `POST`/`DELETE /recomendaciones/{id}/no-me-interesa` |
| Explorar | `GET /categorias`, `GET /lugares`, `GET /lugares/buscar`, `GET`/`DELETE /busquedas/recientes`, `GET /lugares/recientes` |
| Ficha | `GET /lugares/{id}`, `/afluencia`, `/menu`, `/resenas`, `POST /vistas`, `POST /reportes`, `PUT /resenas/{id}/util`, `GET /favoritos/ids`, `PUT`/`DELETE /favoritos/negocio/{id}` |
| Mis favoritos | `GET /favoritos?tipo=negocio`, `DELETE /favoritos/negocio/{id}` |

Contra staging (2026-11-18, 11:50): `/health`, `/ready` y `/ping` dan 200. `/inicio`, `/categorias`, `/lugares`, `/buscar`, `/eventos` y `/planes/populares` responden 200, pero vacíos: staging no tiene negocios cargados (`conteo: 0` en los 6 giros). No hubo errores con `traza_id`. Las pantallas muestran su estado vacío hasta que un administrador cargue la semilla del backend (`docs/semilla-negocios.csv`, `POST /admin/negocios/importar`).

## 4. Modelo de datos

Sin cambios en Room. Nuevos modelos de dominio en `feature/places/domain/Places.kt` (`PlaceCard`, `PlaceDetail`, `Review`, `Traffic`…) y `feature/home/domain/HomeFeed.kt`. Tres repositorios: `PlacesRepository`, `ReviewsRepository` y `FavoritesRepository`. `core/location` tiene `GeoPoint`, `LocationProvider` y `LocationPermissionPreferences`.

## 5. Seguridad y privacidad

- Permiso nuevo: `ACCESS_COARSE_LOCATION`, con fila en `docs/permissions-matrix.md`. Nunca se pide la ubicación precisa.
- La ubicación solo viaja como `lat`/`lng` en la petición. No se guarda en el teléfono ni en logs.
- `SharedPreferences "permisos"` guarda un entero (rechazos) y un booleano ("Ahora no"), sin datos personales.
- Las fotos se cargan con Coil por HTTPS (`network_security_config` sin cambios).
- Excepción de stack: `coil-network-okhttp`, el módulo de red de Coil, que ya estaba en el catálogo. Coil baja de 3.6.3 a 3.3.0, porque 3.6.3 exige `compileSdk 37` y este equipo tiene 36 (igual que en la fase del 2026-11-05).

## 6. Tests / Gates

- **Unit:**
  - `test/.../feature/home/ui/HomeViewModelTest.kt` (8): sin permiso pide ubicación y carga lo general; con permiso manda `lat`/`lng` y trae eventos; compañía que se quita al tocarla otra vez; "No me interesa" con deshacer en su lugar; descarte rechazado regresa; dos rechazos dejan el aviso de Ajustes; volver de Ajustes recarga; error y reintento al deslizar.
  - `test/.../feature/places/ui/PlacesViewModelsTest.kt` (14): categoría desde Inicio, espera al escribir, una letra no busca, filtros que se encienden y apagan, vacío y Quitar filtros, Ver más, filtros sobre la búsqueda, ficha con vista y día de hoy, favorito optimista que vuelve si falla, reporte "Otro" con detalle, 409 en el diálogo, filtro de estrellas, "Útil" que se desmarca si falla, y favoritos.
- **Data:** `test/.../integration/PlacesIntegrationTest.kt` (12) con MockWebServer: parámetros de `/inicio` con y sin ubicación, clima y "por qué", `POST`/`DELETE` de "No me interesa", filtros y cursor de `/lugares`, radio sin ubicación, ficha (dirección, fotos ordenadas, aspectos), 404 → `NotFound`, reseñas con estrellas/orden/cursor, favoritos con `PUT`/`DELETE`, cuerpo del reporte, afluencia vacía y búsqueda recortada con el origen de la vista.
- **UI:** `androidTest/.../feature/places/ui/PlaceCardTest.kt` (1): la tarjeta muestra giro, precio, distancia y "Cerrado", y abre la ficha. Corre en la CI del PR.
- **Security:** `PlatformSecurityTest` (permisos: `INTERNET` y `ACCESS_COARSE_LOCATION`) y `SourceSecurityTest` (SharedPreferences sin texto ni tokens) actualizados. `ArchitectureTest` sigue verde con los paquetes nuevos.

## 7. Plan de entrega

PR "feat: Inicio con recomendaciones, Explorar y ficha del lugar". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
