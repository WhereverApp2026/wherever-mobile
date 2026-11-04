# 004 — Recuperar contraseña

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. **Sprint 1.** La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Recuperar el acceso por correo y terminar con la sesión iniciada.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-014.** Como usuario que olvidó su contraseña quiero un enlace por correo para volver a entrar.

- [ ] Envía el enlace y siempre muestra el mismo mensaje, exista o no la cuenta (202).
- [ ] El reenvío se habilita a los 60 s con cuenta atrás.
- [ ] El enlace abre la app en Nueva contraseña.
- [ ] Al guardar queda con sesión iniciada y las otras sesiones se cierran.
- [ ] El enlace vence en 1 h; vencido o usado, ofrece pedir otro.

### UI

| Frame | Contenido |
|---|---|
| `03_Recuperar` | Candado, "¿Olvidaste tu contraseña?", campo Correo, aviso de privacidad ("no te lo diremos"), Enviar enlace, Volver a iniciar sesión. |
| `04_RevisaCorreo` | Sobre, "Revisa tu correo", correo enmascarado, vigencia, ayuda de spam, Abrir correo, Reenviar enlace en 0:45, Usar otro correo. |
| `NuevaContrasena` | "Crea tu nueva contraseña", vigencia, Nueva contraseña con ojo y medidor, Confirmar, "Las contraseñas coinciden", Guardar contraseña. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador en el botón principal. |
| Vacío | No aplica. |
| Error | 422 en `restablecer` por token inválido o vencido: "Este enlace ya no sirve" + pedir otro (sin frame). 429: cuenta atrás con `Retry-After`. |
| Sin red | Sin red o `503`: conserva lo escrito y muestra aviso con Reintentar (`AppError.Network`/`Server`). |
| Éxito | `RevisaCorreo` tras enviar; `Inicio` tras restablecer. |

### Validaciones

- Correo con formato válido.
- Contraseña nueva de 8 a 128 caracteres, con al menos una letra y un número.
- Confirmar debe coincidir (solo en cliente).
- Reenviar deshabilitado 60 s; el reloj se inyecta.

### Dudas abiertas

- **D02** Vigencia del enlace de recuperación (`specs/README.md`)
- **D11** Enlaces profundos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Login` → `Recuperar` → `RevisaCorreo`.
- `RevisaCorreo` → Abrir correo: intent a la app de correo. Usar otro correo: `Recuperar`.
- Enlace `#token=…` → `NuevaContrasena` → `Inicio`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/recuperar` | no · 10/min por IP | correo | 202 sin cuerpo (exista o no la cuenta) | 400, 422, 429 |
| `POST` | `/api/v1/auth/restablecer` | token del enlace · 10/min por IP | token, password_nuevo, dispositivo{modelo, sistema, app_version} (opcional) | 200: access_token, refresh_token, expira_en, usuario{id, nombre, correo, correo_verificado, roles[]} | 400, 422, 429 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

Sin entidades. Tokens en `TokenStore`, `usuario` en `UserProfileEntity`.

## 5. Seguridad y privacidad

- El token del fragmento no entra en rutas ni logs; se descarta tras usarlo.
- App Links verificados para que otra app no capture el enlace.
- El correo se enmascara en pantalla.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: cuenta atrás con reloj inyectado.
- Unit: extracción de `#token=` del `Intent`.
- Data: 202 y 422 con MockWebServer.
- UI: medidor y coincidencia.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
