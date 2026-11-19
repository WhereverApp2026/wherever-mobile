# NV-47 — Inicio con recomendaciones, Explorar y ficha del lugar

## T1 — Implementación
- [x] Ubicación aproximada con tarjeta previa, "Ahora no" y aviso a Ajustes tras 2 rechazos.
- [x] Inicio: clima, compañía, antojos, recomendaciones con "Por qué", "No me interesa" con deshacer, eventos y deslizar para recargar.
- [x] Explorar: búsqueda con espera, filtros, giros, paginación, recientes y vacío útil.
- [x] Ficha: fotos, datos, ruta, llamada, compartir, horario, aspectos, afluencia, menú, servicios, reseñas filtrables con "Útil" y reporte.
- [x] Favoritos: corazón en la ficha y lista desde Perfil.
- [x] Barra inferior con Explorar; versión 2 (0.2.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (2026-11-18): `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (272), `koverVerifyDebug` (74,18 %), `assembleDebug`, `assembleRelease`, scripts de puerta y Semgrep.
- [x] Contra staging: endpoints de lectura en 200, sin negocios cargados.
- [ ] `PlaceCardTest` en la CI del PR.
- [ ] En el teléfono: permiso de ubicación, Inicio, Explorar y ficha con datos reales (cuando staging tenga negocios).

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `HomeViewModelTest` (8), `PlacesViewModelsTest` (14)
- [x] Integración: `PlacesIntegrationTest` (12)
- [x] UI: `PlaceCardTest` (1)
- [x] Seguridad: `PlatformSecurityTest`, `SourceSecurityTest`
- **No hacer**: pedir ubicación precisa, guardar coordenadas, abrir mapas con una URL fija o filtrar `/buscar` en el servidor (no recibe filtros).

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
