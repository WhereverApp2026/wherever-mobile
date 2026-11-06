# NV-20 — Amigos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 3 · Amigos. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Agregar y administrar amigos, y ver sus perfiles.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-232 / US-233 (fuera de alcance por ahora; criterios inferidos de la maqueta).** Como usuario quiero agregar amigos por usuario, contactos o QR.

- [ ] Enviar, aceptar, rechazar y cancelar solicitudes.
- [ ] Escanear un QR agrega a la persona.
- [ ] Bloquear impide que me busque o escriba.

### UI

| Frame | Contenido |
|---|---|
| `07_EncuentraAmigos` | Paso 3 de 4, "Con amigos es mejor", Buscar en mis contactos, Ya están aquí con Agregar, Seguir, Más tarde. |
| `EncuentraAmigos · solicitud enviada` | Variante: Agregar pasa a Pendiente. |
| `22_AmigosLista` | Pestañas Amigos, Solicitudes; buscador; lista con Recientes; Mis grupos. |
| `23_AmigosSolic` | Recibidas con Aceptar y rechazar; Enviadas con Cancelar; aviso de privacidad. |
| `24_AgregarAmigo` | Buscador, Mi código QR, Escanear, Mis contactos, Quizá los conoces, Compartir mi enlace. |
| `AgregarAmigo · solicitud enviada` | Variante: Agregar pasa a Pendiente. |
| `25_MiCodigo` | Pestañas Mi código, Escanear; QR con nombre y usuario; Compartir, Guardar imagen. |
| `26_PerfilAmigo` | Perfil público: Invitar a un plan, Amigos, contadores, insignias, en común, Mensaje, pestañas con galería. |
| `PerfilAmigo · *` | Variantes por persona (Alexander, Diego, Luis, Mariana, Paola, Sofía). |
| `27_AmigoOpc` | Hoja: Silenciar, Invitar a un grupo, Eliminar de mis amigos, Bloquear. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto de filas. |
| Vacío | Sin amigos: invita a agregar. Sin solicitudes: mensaje (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Botón pasa de Agregar a Pendiente, y Pendiente a Amigos. |

### Validaciones

- El QR contiene `codigo_amigo`, sin tokens.
- Confirmar antes de eliminar o bloquear.

### Dudas abiertas

- **D12** Onboarding: paso de amigos y cómo saber si terminó (`specs/README.md`)
- **D18** Permisos (`specs/README.md`)
- **D22** Barra inferior inconsistente (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Permisos`/onboarding → `EncuentraAmigos`.
- `Inicio` → `AmigosLista` → `AgregarAmigo`, `MiCodigo`, `PerfilAmigo`, `MisGrupos`.
- `PerfilAmigo` → `ChatAmigo`, `InvitarPlan`, hoja de opciones.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `GET` | `/api/v1/usuarios/yo` | JWT | — | 200: Perfil completo: id, nombre, correo, correo_verificado, foto_url, codigo_amigo, roles[], modo_actual, ciudad{id,nombre}|null, gustos{...}|null, preferencias{...}, privacidad{...}, telefono_registrado, puntos, nivel, racha_semanas, estado, eliminar_en, version, creado_en, actualizado_en. Cabecera ETag | 401 |
| `PATCH` | `/api/v1/usuarios/yo/privacidad` | JWT | ubicacion, aprendizaje_personalizado, estado_animo, datos_uso, buscable_por_telefono (parcial) | 200: privacidad completa | 400, 401, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Solicitudes: enviar, aceptar, rechazar, cancelar.
- Lista de amigos y sugerencias.
- Búsqueda por usuario, teléfono y `codigo_amigo`.
- Perfil público de otra persona.
- Silenciar, eliminar y bloquear.
- Coincidencia con contactos.

## 4. Modelo de datos

`FriendEntity` y `FriendRequestEntity` en Room.

## 5. Seguridad y privacidad

- Contactos y cámara se piden en contexto, y deben entrar en la matriz.
- Los contactos se envían con hash o no se envían: por definir con backend.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: máquina de estados de la solicitud.
- Data: lista con cursor.
- UI: botón según estado.
- Security: los contactos no viajan en claro ni se registran en logs (hash o no envío, según el backend).
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
