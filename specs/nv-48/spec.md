# NV-48 — Plan con IA e itinerario editable

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Cubre `specs/nv-23` (crear plan con IA), `nv-24` (detalle y edición) y `nv-28` (mis planes). Rama `feat/plan-ia`, sobre `feat/inicio-recomendaciones` (nv-47).

## 1. Objetivo

La propuesta de valor de Wherever es "una palabra, un plan". La API ya arma itinerarios con IA y los deja editar. Ahora la persona escribe o toca una palabra, ajusta con quién va, cuándo, cuánto gasta, cómo se mueve y cómo se siente, y recibe un plan de 2 a 4 paradas con traslados y costos. Lo puede guardar, editar, compartir y repetir.

Historias: US-035, US-039, US-040, US-041, US-042, US-043, US-044, US-045, US-046, US-048, US-049, US-050 (asistente); US-055, US-056, US-057, US-058, US-061, US-063, US-064, US-065, US-066 (itinerario); US-033 (copiar plan popular); US-010 (consentimiento de ánimo, junto con nv-43).

## 2. Alcance / No-alcance

### Alcance
- **Armar un plan** (15_Asistente y 15b_HojaRapida), desde la caja "¿Qué se te antoja?" de Inicio o desde Planes:
  - Frase libre de hasta 300 caracteres, o una palabra rápida (Antojo, Relax, Cita, Con amigos, En familia, Barato).
  - Hoja rápida: cuándo (ahora, en 2 horas, hoy en la noche, mañana), personas (1 a 20), presupuesto por persona ($200, $400, $700 o $1000), movilidad (a pie, en bici, en coche, en transporte) y ánimo con caritas.
  - El ánimo solo aparece si la persona activó "Estado de ánimo" en Privacidad (US-010); si no, se explica dónde activarlo.
  - La API no recibe presupuesto ni ánimo aparte: los entiende de la frase. La app los agrega al final ("…, con presupuesto de hasta $400 por persona, me siento relajado").
  - Sin ubicación no se genera: se avisa que hace falta activarla desde Inicio.
- **"Armando tu plan"** (US-040): indicador, tres pasos que cambian cada 1.4 s (anunciados por TalkBack) y "Cancelar". Con animaciones apagadas, el texto no parpadea.
- **Resultado sin guardar**:
  - Lo que entendió la IA (compañía, personas, presupuesto, hora, con niños o mascota).
  - Si respondió el motor de reglas (`fuente: reglas`), un aviso (US-039).
  - Costo total y por persona, y la línea de tiempo con hora, minutos, precio y "Por qué" de cada parada, y el traslado entre paradas según la movilidad (US-055).
  - "Otra idea" pide otro plan sin los lugares ya propuestos (US-044); "Ajustar la petición" vuelve a la hoja rápida; "Guardar plan" lo guarda y abre su detalle.
- **Plan guardado** (16_Plan):
  - Fecha, personas, costo total y por persona, siempre recalculados por la API (US-066).
  - Por parada: subir y bajar (US-065), −15/+15 min (US-064), cambiar por una alternativa cercana del mismo giro (US-056), y quitar con "Deshacer" (US-063). La última parada no se quita.
  - Agregar parada buscando un lugar, 15 min después de la última (US-057).
  - Compartir el enlace de 7 días (US-061), "Repetir mañana" (US-058), renombrar y borrar con confirmación.
  - Con 409 (otro dispositivo cambió el plan) se recarga y se avisa.
- **Mis planes** (24_MisPlanes): pestaña "Planes" en la barra, con Próximos, Guardados y Pasados, y botón para armar uno.
- **Inicio**: "Planes populares de la semana" con "Copiar" (US-033), que lo guarda para mañana y lo abre.
- Versión `versionCode 4`, `versionName 0.3.0`.

### No-alcance
- Que el asistente pregunte lo que falta (US-036): la API no devuelve preguntas.
- Arrastrar paradas: se reordenan con subir y bajar, que también funciona con TalkBack.
- Ruta en mapa (US-059), aviso de cierre (US-060) y paquete sin conexión (US-178): llegan con mapa y modo sin conexión.
- Abrir un enlace compartido en la app: requiere App Links (US-210).

## 3. Contratos (API y navegación)

wherever-backend `docs/endpoints.md` (nv-30 a nv-33, nv-38, nv-68): `POST /planes/generar`, `POST /planes/generar/otra-idea`, `GET /planes/populares`, `POST`/`GET /planes`, `GET`/`PATCH`/`DELETE /planes/{id}`, `POST /planes/{id}/clonar`, `POST /planes/{id}/paradas`, `PUT /planes/{id}/paradas/orden`, `PATCH`/`DELETE /planes/{id}/paradas/{parada}`, `GET .../alternativas`, `POST .../cambiar` y `POST /planes/{id}/compartir`. Toda edición manda `If-Match` con la versión. Los planes guardados traen solo `negocio_id` en cada parada: el nombre sale de `GET /lugares/{id}`, con caché en memoria.

Rutas nuevas en `docs/routes.md`: `CreatePlanRoute(phrase)`, `PlanRoute(id)` y `MyPlansRoute`.

Contra staging (2026-11-18, 18:00): `POST /planes/generar` con el centro de CDMX → **422** "no hay lugares abiertos y compatibles cerca; prueba otra hora o zona" (`traza_id` `04b635b02380380060ea106c2430e46a`). Es lo esperado sin negocios cargados; la app muestra ese mensaje. Guardar, editar, compartir y populares quedan probados con MockWebServer hasta que staging tenga catálogo.

## 4. Modelo de datos

Sin cambios en Room. Modelos en `feature/plans/domain/Plans.kt` (`PlanDraft`, `Plan`, `PlanStop`, `Transfer`, `PlanParameters`, `PlanSummary`, `PopularPlan`, `Alternative`, `ShareLink`, `PlanRequest`, `Mobility`, `PlansTab`) y dos repositorios: `PlanAssistantRepository` y `PlansRepository` (singleton, por el caché de nombres). `core/ui/Dates.kt` suma `formatTime` y `shiftIso`.

## 5. Seguridad y privacidad

- La ubicación solo viaja en `POST /planes/generar`; no se guarda.
- El ánimo es dato sensible: solo se manda con el consentimiento `privacidad.estado_animo` (US-010).
- El enlace para compartir lo arma la API con el código en el fragmento; la app solo lo pasa a la hoja de compartir.

## 6. Tests / Gates

- **Unit:** `test/.../feature/plans/ui/PlansViewModelsTest.kt` (17): una palabra manda ubicación, personas y movilidad; presupuesto y ánimo en la frase solo con consentimiento; horas de "hoy en la noche" y "mañana" en la zona del teléfono; sin ubicación no llama a la IA; "Otra idea" excluye lo propuesto; respaldo por reglas; error que vuelve a editar; guardar abre el plan; quitar y deshacer con su hora; la última parada no se quita; subir y bajar; +15 min; cambiar por alternativa; agregar después de la última; 409 recarga; compartir y repetir; renombrar recortado; pestañas de Mis planes.
- `test/.../feature/home/ui/HomeViewModelTest.kt` (+1): planes populares y copiar para mañana.
- **Data:** `test/.../integration/PlansIntegrationTest.kt` (7) con MockWebServer: generar (frase, fuente, parámetros y paradas con nombre), otra idea con `excluir`, guardar sin pedir fichas, nombres de paradas con una sola ficha por lugar, `If-Match` en reordenar/mover/quitar, 409 → `Conflict`, lista por pestaña, compartir y populares.
- **UI:** sin pruebas instrumentadas nuevas; la línea de tiempo se revisó en el teléfono.
- **Security:** `ArchitectureTest` con `feature/plans`; sin permisos nuevos.

## 7. Plan de entrega

PR "feat: plan con IA e itinerario editable (nv-48)". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
