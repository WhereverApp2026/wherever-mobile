# 009 — Ajustes y cerrar sesión

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. **Sprint 1.** La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Reunir las preferencias de la cuenta y permitir salir de forma segura.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-019.** Como usuario quiero cerrar sesión para proteger mi cuenta en este teléfono.

- [ ] Botón en Ajustes con confirmación.
- [ ] Borra tokens y caché personal.
- [ ] Regresa a Iniciar sesión.

### UI

| Frame | Contenido |
|---|---|
| `85_Ajustes` | Cuenta (Datos personales, Correo, Contraseña), Preferencias (Ciudad, Tus gustos, Recordatorios de reservación, Avisos de votaciones), Privacidad (Privacidad social, Usar mi ubicación), Tengo un negocio, Ayuda, Términos y privacidad, Cerrar sesión. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Filas con datos de Room al instante. |
| Vacío | Sin perfil en caché: esqueleto de carga. |
| Error | Interruptor que falla regresa a su valor con aviso. Logout con error: igual limpia localmente. |
| Sin red | Logout limpia localmente. Los interruptores se encolan con WorkManager. |
| Éxito | Interruptor aplicado; logout navega a `Login` limpiando la pila. |

### Validaciones

- Confirmar antes de cerrar sesión (sin frame).
- `notificaciones.reservas` no se puede apagar (422).
- Mapeo: Avisos de votaciones → `notificaciones.grupos`; Usar mi ubicación → `privacidad.ubicacion`.

### Dudas abiertas

- **D06** Recordatorios de reservación (`specs/README.md`)
- **D07** Funciones de API sin pantalla (`specs/README.md`)
- **D13** Estados sin frame (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Perfil` → `Ajustes`.
- → `DatosPersonales` (`specs/010`), `PrivacidadSocial` (`specs/011`), `Gustos` (`specs/006`), `Ayuda` (`specs/029`), `NegCuenta` (`specs/030`).
- Cerrar sesión → `Login`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/logout` | JWT | — | 204 | 401, 503 |
| `PATCH` | `/api/v1/usuarios/yo/preferencias` | JWT | movilidad_default, tema, unidades, horario_silencioso{desde,hasta}, notificaciones{reservas,cupones,grupos,resenas,promociones} (parcial) | 200: preferencias completas | 400, 401, 422 (notificaciones.reservas:false) |
| `PATCH` | `/api/v1/usuarios/yo/privacidad` | JWT | ubicacion, aprendizaje_personalizado, estado_animo, datos_uso, buscable_por_telefono (parcial) | 200: privacidad completa | 400, 401, 422 |
| `PATCH` | `/api/v1/usuarios/yo` | JWT | Cabecera If-Match: "<version>". nombre, foto_url, ciudad_id (parcial) | 200: Perfil completo con versión nueva | 400, 401, 409, 422 |
| `GET` | `/api/v1/ciudades/autocompletar` | no | ?q= (2+ letras) &limite= (8, máx. 50) | 200: [id, nombre, estado] | 400, 422 |
| `POST` | `/api/v1/auth/logout-todos` | JWT | — | 204 | 401, 503 |
| `GET` | `/api/v1/auth/sesiones` | JWT | — | 200: [id, dispositivo{...}, ultimo_uso, actual] | 401 |
| `DELETE` | `/api/v1/auth/sesiones/{id}` | JWT | id en la ruta | 204 | 401, 404 |
| `PUT` | `/api/v1/auth/password` | JWT | password_actual, password_nuevo | 204 | 400, 401, 422 |
| `PUT` | `/api/v1/usuarios/yo/correo` | JWT | correo_nuevo, password | 200: Perfil completo | 400, 401, 409, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

Lee `UserProfileEntity`. Logout borra tablas personales de Room y `TokenStore`.

## 5. Seguridad y privacidad

- Limpieza total aunque falle la red.
- Sin `usuario` residual tras cerrar sesión: lo verifica una prueba.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: logout con 204, 401 y sin red siempre limpia.
- Data: Room vacío tras logout.
- UI: diálogo de confirmación.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
