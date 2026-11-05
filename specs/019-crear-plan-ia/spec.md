# 019 — Crear plan con IA

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 5 · Crear plan con IA. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Armar un plan del día a partir de una palabra.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero decir una palabra y recibir un plan.

- [ ] Usa mis gustos si los tengo.
- [ ] Muestra el avance en 4 pasos.
- [ ] Puedo cancelar.
- [ ] Al terminar abre el plan.

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
| Vacío | Sin resultados: propone ajustar los detalles. |
| Error | Fallo: mensaje y volver al formulario con lo escrito. |
| Sin red | `89_SinConexion`: "No podemos armar planes sin internet". |
| Éxito | `Generando 4` → Ver mi plan. |

### Validaciones

- Texto no vacío.
- Paradas de 2 a 4.
- Personas ≥ 1.
- Fecha y hora en el futuro; reloj inyectado.

### Dudas abiertas

- **D29** Generación del plan con IA (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Hoja `Crear` o `Inicio` → `IAPalabra` → `AjustaPlan` o `Generando` → `Plan` (`specs/020`).
- `AjustaPlan` → `CrearPlan`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Generar plan (entrada: palabra y ajustes; salida: paradas, horarios, costos, traslados).
- Avance y cancelación.

## 4. Modelo de datos

`PlanEntity` borrador tras recibirlo.

## 5. Seguridad y privacidad

- `privacidad.aprendizaje_personalizado` limita el uso de gustos en el servidor.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: transiciones y cancelación con dispatcher de prueba.
- UI: pasos.
- Data: el borrador del plan generado persiste en Room y sobrevive a un reinicio sin red.
- Security: los gustos usados por la IA no aparecen en logs y viaja `privacidad.aprendizaje_personalizado`.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
