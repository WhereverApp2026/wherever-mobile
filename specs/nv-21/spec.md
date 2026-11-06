# NV-21 — Explorar y lugares

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 4 · Explorar y lugares. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Descubrir lugares con filtros y mapa, y ver su información para decidir.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero filtrar lugares para encontrar lo que busco.

- [ ] Aplica distancia, tipo, presupuesto, abierto, Sello y cupón.
- [ ] El botón indica cuántos resultados hay.
- [ ] Limpiar quita los filtros.

**HU propuesta.** Como usuario quiero ver el detalle de un lugar.

- [ ] Muestra calificaciones y afluencia por hora.
- [ ] Reservar solo si el lugar lo permite.
- [ ] Puedo agregarlo a un plan o a favoritos.

### UI

| Frame | Contenido |
|---|---|
| `30_Explorar` | Buscador, filtros rápidos, mapa pequeño con Ver en mapa, tarjetas con calificación, afluencia, Sello y amigos. |
| `31_Filtros` | Hoja: distancia, tipo, presupuesto, Abiertos ahora, Con Sello, Con cupón, Limpiar, Ver 24 lugares. |
| `32_ExpMapa` | Mapa con pines, buscar en esta zona, tarjeta de lugar, Ver detalle, A mi plan. |
| `33_Detalle` | Foto, favorito, nombre, calificación, Sello, Sabor/Atención/Ambiente, Qué tan lleno, amigos que han ido, dirección, horario, promoción, A mi plan, Reservar mesa. |
| `Detalle · *` | Variantes: Fonda Doña Meche (reserva), Jardín del Cerro y Mercado de Tula (Cómo llegar). |
| `34_Sello` | Qué es el Sello, requisitos, negocios con Sello cerca, Ver negocios con Sello. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueletos de tarjeta; mapa con su propio indicador. |
| Vacío | `21_SinResultados` o "Quita filtros". |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. El mapa se oculta. |
| Éxito | Lista con cursor; en el mapa, pines de la zona. |

### Validaciones

- Sin permiso de ubicación se usa `ciudad` del perfil; Cerca de mí lo pide en contexto.

### Dudas abiertas

- **D18** Permisos (`specs/README.md`)
- **D25** Mapas y navegación (`specs/README.md`)
- **D32** Lugares con y sin reserva (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → `Explorar` → `Filtros`, `ExpMapa`, `Buscar`, `Detalle`.
- `Detalle` → `Reserva`, `Sello`, `ResLugar`, `Llegar`, plan (A mi plan), `DetCupon`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Búsqueda y filtros de lugares.
- Detalle con calificaciones por rubro, afluencia por hora y promociones.
- Amigos que han ido.
- Campo que indique si acepta reservas.
- Negocios con Sello.

## 4. Modelo de datos

`PlaceEntity` con caché y expiración.

## 5. Seguridad y privacidad

- Ubicación solo mientras se usa la app; no se guarda histórico.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: combinación de filtros.
- Data: caché y actualización.
- UI: conteo en el botón.
- Security: la ubicación se usa solo en primer plano; no se escribe histórico de coordenadas en disco ni en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
