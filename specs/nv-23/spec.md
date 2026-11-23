# NV-23 — Crear plan con IA

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 5 · Crear plan con IA.
> **Conciliada el 2026-11-22** con lo que implementó `specs/nv-48` y con `wherever-backend/docs/endpoints.md` (`origin/main`). Estado: **parcial**. Lo que falta va en la Fase 3 de `docs/itinerario-planes.md`.

## 1. Objetivo

Armar un plan del día a partir de una palabra.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero decir una palabra y recibir un plan.

- [x] Usa mis gustos si los tengo. Lo hace el servidor según `privacidad.aprendizaje_personalizado` (nv-48).
- [ ] Muestra el avance en 4 pasos. Hoy son 3 (`GENERATING_STEPS` en `CreatePlanScreen.kt`).
- [x] Puedo cancelar (`CreatePlanViewModel.cancel()`).
- [x] Al terminar abre el plan: resultado → Guardar plan → `PlanRoute(id)` (nv-48).

### Conciliación con lo implementado (2026-11-22)

| Frame | Estado | Detalle |
|---|---|---|
| `IAPalabra` | funciona | Frase de 1 a 300 caracteres y palabras rápidas (nv-48) |
| `Generando *` | funciona en parte | Indicador con 3 pasos y Cancelar; falta el 4.º paso "¡Plan listo!" con Ver mi plan |
| `17_AjustaPlan` | falta | La hoja rápida de nv-48 cubre cuándo (4 opciones), personas, presupuesto, movilidad y ánimo. Faltan día y hora exactos, número de paradas y "Crear plan a mano" → `CrearPlan` (nv-24) |

El conteo de `docs/avance.md` (4 de 6) se dedujo del código; se confirma frame por frame en la Fase 3.

### UI

| Frame | Contenido |
|---|---|
| `IAPalabra` | "¿Qué se te antoja hoy?", campo, chips de ejemplo, recordatorio de gustos, Prefiero ajustar los detalles, Armar mi plan. |
| `17_AjustaPlan` | Quién va, día, hora, personas, paradas (2 a 4), presupuesto, cómo me muevo, Crear plan a mano, Armar mi plan. |
| `Generando *` | Cuatro pasos de carga, de "Armando tu plan" a "¡Plan listo!", con Cancelar y Ver mi plan. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | `Generando 1…4`. |
| Vacío | 422 "no hay lugares abiertos y compatibles cerca": se muestra el mensaje de la API y se propone ajustar los detalles. |
| Error | Fallo: mensaje y volver al formulario con lo escrito. |
| Sin red | `89_SinConexion`: "No podemos armar planes sin internet". |
| Éxito | `Generando 4` → Ver mi plan. |

### Validaciones

- Frase de 1 a 300 caracteres (contrato).
- Personas de 1 a 20 (nv-48).
- Fecha y hora de ahora a 30 días (contrato, campo `fecha`); reloj inyectado.
- Paradas: la API no recibe cuántas; la IA propone de 2 a 4 (el motor de reglas, 3). En `17_AjustaPlan` el número solo puede viajar dentro de la frase, como el presupuesto y el ánimo en nv-48. Se decide en la Fase 3.

### Dudas abiertas

- **D29** resuelta con el contrato: la API no publica avance ni cancelación. Los 4 pasos son una animación y Cancelar descarta la respuesta en el teléfono.

### No-alcance

- Que el asistente pregunte lo que falta (US-036): la API no devuelve preguntas.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Inicio ("¿Qué se te antoja?"), pestaña Planes o hoja `15_Crear` → `CreatePlanRoute(phrase)` → resultado → `PlanRoute(id)`. Registradas en `docs/routes.md` (nv-48).
- `17_AjustaPlan` → `CrearPlan` (nv-24): ruta nueva al implementarla.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md`. Aquí solo se enlaza.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/planes/generar` | `{frase, lat, lng, fecha?, personas?, movilidad?}` → plan sin guardar con `fuente`, `aviso`, `parametros` y `plan` (2 a 4 paradas; respaldo por reglas si la IA no responde en 7 s). |
| POST | `/planes/generar/otra-idea` | Igual, con `excluir` (lugares ya propuestos). |
| POST | `/planes` | Guardar el plan propuesto (nv-48). |

La API no tiene avance ni cancelación del lado del servidor: los pasos de `Generando` avanzan en la app mientras espera la respuesta (los tres primeros con el tiempo, el cuarto con la respuesta) y Cancelar corta la llamada.

### Decisiones al implementar (2026-11-22)

- **D-23a Ajustes en la frase:** la API no recibe compañía, paradas ni presupuesto aparte (los entiende de la frase, `parametros`); 17_AjustaPlan los agrega al final de lo escrito, como ya hacía nv-48 con el presupuesto. Día y hora sí van en `fecha`; personas y movilidad en sus campos.
- **D-23b Sin Room:** el plan generado es un borrador que la API no guarda; se guarda con `POST /planes` (Guardar) y el plan guardado vive en la API (nv-48). No se agrega `PlanEntity`.
- **D-23c «Crear plan a mano»** (17_AjustaPlan) no se dibuja: `CrearPlan` es de nv-24.
- **D-23d Ver mi plan** abre el plan propuesto de nv-48 (Guardar, Otra idea, Ajustar la petición).
- **D-23e Ánimo:** 17_AjustaPlan muestra las caritas solo con el consentimiento de ánimo (US-048, nv-48); sin él no se dibujan.

## 4. Modelo de datos

Hoy no hay Room: el borrador vive en el `UiState` (`PlanDraft`, nv-48). Guardar el borrador en Room queda en `nv-56` (Fase 1 del itinerario).

## 5. Seguridad y privacidad

- El servidor decide si usa los gustos según `privacidad.aprendizaje_personalizado`, que la persona cambia en Privacidad; la app no lo manda en `generar`.
- El ánimo solo viaja con `privacidad.estado_animo` activo (nv-48, US-010).
- La ubicación solo viaja en `POST /planes/generar`; no se guarda.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit (hecho, nv-48): `test/.../feature/plans/ui/PlansViewModelsTest.kt`: una palabra manda ubicación, personas y movilidad; presupuesto y ánimo solo con consentimiento; horas en la zona del teléfono; sin ubicación no llama; otra idea; respaldo por reglas; error que vuelve a editar; guardar abre el plan.
- Unit (falta): Cancelar con dispatcher de prueba descarta la respuesta; los 4 pasos; `17_AjustaPlan` arma `fecha` y frase.
- Data (hecho, nv-48): `test/.../integration/PlansIntegrationTest.kt`: generar y otra idea con MockWebServer.
- Data (falta, nv-56): el borrador generado persiste en Room y sobrevive a un reinicio sin red.
- Security (falta): ni la frase ni los gustos aparecen en los logs del interceptor redactado.
- UI (falta): pasos de "Generando" con Compose Testing.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Lo hecho entró con nv-48. Lo que falta: rama `feat/nv-23-ia` (Fase 3 de `docs/itinerario-planes.md`), después de `nv-56`. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Conciliación del 2026-11-22 en borrador, con evidencia, pendiente de Osmar. Implementada el 2026-11-22 (Diego) en `feat/nv-23-ai-plan`, sobre la base de nv-48; sin probar contra staging.
