# NV-30 — El día del plan

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 8 · El día del plan.
> **Contrato conciliado el 2026-11-22** con `wherever-backend/docs/endpoints.md` (`origin/main`): check-in (backend nv-62) y paquete sin conexión (nv-68) existen; rutas y navegación no tienen endpoint (D25). Sin código. Fase 5 de `docs/itinerario-planes.md`.

## 1. Objetivo

Acompañar a la persona durante el plan: ruta, llegada y check-in.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero llegar a cada parada sin salir de la app.

- [ ] Veo la ruta con tiempos y modo de transporte (traslados de `GET /planes/{id}`).
- [ ] Veo instrucciones paso a paso (depende de D25).
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
| GET | `/planes/{id}` | Paradas, horarios y traslados del plan |
| GET | `/planes/{id}/paquete-offline` | Plan, fichas de lugares y reservas para hoy o mañana; `ETag` y 304; 30 por hora |
| POST | `/checkins` | `{negocio_id, metodo (ubicacion o qr_negocio), lat?, lng?, precision_m?, codigo?, plan_id?}` → `{id, valido, distancia_m, dia, resena_id, ...}`; 10 por hora |
| GET | `/checkins` | Historial con cursor |

Sin endpoint: rutas paso a paso y direcciones (D25).

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

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Contrato conciliado el 2026-11-22 en borrador, pendiente de Osmar.
