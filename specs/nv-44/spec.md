# NV-44 — Presupuesto con barra y cantidad manual

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Amplía `specs/nv-10` (Gustos, sección "Gasto por salida"). Rama `feat/presupuesto-manual`.

## 1. Objetivo

En Gustos, el presupuesto solo se elegía con una barra doble de $0 a $1000 que avanzaba a saltos. Ahora hay dos formas sincronizadas: la barra, que se arrastra libremente y queda en pasos de $50 al soltarla, y dos campos para escribir el mínimo y el máximo. El ViewModel valida los dos campos.

## 2. Alcance / No-alcance

### Alcance
- **Barra (`RangeSlider`).** Se arrastra sin saltos (`steps = 0`) y, al soltarla, el extremo que se movió queda en el múltiplo de $50 más cercano. Arriba muestra el rango: "$150 – $300 MXN", y "$150 – $1000 o más" cuando el máximo es justo $1000. Alto mínimo de 48 dp.
- **Campos "Mínimo" y "Máximo"**, siempre visibles debajo de la barra: prefijo "$", teclado numérico y solo enteros.
  - Al escribir o pegar se quitan espacios, letras, comas de miles y signos, y se descarta lo que vaya después del primer punto decimal ("150.50" → "150"). También se quitan los ceros a la izquierda ("007" → "7") y se cortan a 6 dígitos.
  - Un "-" inicial se conserva para mostrar el error de rango, en vez de cambiar el signo sin avisar.
- **Sincronía.**
  - Mover la barra cambia los campos.
  - Escribir en los campos mueve la barra.
  - Una cantidad mayor de $1000 deja la barra al tope, pero se guarda la cantidad escrita. Si luego se mueve el otro extremo, la cantidad escrita no cambia.
  - Una cantidad escrita a mano no se redondea a $50.
- **Validación (`feature/tastes/domain/BudgetValidator.kt`)**, con el mensaje bajo el campo:

| Caso | Mensaje (`strings.xml`) |
|---|---|
| Campo vacío | "Escribe una cantidad" (`tastes_budget_empty`) |
| Negativo o mayor de $50,000 | "Escribe una cantidad entre $0 y $50,000" (`tastes_budget_out_of_range`) |
| Mínimo mayor que el máximo (bajo Mínimo) | "El mínimo no puede ser mayor que el máximo" (`tastes_budget_min_greater`) |

- Continuar/Guardar queda deshabilitado mientras haya un error.
- **422** en `presupuesto_min` o `presupuesto_max`: el `mensaje` de la API se muestra bajo ese campo, sin banner, y se borra al editar el campo.
- **Desde Ajustes** (`fromSettings`): los campos y la barra cargan el presupuesto guardado.
- **TalkBack.**
  - La barra se anuncia como "Rango de gasto por salida" y lee el rango actual.
  - Cada campo se anuncia como "Presupuesto mínimo en pesos" o "Presupuesto máximo en pesos".

### No-alcance
- El tope de $50,000 es solo de la app: la API solo pide enteros y `presupuesto_max ≥ presupuesto_min` (`docs/contexto_api_sprint1.md`).
- Botón "Escribir cantidad": los campos van siempre visibles, porque así se descubren mejor y hay un toque menos.

## 3. Contratos (API y navegación)

Sin cambios. `PUT /api/v1/usuarios/yo/gustos` sigue enviando `presupuesto_min` y `presupuesto_max` como enteros. Sin rutas nuevas.

## 4. Modelo de datos

- `TastesUiState.budgetMin/budgetMax` (Int) se reemplazan por `budget: BudgetForm` (textos de los campos y extremos arrastrados), más `budgetMinError` y `budgetMaxError` (422).
- `BudgetForm` (`feature/tastes/ui/BudgetForm.kt`) es Kotlin puro: sin Compose ni Android.
- Sin cambios en Room.

## 5. Seguridad y privacidad

Sin datos nuevos. El presupuesto sigue fuera de los logs (`specs/nv-10` §5).

## 6. Tests / Gates

- **Unit:**
  - `test/.../feature/tastes/domain/BudgetValidatorTest.kt`: limpieza de texto (espacios, letras, ceros, decimales, comas, signos y texto pegado) y los tres errores con sus bordes (0, 50,000 y 50,001).
  - `test/.../feature/tastes/ui/BudgetFormTest.kt`: la barra mueve los campos, los campos mueven la barra, el paso de $50 al soltar, una cantidad mayor de $1000 deja la barra al tope sin perder lo escrito, y lo escrito a mano no se redondea.
  - `TastesViewModelTest`: Continuar deshabilitado con error, carga desde Ajustes y el 422 en el campo.
- **Data:** `test/.../integration/TastesBudgetIntegrationTest.kt`, con `TastesViewModel`, el repositorio real y MockWebServer. El 200 envía los enteros escritos; el 422 en `presupuesto_max` muestra el mensaje en el campo.
- **UI:** `androidTest/.../feature/tastes/ui/BudgetSectionTest.kt` (Compose Testing). Escribir mueve la barra, mover la barra cambia el campo, las letras se descartan y el mínimo mayor que el máximo muestra el error.
- **Security:** sin superficie nueva.

## 7. Plan de entrega

PR "feat: presupuesto con barra y cantidad manual". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
