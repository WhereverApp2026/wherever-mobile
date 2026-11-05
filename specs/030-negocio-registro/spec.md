# 030 — Negocio: registro

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 11 · Negocio. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Dar de alta un negocio en 5 pasos, con revisión de Wherever antes de publicarlo.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como dueño quiero registrar mi negocio para recibir reservas.

- [ ] Puedo guardar y salir en cualquier paso.
- [ ] Exige mínimo 3 fotos y documentos.
- [ ] Tras enviar, veo el estado de la revisión.

### UI

| Frame | Contenido |
|---|---|
| `42_NegCuenta` | Paso 1: administrador, correo, teléfono, contraseña y confirmación, términos, Continuar, Guardar y salir. |
| `43_NegRegistro` | Paso 2: nombre comercial, giro, tipo de experiencia, descripción (200), año de apertura. |
| `44_NegUbic` | Paso 3: calle, colonia, municipio, C.P., pin en mapa, WhatsApp, Instagram o sitio. |
| `45_NegOper` | Paso 4: horario, mesas, máximo de personas, rango de precios, servicios, anticipo por persona. |
| `46_NegVerif` | Paso 5: logo, mínimo 3 fotos, identificación, comprobante, constancia fiscal, CLABE, Enviar solicitud. |
| `47_NegListo` | "Solicitud enviada": revisión en 24 a 48 horas, avance, Ir al panel, Editar mi solicitud. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador en Continuar; avance por archivo. |
| Vacío | No aplica. |
| Error | 422 bajo el campo; documento rechazado con motivo. |
| Sin red | Borrador local; envío deshabilitado. |
| Éxito | `47_NegListo`. |

### Validaciones

- Contraseña con las mismas reglas de la API.
- Confirmación coincide.
- Descripción hasta 200.
- C.P. de 5 dígitos.
- CLABE de 18 dígitos con dígito verificador.
- Anticipo ≥ 0.

### Dudas abiertas

- **D27** Cuenta de negocio (`specs/README.md`)
- **D33** Datos fiscales y bancarios (`specs/README.md`)
- **D34** Subir fotos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Ajustes` → `NegCuenta` → `NegRegistro` → `NegUbic` → `NegOper` → `NegVerif` → `NegListo` → `NegPanel`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/usuarios` | no · 5/h por IP | nombre, fecha_nacimiento, correo, password, acepta_terminos, version_terminos, promociones_correo, dispositivo{modelo, sistema, app_version} (opcional) | 201: access_token, refresh_token, expira_en, usuario{id, nombre, correo, correo_verificado, roles[]} | 400, 409, 422, 429 |
| `POST` | `/api/v1/usuarios/yo/modo` | JWT | modo: explorar | negocio | 200: modo_actual | 400, 401, 403, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Alta de negocio y borrador.
- Subida de documentos y fotos.
- Estado de la revisión.
- Otorgar rol comerciante.

## 4. Modelo de datos

Borrador en Room sin documentos ni CLABE.

## 5. Seguridad y privacidad

- Documentos y CLABE fuera de Room y logs; `FLAG_SECURE` en verificación.
- Documentos solo por canal cifrado, con selector del sistema.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: validación de CLABE.
- Unit: reanudar borrador.
- Security: CLABE fuera de logs y de Room.
- Data: el borrador de registro de negocio persiste en Room y se reanuda tras reiniciar.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
