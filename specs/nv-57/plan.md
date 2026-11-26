# plan.md — NV-57 — Correcciones de la prueba en teléfono

> Deriva de `specs/nv-57/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/business/ui/BusinessPanelScreen.kt`: `WeekCard` (semana sin reservas) y la fila de calificación del resumen (0 = sin reseñas).
- `feature/places/ui/PlaceDetailScreen.kt`: `Actions` (Llamar en su renglón con letra grande).
- `strings.xml`: `business_week_empty`, `business_week_no_reviews`.

## 2. Guardarraíles
- Sin lógica de negocio en Composables: solo se decide qué mostrar con lo que ya trae el estado.
- Textos en `strings.xml`; sin `dp` ni colores fuera de `core/designsystem`.

## 3. Pruebas
- Unit: siguen verdes las del Panel y la ficha.
- UI: vistas previas y capturas de Roborazzi de los casos vacíos y de letra grande.
- Teléfono contra staging.

## 4. Secuencia
1. Spec.
2. Panel y ficha.
3. Vistas previas y capturas.
4. Compuertas, teléfono y PR.

## 5. Verificación
- `./gradlew assembleDebug`, `ktlintCheck`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug` (detekt: CI).
