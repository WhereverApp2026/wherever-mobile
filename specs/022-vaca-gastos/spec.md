# 022 — Vaca virtual y gastos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 6 · Compartir, grupos y chats. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Calcular quién debe a quién en un plan de grupo, sin mover dinero.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como integrante quiero saber cuánto debo.

- [ ] Calcula en partes iguales o personalizadas.
- [ ] Marcar pagado actualiza a todos.
- [ ] Wherever no mueve dinero, y lo dice en pantalla.

### UI

| Frame | Contenido |
|---|---|
| `28_Vaca` | Aviso, gasto total y por persona, Partes iguales o Personalizado, Quién pagó, Para quedar a mano con Marcar pagado, Agregar gasto. |
| `Vaca · *` | Variantes: Mariana pagó y todos pagaron. |
| `45_AgregarGasto` | En qué, monto, quién pagó, dividir entre, Guardar gasto. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Datos de Room. |
| Vacío | Sin gastos: Agregar gasto. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Gastos encolados; total marcado como pendiente. |
| Éxito | Saldos recalculados. |

### Validaciones

- Monto mayor que 0, en centavos enteros, nunca `Float`.
- Al menos una persona en Dividir entre.
- La suma personalizada debe igualar el monto.

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Grupo` → `Vaca` → `AgregarGasto`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Gastos por grupo y estado de pago.

## 4. Modelo de datos

`ExpenseEntity` y cálculo en dominio, probado sin Android.

## 5. Seguridad y privacidad

- Sin datos bancarios.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: el ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70).
- Unit: redondeo sin centavos perdidos.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
