# NV-07 — Iniciar sesión

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Entrar con correo y contraseña, recordar la sesión 30 días y volver al destino que abrió la app.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-013.** Como usuario quiero iniciar sesión y que se recuerde para no escribir mi contraseña cada vez.

- [x] Refresh token cifrado (DataStore + Keystore), válido 30 días.
- [x] Tras 5 intentos fallidos, bloqueo de 15 min (423) con mensaje claro.
- [ ] Tras el login regresa al destino del enlace profundo, si lo hubo.
- [x] Correo o contraseña incorrectos (401) muestran un solo mensaje, sin revelar si el correo existe.

### UI

| Frame | Contenido |
|---|---|
| `03_Login` | Regresar, logo, "Qué bueno verte de nuevo", campos Correo y Contraseña, Olvidé mi contraseña, botón Entrar, Continuar con Google, ¿No tienes cuenta? Crea una. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Botón Entrar con indicador y deshabilitado; campos conservan el texto. |
| Vacío | No aplica. |
| Error | 401: "Correo o contraseña incorrectos". 423: "Cuenta bloqueada 15 minutos" (sin frame). 429: espera con `Retry-After`. 422: error bajo el campo. |
| Sin red | Sin red o `503`: conserva lo escrito y muestra aviso con Reintentar (`AppError.Network`/`Server`). |
| Éxito | Guarda tokens, persiste `usuario` y navega al destino pendiente, a onboarding o a Inicio. |

### Validaciones

- Correo con formato válido al salir del campo.
- Contraseña no vacía. No se exige formato al entrar, para no revelar reglas.
- Entrar deshabilitado con campos inválidos.

### Dudas abiertas

- **D10** Google Sign-In (`specs/README.md`)
- **D13** Estados sin frame (`specs/README.md`)
- **D20** Correo sin verificar (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Login` → `Inicio`, destino pendiente o `Onboarding` (`specs/006`).
- `Login` → Olvidé mi contraseña: `Recuperar` (`specs/004`).
- `Login` → Crea una: `Registro` (`specs/005`).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/login` | no · 5 cada 5 min por IP | correo, password, dispositivo{modelo, sistema, app_version} | 200: access_token, refresh_token, expira_en, usuario{id, nombre, correo, correo_verificado, roles[]} | 400, 401, 422, 423, 429, 503 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

`UserProfileEntity` parcial desde `usuario`. Tokens solo en `TokenStore`.

## 5. Seguridad y privacidad

- `password` nunca en logs; se redacta el cuerpo de `/auth/*`.
- Contraseña sin autocompletar por defecto fuera del gestor del sistema; `KeyboardType.Password`.
- No se guarda la contraseña en `SavedStateHandle`.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `LoginViewModel` con 200, 401, 422, 423, 429 y sin red.
- Data: repositorio con MockWebServer guarda tokens rotados.
- Security: 401 de login no dispara refresh.
- UI: validación y mensajes por `testTag`.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md` y `task.md`.

## Implementado (2026-11-04)

**Hecho**

- 401 muestra el `mensaje` de la API. 423 usa texto propio sobre el bloqueo de 15 min. 429 desactiva Entrar con cuenta atrás según `Retry-After`.
- Tras entrar pide `GET /usuarios/yo` y decide el destino: eliminación pendiente, Gustos (si `gustos` es null y no se omitieron) o Inicio.

**Difiere de lo especificado**

- "Continuar con Google" no se muestra (D10).
- No regresa al destino de un enlace profundo: aún no hay enlaces (D11).

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/auth/ui/LoginScreen.kt`
- `feature/auth/ui/LoginViewModel.kt`
- `feature/auth/data/AuthRepositoryImpl.kt`

**Pruebas**

- `feature/auth/ui/AuthViewModelsTest.kt`
- `feature/auth/data/AuthRepositoryImplTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: el bloqueo (423) y el límite de intentos (429).

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada y probada en un teléfono, sin revisor.
