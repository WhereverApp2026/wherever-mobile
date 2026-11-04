# NV-04 — Arquitectura de la aplicación móvil

## T1 — Módulos, dependencias y MVVM (R1, R2)
- **Crear**: mapa de los 11 módulos en `docs/architecture.md`; reglas de dependencia y plantilla de feature en `specs/nv-04/spec.md`.
- **Verificar**: los 11 paquetes `feature/*` están cubiertos y las reglas prohíben `feature → feature`, `core → feature` y `ui → data`.
- **No hacer**: crear paquetes, código ni Gradle.

## T2 — Navegación (R3)
- **Crear**: grafos de navegación y rutas raíz en `specs/nv-04/spec.md` y `docs/architecture.md`; nota de rutas reales en `docs/routes.md`.
- **Verificar**: cada módulo tiene grafo, auth gate y deep links sin datos sensibles.
- **No hacer**: registrar pantallas concretas ni parámetros definitivos.

## T3 — Datos, offline y sincronización (R4)
- **Crear**: arquitectura Room/MVVM, propiedad de tablas, caché, paginación, sync, conflictos y migraciones en `specs/nv-04/spec.md`.
- **Verificar**: Room es fuente de verdad; `PageResult` límite 50; sin migración destructiva; flujo DTO→Entity→Domain→UiState documentado.
- **No hacer**: entidades, DAOs, base de datos ni migraciones reales.

## T4 — Contratos REST y backend Golang (R5)
- **Crear**: convenciones y catálogo propuesto de 11 grupos de endpoints en `specs/nv-04/spec.md`; resumen en `docs/architecture.md`.
- **Verificar**: todos los grupos están marcados como propuestos/pendientes de OpenAPI; auth, errores, cursor e idempotencia documentados.
- **No hacer**: declarar endpoints definitivos, generar OpenAPI ni escribir clientes Retrofit.

## T5 — Auth, seguridad, UX y observabilidad (R6, R7)
- **Crear**: flujo de sesión/refresh, permisos, design system, estados, accesibilidad, formatos y logs en `specs/nv-04/spec.md`.
- **Verificar**: Keystore, refresh único, degradación sin permisos, `strings.xml`, 48 dp, `request_id` y logs redactados.
- **No hacer**: implementar FCM, Maps, cámara, pagos, chat, IA ni analítica.

## T6 — Calidad, documentación y referencias (R8)
- **Crear**: matriz de pruebas R8, gates de `nv-04` e inventario visual en `docs/figma-inventory.md`.
- **Actualizar**: `AGENTS.md`, `README.md`, `CLAUDE.md`, `Memoria.md`, `specs/nv-01/*`, `docs/*` y `.claude/*` para que `nv-04` sea arquitectura y `nv-05+` producto.
- **Verificar**: 8 secciones, R1-R8 trazados, 11 módulos, `git diff --check` limpio y sin `nv-04` asociado a implementación.
- **No hacer**: tests ejecutables, CI, scripts ni pantallas.
