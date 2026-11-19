# NV-48 — Plan con IA e itinerario editable

## T1 — Implementación
- [x] Asistente con frase, palabras rápidas y hoja rápida (cuándo, personas, presupuesto, movilidad y ánimo con consentimiento).
- [x] "Armando tu plan" con pasos y Cancelar; resultado con lo entendido, aviso de reglas, costos y línea de tiempo; Otra idea; Guardar.
- [x] Plan guardado: subir/bajar, ±15 min, cambiar, quitar con deshacer, agregar, compartir, repetir, renombrar y borrar; 409 recarga.
- [x] Mis planes con pestañas; pestaña Planes en la barra; populares con Copiar en Inicio.
- [x] Versión 4 (0.3.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [x] Contra staging: generar responde 422 sin catálogo y la app muestra el mensaje.
- [ ] En el teléfono con catálogo cargado: generar, guardar y editar un plan real.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `PlansViewModelsTest` (17), `HomeViewModelTest` (+1)
- [x] Integración: `PlansIntegrationTest` (7)
- [x] UI: revisión en el teléfono
- [x] Seguridad: `ArchitectureTest`
- **No hacer**: mandar el ánimo sin consentimiento, reintentar una edición con 409 o guardar la ubicación.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
