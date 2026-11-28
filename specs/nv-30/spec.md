# NV-30 — El día del plan

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 8 · El día del plan.
> **Contrato conciliado el 2026-11-22** con `wherever-backend/docs/endpoints.md` (`origin/main`): check-in (backend nv-62) y paquete sin conexión (nv-68) existen; rutas y navegación no tienen endpoint (D25). Sin código. Fase 5 de `docs/itinerario-planes.md`.

## 1. Objetivo

Acompañar a la persona durante el plan: ruta, llegada y check-in.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero llegar a cada parada sin salir de la app.

- [x] Veo la ruta con tiempos y modo de transporte (traslados de `GET /planes/{id}`; 13_Mapa y 14_MapaFull, D-30f).
- [ ] Veo instrucciones paso a paso (espera un proveedor de rutas, D-30f; mientras, la app de mapas del teléfono, D-30g).
- [ ] Sin permiso de ubicación se ofrece la ruta sin posición.

**HU propuesta.** Como usuario quiero que mi visita quede verificada.

- [ ] Hago check-in escaneando el QR del mostrador del negocio o con mi ubicación (el contrato lo inicia la persona, no el negocio).
- [ ] La app confirma el check-in y ofrece la postal.

### UI

| Frame | Contenido |
|---|---|
| `13_Mapa` | Mapa con ruta numerada y hoja Ruta del plan, Cómo llegar a la primera parada. |
| `14_MapaFull` | Mapa con horarios y traslados; Todo tu recorrido; Ver pasos; Iniciar ruta. |
| `15_Llegar` | A pie, Coche, Bus; hora de llegada; instrucciones; Iniciar ruta. |
| `18_Navegando` | Instrucción grande, Luego, Recentrar, tiempo y distancia restantes, salir, siguiente. |
| `26_CheckinOk` | "¡Ya estás en…!", beneficios, Tomar una postal, Ir a la siguiente parada. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador sobre el mapa. |
| Vacío | No aplica. |
| Error | Sin ruta: propone abrir la app de mapas. Check-in no válido: 422 con el mensaje neutro de la API ("Parece que aún no llegas. ¿Escaneas el código?"). Segundo check-in válido del día: 409. |
| Sin red | Paradas, direcciones y reservas del plan guardado (`GET /planes/{id}/paquete-offline`, en Room por `nv-56`), sin navegación. |
| Éxito | `26_CheckinOk`. |

### Validaciones

- La ubicación se usa solo con la app en primer plano, salvo decisión explícita.
- Check-in por ubicación: la API lo acepta a 100 m o menos con precisión de 100 m o menos.
- El paquete sin conexión solo existe para planes de hoy o mañana (si no, 422 `fuera_de_ventana`).

### Dudas abiertas

- **D18** Permisos (`specs/README.md`).
- **D25** Mapas y navegación (`specs/README.md`): el contrato difiere `GET /geo/direcciones` "hasta elegir proveedor de mapas". Recomendación del itinerario: abrir la app de mapas del teléfono para `15_Llegar` y `18_Navegando` mientras no haya proveedor.
- Aviso "Es hora de salir": falta su tipo de push en el backend (`docs/pendientes.md`).

### No-alcance

- Navegación paso a paso propia mientras D25 siga abierta.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `PlanRoute` o `DetReserva` (nv-27) → `Mapa`, `MapaFull`, `Llegar` → `Navegando`.
- `CheckinOk` → cámara de postal (nv-31), `Llegar` a la siguiente parada.
- Rutas nuevas en `docs/routes.md` al implementar.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md`.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/checkins` | «Ya llegué»: `metodo: ubicacion` con `lat`/`lng`, o `qr_negocio` con `codigo` del mostrador; fuera de radio = 422 neutro; segundo del día = 409. |
| GET | `/lugares/{id}` | Coordenadas del lugar para «Cómo llegar». |

### Pendiente de backend o de decisión

- **D25 resuelta para el mapa** (Jorge, 2026-11-23: osmdroid + OpenStreetMap, excepción E-04). osmdroid dibuja el mapa pero **no calcula rutas**, y la API no las da: 15_Llegar y 18_Navegando siguen esperando un proveedor de rutas (ver D-30f).
- **Sin consumir aún (conciliación de Borre):** `GET /planes/{id}/paquete-offline` (plan, fichas y reservas de hoy o mañana; `ETag` y 304; 30 por hora) espera Room de `nv-56`; `GET /checkins` (historial con cursor) no tiene pantalla.

### Decisiones al implementar (2026-11-22, Diego)

- **D-30a «Cómo llegar»** en cada parada de 39_Plan abre la app de mapas del teléfono (`geo:` con las coordenadas de la ficha): la alternativa sin proveedor que propone D25.
- **D-30b «Ya llegué»** en cada parada: pide la ubicación aproximada solo en ese momento (permiso en contexto) o escanea el código del mostrador con el lector ZXing ya en el stack (nv-20). 26_CheckinOk ofrece «Tomar una postal» (71_Camara con el lugar y el plan) e «Ir a la siguiente parada».
- **D-30c Sin precisión:** `LocationProvider` (`core/location`) no da precisión y la API pide `precision_m` ≤ 100 m; si la API rechaza la ubicación, queda el código del mostrador. Pedirlo a Jorge (dueño de `core`).
- **D-30d Sin Room:** el check-in vive en la API (`GET /checkins`); la ubicación no se guarda en el teléfono ni en logs.
- **D-30e** «Cupón aplicado» de 26_CheckinOk no se dibuja: la respuesta del check-in no trae cupones.

### Decisiones de los mapas (2026-11-25, Borre, rama `feat/nv-30-mapas`)

- **D-30f Mapa sí, rutas no.** `13_Mapa` y `14_MapaFull` se dibujan con osmdroid: un pin numerado por parada (coordenadas de `GET /lugares/{id}`) unidos en orden por una línea recta, y en la hoja los traslados reales de `GET /planes/{id}` (minutos y km según la movilidad del plan). La línea no sigue calles: no hay proveedor de rutas. `15_Llegar` y `18_Navegando` piden instrucciones calle por calle y tiempos por modo que nadie da; no se construyen con datos inventados (constitución §2.7). Quedan para cuando el equipo elija un proveedor de rutas (p. ej. OSRM o GraphHopper, con su excepción de stack).
- **D-30g Iniciar ruta y Cómo llegar** abren la app de mapas del teléfono, como D-30a: «Cómo llegar a {primera parada}» con `geo:` y «Iniciar ruta» con todas las paradas en orden (`https://www.google.com/maps/dir/?api=1` con `waypoints` y el `travelmode` de la movilidad del plan). Sin app de mapas, aviso.
- **D-30h Sin "Tú" ni "Desde tu ubicación".** La maqueta dibuja la posición de la persona y el traslado desde ella. La API solo da traslados entre paradas y el mapa del plan no pide permiso de ubicación; se muestran solo los traslados reales entre paradas. "Ver pasos" de 14_MapaFull no se dibuja (no hay pasos).
- **D-30i Totales.** "N min de traslados · X km" suma los traslados de la API; "h con paradas" va de la llegada a la primera parada al fin de la última (`llega` + `minutos`).
- **D-30j Entrada.** 39_Plan gana «Ver mapa» bajo el resumen → `PlanMapRoute(id)` (13_Mapa); desde ahí «Ver todo el recorrido» → `PlanMapRoute(id, full = true)` (14_MapaFull).

## 4. Modelo de datos

Ruta en memoria; paradas desde `PlanStopEntity` (`nv-56`). El resultado del check-in se guarda en Room para leerlo sin red.

## 5. Seguridad y privacidad

- Sin histórico de ubicación en disco ni logs. La API tampoco guarda la ubicación del check-in.
- El código QR del mostrador se escanea con ZXing (ya en el stack por nv-20); la cámara se pide antes, en contexto (nv-11).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: pasos de la ruta y orden de paradas; check-in 201, 409 y 422.
- UI: estados sin permiso de ubicación y de cámara.
- Data: el estado del check-in queda en Room y se lee sin red; el paquete sin conexión respeta el `ETag`.
- Security: no queda histórico de ubicación en disco ni en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: rama `feat/nv-30-dia` (Fase 5 de `docs/itinerario-planes.md`). `26_CheckinOk` puede ir primero porque no depende del mapa. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Contrato conciliado el 2026-11-22 en borrador, pendiente de Osmar. Parcial el 2026-11-22 (Diego) en `feat/nv-30-checkin`: 26_CheckinOk, check-in y «Cómo llegar»; 13, 14, 15 y 18 esperan D25. 13_Mapa y 14_MapaFull el 2026-11-26 (Borre, `feat/nv-30-mapas`, D-30f a D-30j); 15 y 18 esperan un proveedor de rutas.
