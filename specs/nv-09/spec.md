# NV-09 — Registro con correo

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Crear la cuenta en menos de un minuto y dejar la sesión iniciada para seguir el onboarding.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-012.** Como persona nueva quiero registrarme con correo y contraseña para usar Wherever.

- [x] Campos nombre, fecha de nacimiento (18+), correo y contraseña.
- [x] Valida al salir de cada campo.
- [x] Avisa si el correo ya existe, antes de enviar y ante un 409.
- [x] Envía el correo de verificación.
- [x] Queda con sesión iniciada (201).

### UI

| Frame | Contenido |
|---|---|
| `05_Registro` | Paso 1 de 4, "Crea tu cuenta", Nombre, Correo, Contraseña, tarjeta de ciudad con Cambiar, casilla de términos, Crear cuenta, Continuar con Google. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador en Crear cuenta; comprobación de correo discreta junto al campo. |
| Vacío | No aplica. |
| Error | 409: "Ese correo ya tiene cuenta" con enlace a Login (sin frame). 422: `campos[].mensaje` bajo su campo. 429: límite de 5/h, mensaje con reintento. |
| Sin red | Sin red o `503`: conserva lo escrito y muestra aviso con Reintentar (`AppError.Network`/`Server`). |
| Éxito | Guarda tokens y pasa a `Gustos`. |

### Validaciones

- Nombre de 2 a 40 caracteres.
- Fecha de nacimiento AAAA-MM-DD, mayor de 18 años; reloj inyectado.
- Correo válido y disponible (`correo-disponible`, al salir del campo, con 20/min de límite).
- Contraseña de 8 a 128 caracteres, con letra y número.
- Casilla de términos obligatoria (`acepta_terminos=true`).

### Dudas abiertas

- **D01** Registro sin fecha de nacimiento (`specs/README.md`)
- **D10** Google Sign-In (`specs/README.md`)
- **D11** Enlaces profundos (`specs/README.md`)
- **D13** Estados sin frame (`specs/README.md`)
- **D14** Términos y aviso de privacidad (`specs/README.md`)
- **D19** Ciudad al registrarse (`specs/README.md`)
- **D20** Correo sin verificar (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Bienvenida` o `Login` → `Registro` → `Gustos` (`specs/006`).
- Enlaces de términos y aviso → textos (sin frame).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `GET` | `/api/v1/usuarios/correo-disponible` | no · 20/min por IP | ?correo= | 200: disponible | 400, 422, 429 |
| `POST` | `/api/v1/usuarios` | no · 5/h por IP | nombre, fecha_nacimiento, correo, password, acepta_terminos, version_terminos, promociones_correo, dispositivo{modelo, sistema, app_version} (opcional) | 201: access_token, refresh_token, expira_en, usuario{id, nombre, correo, correo_verificado, roles[]} | 400, 409, 422, 429 |
| `GET` | `/api/v1/ciudades/autocompletar` | no | ?q= (2+ letras) &limite= (8, máx. 50) | 200: [id, nombre, estado] | 400, 422 |
| `POST` | `/api/v1/auth/verificar-correo` | token del enlace · 10/min por IP | token | 200: correo_verificado | 400, 422, 429 |
| `POST` | `/api/v1/auth/reenviar-verificacion` | JWT | — | 202 (máx. uno cada 60 s) | 401, 429 con Retry-After |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

`UserProfileEntity` desde `usuario`. El borrador del formulario no se persiste en disco.

## 5. Seguridad y privacidad

- Contraseña y fecha de nacimiento fuera de los logs.
- La contraseña no se guarda en `SavedStateHandle`.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: validación de edad en el límite de 18 años con reloj inyectado.
- Unit: `RegisterViewModel` con 201, 409, 422 y 429.
- Data: cuerpo enviado con MockWebServer.
- UI: errores al salir del campo.
- Security: la contraseña y la fecha de nacimiento no aparecen en el cuerpo registrado de `POST /usuarios`.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Implementado (2026-11-04)

**Hecho**

- Validación en vivo de nombre, edad y contraseña; el correo se comprueba con `GET /usuarios/correo-disponible` al salir del campo.
- Se agregó la fecha de nacimiento, que la maqueta no tiene y la API exige (D01). Se elige con `DatePicker`.
- `version_terminos` viene de `BuildConfig.TERMS_VERSION`; `promociones_correo` es `false` (D14).
- Los errores `campos[]` de la API se muestran bajo su campo.

**Difiere de lo especificado**

- Sin tarjeta de ciudad: la API no la acepta aquí, y se pide en Gustos (D19).
- Sin Google (D10).
- Barra de pasos "1 de 2": amigos y permisos no existen aún (D12).
- Los enlaces a términos y aviso no abren nada: no hay URL ni frame (D14).

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/auth/ui/RegisterScreen.kt`
- `feature/auth/ui/RegisterViewModel.kt`
- `feature/auth/domain/RegistrationValidator.kt`

**Pruebas**

- `feature/auth/ui/AuthViewModelsTest.kt`
- `feature/auth/data/AuthRepositoryImplTest.kt`
- `core/common/ValidatorsTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: correo ya registrado (409) y límite de intentos (429).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Implementada, probada en un teléfono y aprobada (Jorge, 2026-11-06).
