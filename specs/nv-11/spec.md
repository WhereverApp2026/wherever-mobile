# NV-11 — Permisos en contexto

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Explicar para qué se usa cada permiso y dejar que la persona decida, sin bloquear la app.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero saber por qué se piden permisos para decidir con confianza.

- [ ] Pide cada permiso solo al tocar Permitir y continuar.
- [ ] Con el permiso negado la app sigue y ofrece elegir ciudad a mano.
- [ ] Ahora no entra a la app sin pedir nada.
- [ ] Refleja la decisión en `privacidad.ubicacion`.

### UI

| Frame | Contenido |
|---|---|
| `08_Permisos` | Paso 4 de 4, "Antes de empezar". Tarjetas Ubicación (Necesario), Notificaciones (Opcional), Cámara (Opcional), nota de privacidad, Permitir y continuar, Ahora no. |
| `06_PermisoSys` | Diálogo de ubicación sobre `08_Permisos`: mapa, Ubicación precisa, Permitir al usar la app, solo esta vez, No permitir. Es del sistema: no se implementa, solo se dispara. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | No aplica: el diálogo es del sistema. |
| Vacío | No aplica. |
| Error | Permiso denegado de forma permanente: enlace a Ajustes del sistema. |
| Sin red | Funciona igual; la preferencia se sincroniza después. |
| Éxito | Navega a `Inicio`. |

### Validaciones

- `POST_NOTIFICATIONS` solo en Android 13+.
- Cámara no se pide aquí si el texto dice "solo cuando abres las postales": se pide en contexto (`specs/027`).

### Dudas abiertas

- **D18** Permisos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Gustos` → `Permisos` → `Inicio`.
- Ahora no → `Inicio`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `PATCH` | `/api/v1/usuarios/yo/privacidad` | JWT | ubicacion, aprendizaje_personalizado, estado_animo, datos_uso, buscable_por_telefono (parcial) | 200: privacidad completa | 400, 401, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

Sin entidades. `privacidad.ubicacion` en `UserProfileEntity`.

## 5. Seguridad y privacidad

- Cada permiso exige fila en `docs/permissions-matrix.md`.
- La app funciona si se niegan (constitución §6).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: flujo de decisión por permiso.
- Data: `PATCH /usuarios/yo/privacidad` con MockWebServer escribe Room tras la decisión, y la UI lee de Room.
- Security: el manifiesto declara solo los permisos de la matriz.
- UI: Ahora no navega sin diálogos.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
