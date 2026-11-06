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

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Rutas y tiempos (o proveedor externo).
- Aviso de check-in hacia el cliente.

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

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
