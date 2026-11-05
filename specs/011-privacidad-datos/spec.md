# 011 — Privacidad, mis datos y eliminar cuenta

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Dar control sobre los datos personales, incluida su descarga y la eliminación de la cuenta.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-007.** Como usuario quiero ver, descargar y borrar mis datos.

- [x] Interruptores de ubicación, aprendizaje, ánimo y datos de uso.
- [ ] Descargar mis datos: enlace por correo, 24 h.
- [ ] Borrar historial.
- [x] Eliminar cuenta escribiendo ELIMINAR, efectiva en 30 días y cancelable.

**US-236.** Como usuario quiero decidir quién puede encontrarme.

- [x] Interruptor buscable por teléfono, apagado por defecto.
- [x] Los cambios aplican al instante.

### UI

| Frame | Contenido |
|---|---|
| `28_PrivSocial` | Quién ve mi actividad (Solo mis amigos, Todos, Nadie), Qué comparto (reseñas, planes, postales), Encontrarme (sugerencias, solicitudes). |
| `EliminarCuenta` | "¿Eliminar tu cuenta?", consecuencias, casilla "Entiendo…", Mejor me quedo, Eliminar mi cuenta (rojo). |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Interruptores desde Room; el que cambia muestra su avance. |
| Vacío | No aplica. |
| Error | 429 en exportar: "Ya pediste una copia hoy" con `Retry-After`. 409 al cancelar: fuera de plazo. 422 si `confirmacion` no es exacta. El interruptor regresa si falla. |
| Sin red | Interruptores encolados; acciones destructivas deshabilitadas. |
| Éxito | Eliminar: `eliminar_en`, cierre de sesión local y pantalla de pendiente con Cancelar eliminación. |

### Validaciones

- `confirmacion` exactamente ELIMINAR.
- Confirmar antes de borrar el historial.
- Con eliminación pendiente solo responden `GET /usuarios/yo`, cancelar y logout; lo demás da 403.

### Dudas abiertas

- **D03** Eliminar cuenta: la maqueta contradice a la API (`specs/README.md`)
- **D04** Privacidad: dos pantallas distintas (`specs/README.md`)
- **D13** Estados sin frame (`specs/README.md`)
- **D15** Cuenta con eliminación pendiente (`specs/README.md`)
- **D35** Descargar mis datos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Ajustes` o `EditarPerfil` → `PrivacidadSocial`.
- `DatosPersonales` o `NegPerfil` → `EliminarCuenta` → `Login`.
- Enlace de exportación → descarga (sin frame).
- `SessionGate` con eliminación pendiente → `EliminacionPendiente` (sin frame).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `PATCH` | `/api/v1/usuarios/yo/privacidad` | JWT | ubicacion, aprendizaje_personalizado, estado_animo, datos_uso, buscable_por_telefono (parcial) | 200: privacidad completa | 400, 401, 422 |
| `POST` | `/api/v1/usuarios/yo/exportar` | JWT | — | 202 (una por día) | 401, 429 con Retry-After |
| `DELETE` | `/api/v1/usuarios/yo/historial` | JWT | — | 204 | 401, 503 |
| `DELETE` | `/api/v1/usuarios/yo` | JWT | confirmacion: "ELIMINAR" | 202: eliminar_en | 400, 401, 422 |
| `POST` | `/api/v1/usuarios/yo/cancelar-eliminacion` | JWT | — | 200: Perfil completo | 401, 409 |
| `POST` | `/api/v1/usuarios/exportaciones/descargar` | token del enlace · 10/min por IP | token | 200: un objeto por colección | 400, 410, 422, 429 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Quién ve mi actividad, Qué comparto, Aparecer en sugerencias y Recibir solicitudes: no existen en `privacidad`.

## 4. Modelo de datos

`UserProfileEntity.privacidad`, `estado`, `eliminar_en`.

## 5. Seguridad y privacidad

- El JSON exportado se guarda solo donde la persona elija.
- Token del enlace fuera de logs; es de un solo uso.
- Tras eliminar se limpian tokens y Room.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: botón activo solo con ELIMINAR exacto.
- Data: 202, 409 y 429 con MockWebServer.
- Unit: 403 con eliminación pendiente lleva a su pantalla.
- Security: el token de un solo uso de exportar no se registra en logs ni queda en Room tras descargar.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md` y `task.md`.

## Implementado (2026-11-04)

**Hecho**

- Privacidad usa los 5 interruptores de la API, en el estilo de `28_PrivSocial` (D04).
- Eliminar cuenta sigue a la API: se escribe ELIMINAR y hay 30 días para cancelar (D03). Al aceptar limpia la sesión local y va a Login.
- Pantalla de eliminación pendiente (sin frame, D13): se detecta con `eliminar_en`, y permite cancelar con `POST /usuarios/yo/cancelar-eliminacion` o cerrar sesión.

**Difiere de lo especificado**

- Los controles del frame `28_PrivSocial` no están: no existen en la API (D04).
- Descargar mis datos y Borrar historial no están implementados (D35): no estaban en este encargo.
- Confirmado en staging: `/auth/login` funciona con eliminación pendiente y lleva a su pantalla (D15).

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/settings/ui/PrivacyScreen.kt`
- `feature/settings/ui/PrivacyViewModel.kt`
- `feature/settings/ui/DeleteAccountScreen.kt`
- `feature/settings/ui/DeleteAccountViewModel.kt`
- `feature/settings/ui/DeletionPendingScreen.kt`
- `feature/settings/ui/DeletionPendingViewModel.kt`

**Pruebas**

- `feature/settings/ui/SettingsViewModelsTest.kt`
- `feature/profile/data/ProfileRepositoryImplTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: Cancelar eliminación: no se pulsó, para no revertir el borrado de la cuenta de prueba.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada y probada en un teléfono, sin revisor.
