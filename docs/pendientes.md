# Pendientes — pausa del 2026-11-19 (tarde)

> Trabajo directo en `main` (pedido de Jorge). Métrica: pantallas que funcionan / 162 en `docs/avance.md` (hoy **54 reales + 1 local = 33,3 %**).

## Dónde me quedé

- **En curso: Búsqueda (nv-19), rama `wip/pausa`** (no está en `main`).
  - Hecho: `feature/search` (`SearchViewModel`, `SearchScreen`, `SearchActions`), ruta `SearchRoute(query)`, la barra de Inicio abre Buscar (con la fila "Armar un plan con la IA: «…»" para no perder lo de nv-48), pestañas Lugares/Personas/Planes, recientes con Borrar, ideas, Cerca de ti, 21_SinResultados con "Pedirle a la IA" y Explorar, Agregar → Pendiente en Personas, validación local de personas (`@usuario` 3–20 `a-z0-9_` o teléfono; si no, ayuda y no llama: la API da 422).
  - Verde en local: `ktlintCheck`, `detekt`, `testDebugUnitTest` (incluye `SearchViewModelTest`, 9 pruebas), `installDebug`.
  - Probado en el teléfono: Inicio → Buscar, ideas, `GET /lugares/buscar` → sin resultados (staging sin negocios). Captura `docs/pruebas/nv19_sin_resultados.png`.
  - **Falta**: (1) probar la pestaña Personas en el teléfono con la cuenta QA B; antes ponerle usuario: `PATCH /usuarios/yo {nombre_usuario: "qa_wv_b"}` exige cabecera `If-Match` (dio 400 sin ella) → leer `version`/ETag de `GET /usuarios/yo` y mandarla; (2) `lintDebug`, `koverVerifyDebug`, `assembleRelease`, scripts de puerta; (3) marcar `specs/nv-19/task.md` y `spec.md`, `docs/avance.md` (nv-19 → 4/4) y `Memoria.md`; (4) merge a `main`, push y revisar CI.

## Siguiente paso al volver

1. `git checkout wip/pausa` (o traerla a `main` con `git merge --ff-only` si `main` no cambió) y terminar los puntos de "Falta" de arriba.
2. Seguir el orden pedido: Permisos en contexto (nv-11: pantalla 08_Permisos "Antes de empezar" tras Gustos; hoy el permiso de avisos se pide directo en Inicio) → Explorar y ficha → Plan con IA → Detalle del plan → Amigos (incluido el QR: requiere acordar librería, constitución §3) → Reserva y pago → Inicio y feed → lo que está en 0 %.

## Entorno (para retomar rápido)

- JDK 17: `…/ceb6bb66-…/scratchpad/jdk17dl/jdk-17.0.20.1+1` (temporal; ver `docs/revision-jorge.md`).
- Teléfono Xiaomi: dar `adb shell appops set com.wherever.mobile 10021 allow` tras cada reinstalación para las pruebas de UI; no bloquear la pantalla (tiene PIN).
- Cuentas QA en staging (`@example.com`, A en el teléfono y B por script); credenciales solo en el scratchpad de la sesión, nunca en el repo. Si se pierden, crear otras.
- Staging: sin negocios (Explorar/ficha/reservar solo muestran vacío) y sin SMTP (no llegan correos).

## CI de `main`

Último commit `7791a86` (nv-18): **verde** (corrida 44591903865).
