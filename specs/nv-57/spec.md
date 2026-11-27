# NV-57 — Correcciones de la prueba en teléfono (Modo negocio y ficha)

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`.
> Sale de la prueba en el Samsung SM-A315G contra staging del 2026-11-26 con las cuentas Prueba QA y QA B. Rama `fix/negocio-detalles`. Toca pantallas de `nv-35` (48_NegPanel) y `nv-21` (33_Detalle).

## 1. Objetivo

Corregir lo que se vio mal en el teléfono, y dejar escrito qué no era un error de la app.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

- [x] 48_NegPanel, «Reservas de la semana» sin reservas: muestra «Aún no tienes reservas esta semana.» en lugar de siete rayas planas.
- [x] 48_NegPanel, «Calificación de la semana» sin reseñas: muestra «Sin reseñas esta semana» en lugar de «0.0 ★ (+0)». Las calificaciones van de 1 a 5, así que 0 es que no hubo reseñas.
- [x] 33_Detalle con letra o pantalla grande: «Cómo llegar» y «Llamar» ya no se cortan («Cómo lle…»). Se mide el texto con el estilo del botón (`ButtonText`) contra el ancho disponible; si no cabe, «Llamar» baja a su renglón. Un umbral fijo de escala (1.15) no bastaba: el Samsung de prueba tiene letra 1.1 y pantalla ampliada.
- [x] 48_NegPanel, «Camino al Sello de Excelencia»: los requisitos salían con la clave de la API (`dias_aprobado`, `resenas_verificadas_90d`, `promedio_90d`, `rechazos_moderacion_90d`); ahora con su texto («Días publicado», «Reseñas verificadas (90 días)»…). Una clave nueva se muestra sin guiones bajos.

### Lo que no era un error de la app

- **Recuadro gris sobre el encabezado del Panel:** el encabezado usa la portada del negocio con una capa oscura; la portada de «Cafe Prueba QA» es la imagen de prueba del alta (naranja con un marco de líneas blancas). Con una foto real desaparece.
- **Aviso cortado en 87_NegGaleria:** la pantalla tiene scroll y el botón «Guardar cambios» va fijo abajo; al desplazar se ve completo.
- **Nombre «Cafe Prueba QANegocio de prueba de la app»:** el panel muestra el `nombre` que manda la API, y el alta manda nombre y descripción en campos separados (`BusinessCreateDto`). Así se escribió en el campo de nombre.

### No-alcance

- **`requiere_refresh` tras crear el negocio** (anotado en `docs/pendientes.md`): forzar la renovación del token toca `core/network` (área de Jorge, constitución §4). El refresh usa un candado único porque la API rota el token y reusar uno viejo cierra todas las sesiones; duplicarlo en Negocio sería arriesgado. Va por el Issue #78 a Jorge.

### Estados

Sin estados nuevos: los dos casos del Panel son el estado vacío de cada bloque.

## 3. Contratos (API y navegación)

Sin cambios. `GET /negocios/{id}/panel` y `GET /negocios/{id}/estadisticas` (nv-35), `GET /lugares/{id}` (nv-21).

## 4. Modelo de datos

Sin cambios.

## 5. Seguridad y privacidad

Sin cambios. Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: sin lógica nueva en ViewModels (las tres correcciones son de presentación); `BusinessPanelViewModelTest` sigue verde.
- Data: sin cambios de contrato; `BusinessHomeRepositoryImplTest` sigue verde.
- Security: sin cambios; `SourceSecurityTest` y `ArchitectureTest` siguen verdes.
- UI: capturas de Roborazzi `48_neg_panel_semana_vacia` y `33_detalle_acciones` (la ficha no tenía vista previa; con «chica con letra grande», Llamar baja de renglón).
- Prueba en teléfono contra staging con Prueba QA (Panel) y cualquier cuenta (ficha de Restaurante Cafe Madero, que tiene teléfono).

## 7. Plan de entrega

Un PR: "fix(nv-57): correcciones de la prueba en teléfono (Panel de negocio y ficha)". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** Borrador con evidencia (Borre, 2026-11-26), pendiente de Osmar.
