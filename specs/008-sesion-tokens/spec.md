# 008 — Sesión y tokens

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: Sin pantalla. **Sprint 1.** La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Guardar tokens cifrados y renovarlos una sola vez ante un 401, sin que la UI lo note.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-158.** Como usuario quiero que mi sesión sea segura y no se interrumpa.

- [ ] Tokens cifrados con DataStore + Keystore.
- [ ] El interceptor agrega Bearer en rutas con JWT.
- [ ] Ante 401 se renueva una sola vez y se repite la petición.
- [ ] Guarda siempre el refresh nuevo, porque rota.
- [ ] Si el servidor rechaza el refresh, limpia la sesión y va a Login.

### UI

Sin pantalla.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Transparente para la UI. |
| Vacío | Sin tokens: sesión ausente. |
| Error | Refresh rechazado → `SessionEvents` → Login conservando destino. |
| Sin red | Refresh sin red: la sesión se conserva (ver D16). |
| Éxito | La petición original se repite con el token nuevo. |

### Validaciones

- Dos 401 simultáneos producen un solo `POST /auth/refresh`.
- `expira_en` en UTC; reloj inyectado.

### Dudas abiertas

- **D16** Refresh con fallo transitorio (`specs/README.md`)
- **D17** Backend fuera de horario (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Solo emite el evento que lleva a `Login`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/refresh` | refresh_token · 10/min por IP | refresh_token | 200: access_token, refresh_token, expira_en, usuario{id, nombre, correo, correo_verificado, roles[]} | 400, 401, 422, 429, 503 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

`TokenStore`: access, refresh y `expira_en`, cifrados. Nunca en Room. Diseño en `specs/000-arquitectura §3.2` y `§5.1`.

## 5. Seguridad y privacidad

- Reusar un refresh viejo cierra todas las sesiones: el lock es obligatorio.
- Sin tokens en logs, `SharedPreferences` ni respaldos.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Casos de `specs/000-arquitectura §6`.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
