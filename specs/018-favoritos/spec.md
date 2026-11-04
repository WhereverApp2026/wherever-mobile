# 018 — Favoritos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 4 · Explorar y lugares. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Guardar lugares para armar planes más rápido.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero guardar lugares para encontrarlos después.

- [ ] El corazón guarda y quita al instante.
- [ ] Filtra por categoría.
- [ ] Funciona sin red y se sincroniza.

### UI

| Frame | Contenido |
|---|---|
| `35_Favoritos` | Contador, filtros Todos, Comida, Café, Aire libre; tarjetas con horario. |
| `36_FavVacio` | "Aún no tienes favoritos", Explorar lugares. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto. |
| Vacío | `36_FavVacio`. |
| Error | Si falla, el corazón regresa con aviso. |
| Sin red | Lectura de Room; los cambios se encolan con WorkManager. |
| Éxito | Lista actualizada. |

### Validaciones

- Operación idempotente.

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Perfil` → `Favoritos` → `Detalle`.
- `FavVacio` → `Explorar`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Listar, agregar y quitar favoritos.

## 4. Modelo de datos

`FavoriteEntity` con estado de sincronización.

## 5. Seguridad y privacidad

- Dato personal: se borra al cerrar sesión.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Data: encolado y sincronización con Room real.
- UI: estado vacío.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
