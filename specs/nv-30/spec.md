# NV-30 — El día del plan

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 8 · El día del plan. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Acompañar a la persona durante el plan: ruta, llegada y check-in.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero llegar a cada parada sin salir de la app.

- [ ] Veo la ruta con tiempos y modo de transporte.
- [ ] Veo instrucciones paso a paso.
- [ ] Sin permiso de ubicación se ofrece la ruta sin posición.

**HU propuesta.** Como usuario quiero que mi visita quede verificada.

- [ ] El negocio escanea mi QR.
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
| Error | Sin ruta: propone abrir la app de mapas. |
| Sin red | Paradas y direcciones del plan guardado, sin navegación. |
| Éxito | `26_CheckinOk`. |

### Validaciones

- La ubicación se usa solo con la app en primer plano, salvo decisión explícita.

### Dudas abiertas

- **D18** Permisos (`specs/README.md`)
- **D25** Mapas y navegación (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Plan` o `DetReserva` → `Mapa`, `MapaFull`, `Llegar` → `Navegando`.
- `CheckinOk` → `Camara`, `Llegar`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Uso |
|---|---|---|
| POST | `/checkins` | «Ya llegué»: `metodo: ubicacion` con `lat`/`lng`, o `qr_negocio` con `codigo` del mostrador; fuera de radio = 422 neutro; segundo del día = 409. |
| GET | `/lugares/{id}` | Coordenadas del lugar para «Cómo llegar». |

### Pendiente de backend o de decisión

- **D25 (proveedor de mapa y rutas):** 13_Mapa, 14_MapaFull, 15_Llegar y 18_Navegando no se dibujan; la API no da rutas.

### Decisiones al implementar (2026-11-22, Diego)

- **D-30a «Cómo llegar»** en cada parada de 39_Plan abre la app de mapas del teléfono (`geo:` con las coordenadas de la ficha): la alternativa sin proveedor que propone D25.
- **D-30b «Ya llegué»** en cada parada: pide la ubicación aproximada solo en ese momento (permiso en contexto) o escanea el código del mostrador con el lector ZXing ya en el stack (nv-20). 26_CheckinOk ofrece «Tomar una postal» (71_Camara con el lugar y el plan) e «Ir a la siguiente parada».
- **D-30c Sin precisión:** `LocationProvider` (`core/location`) no da precisión y la API pide `precision_m` ≤ 100 m; si la API rechaza la ubicación, queda el código del mostrador. Pedirlo a Jorge (dueño de `core`).
- **D-30d Sin Room:** el check-in vive en la API (`GET /checkins`); la ubicación no se guarda en el teléfono ni en logs.
- **D-30e** «Cupón aplicado» de 26_CheckinOk no se dibuja: la respuesta del check-in no trae cupones.

## 4. Modelo de datos

Ruta en memoria; paradas desde `PlanStopEntity`.

## 5. Seguridad y privacidad

- Sin histórico de ubicación en disco ni logs.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: pasos de la ruta.
- UI: estados sin permiso.
- Data: el estado del check-in queda en Room y se lee sin red.
- Security: no queda histórico de ubicación en disco ni en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Parcial el 2026-11-22 (Diego) en `feat/nv-30-checkin`: 26_CheckinOk, check-in y «Cómo llegar»; 13, 14, 15 y 18 esperan D25.
