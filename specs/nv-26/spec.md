# NV-26 — Vaca virtual y gastos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 6 · Compartir, grupos y chats.
> **Contrato conciliado el 2026-11-22** con `wherever-backend/docs/endpoints.md` (`origin/main`, backend nv-58, hoja J4): ya no es "Pendiente de backend". Sin código. Fase 4 de `docs/itinerario-planes.md`; requiere `46_Grupo` (nv-25) en `main`.

## 1. Objetivo

Calcular quién debe a quién en un plan de grupo, sin mover dinero.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como integrante quiero saber cuánto debo.

- [ ] Calcula en partes iguales o personalizadas (`division: igual | personalizada`; el corte lo calcula la API).
- [ ] Marcar pagado actualiza a todos (`PATCH /vaca/deudas/{id}` recalcula el corte; los demás lo ven al recargar).
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
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. 409 (versión vieja o `deudas_pagadas`): recarga y, si aplica, pide confirmar. |
| Sin red | Gastos encolados con su `id_cliente`; total marcado como pendiente. Reenviar el mismo `id_cliente` no duplica (la API responde 200 con el existente). |
| Éxito | Saldos recalculados por la API. |

### Validaciones

- Monto de 1 a 10 000 000 centavos, en enteros, nunca `Float`.
- Concepto: comida, bebidas, transporte, entrada u otro; descripción hasta 60 caracteres.
- Dividir entre 2 y 12 integrantes confirmados (por omisión, todos los confirmados).
- La suma personalizada debe igualar el monto (si no, 422).
- Referencia de pago hasta 60 caracteres.

### No-alcance

- Mover dinero o pedir datos bancarios.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `46_Grupo` (nv-25) → `Vaca` → `AgregarGasto`. Rutas nuevas en `docs/routes.md` al implementar.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md` (nv-58). Solo integrantes confirmados del grupo.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/grupos/{id}/vaca` | Total, `por_persona` y `deudas` (28_Vaca). |
| GET | `/grupos/{id}/gastos` | «Quién pagó». |
| POST | `/grupos/{id}/gastos` | 45_AgregarGasto: `monto_centavos`, `concepto`, `descripcion`, `pagado_por`, `dividir_entre`, `division` (`igual` o `personalizada`), `montos_personalizados`, `plan_id` e `id_cliente`. |
| PATCH | `/vaca/deudas/{id}` | «Marcar pagado» (`estado: pagada`); ya pagada = 409. |

### Decisiones al implementar (2026-11-22)

- **D-26a Entrada:** botón «La vaca» junto a «Ver plan» en el plan fijado de 45_GrupoChat (nv-25).
- **D-26b Sin Room ni cola sin red:** la API calcula el corte y es la fuente de verdad. Sin red se muestra el error con Reintentar; reintentar un gasto manda el mismo `id_cliente` y la API no lo duplica.
- **D-26c Partes iguales / Personalizado** en 28_Vaca elige cómo se divide el siguiente gasto (la API divide por gasto, no por vaca).
- **D-26d Concepto:** la API pide `concepto`; 45_AgregarGasto agrega chips (Comida, Bebidas, Transporte, Entrada, Otro).
- **D-26e Dividir entre 2 a 12 personas** (la API no acepta 1).

## 4. Modelo de datos

`ExpenseEntity` (con `id_cliente` y estado de envío) y el corte del grupo en Room; montos en centavos (`Long`). El cálculo lo hace la API; la app solo valida y muestra.

## 5. Seguridad y privacidad

- Sin datos bancarios ni de tarjeta; la referencia es texto libre corto.
- La respuesta lleva `Cache-Control: no-store`: Room guarda solo lo necesario para mostrar sin red.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: el ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70) leído del corte de la API.
- Unit: validaciones en centavos y suma personalizada igual al monto.
- Data: gastos y saldos se guardan en Room y se leen tras reiniciar sin red; un gasto encolado se reenvía con el mismo `id_cliente` (MockWebServer).
- Security: no hay datos bancarios ni de tarjeta en los gastos y su cuerpo no se registra en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: rama `feat/nv-26-vaca` (Fase 4 de `docs/itinerario-planes.md`), cuando `46_Grupo` esté en `main`. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Contrato conciliado el 2026-11-22 en borrador, pendiente de Osmar. Implementada el 2026-11-22 (Diego) en `feat/nv-26-vaca`; sin probar contra staging.
