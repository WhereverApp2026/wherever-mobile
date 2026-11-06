# NV-06 — Splash y bienvenida

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Decidir el primer destino de la app en menos de 2 s y presentar el producto a quien llega por primera vez.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-001.** Como usuario quiero que la app abra rápido y me lleve al lugar correcto para no repetir pasos.

- [ ] Usa SplashScreen API, entre 0.8 y 2 s.
- [x] Sin tokens → Bienvenida.
- [x] Sesión válida → Inicio.
- [x] Sesión vencida (refresh rechazado) → Login.
- [ ] Sin red → Sin conexión con Reintentar (`specs/012`).

### UI

| Frame | Contenido |
|---|---|
| `01_Bienvenida` | Fondo azul degradado con ruta ilustrada, logo, "Tu día completo, en una sola palabra.", subtítulo, botones Crear cuenta (naranja) y Ya tengo cuenta (contorno). |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Splash del sistema, 0.8 a 2 s como máximo aunque la API no responda. |
| Vacío | No aplica. |
| Error | La validación falla por 5xx: entra con la caché si hay sesión; si no, Bienvenida. |
| Sin red | `89_SinConexion` con Reintentar. |
| Éxito | Navega al destino y limpia la pila. |

### Validaciones

- La sesión se valida con `GET /usuarios/yo`; el `Authenticator` renueva el token si hace falta.
- Un tiempo límite de 2 s evita que la app se quede en el splash.

### Dudas abiertas

- **D13** Estados sin frame (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Inicio de la app → `Bienvenida`, `Inicio`, `Login` o `SinConexion`.
- `Bienvenida` → Crear cuenta: `Registro`. Ya tengo cuenta: `Login`.
- Un enlace profundo se guarda y se retoma al terminar (`specs/003`).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `GET` | `/api/v1/usuarios/yo` | JWT | — | 200: Perfil completo: id, nombre, correo, correo_verificado, foto_url, codigo_amigo, roles[], modo_actual, ciudad{id,nombre}|null, gustos{...}|null, preferencias{...}, privacidad{...}, telefono_registrado, puntos, nivel, racha_semanas, estado, eliminar_en, version, creado_en, actualizado_en. Cabecera ETag | 401 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

Sin entidades propias. Lee `TokenStore` y `UserProfileEntity`.

## 5. Seguridad y privacidad

- No loguea tokens al decidir el destino.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `StartupViewModel` con cada combinación de sesión y red, reloj inyectado.
- UI: botones de Bienvenida navegan.
- Empírico: medir arranque en frío antes de fijar umbral.
- Data: `StartupRepository` con MockWebServer guarda `usuario` en Room con 200; con 401 Room queda vacío.
- Security: el log de arranque no incluye el access token ni el cuerpo de `GET /usuarios/yo`.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Implementado (2026-11-04)

**Hecho**

- SplashScreen API: se mantiene hasta decidir el destino. `MainViewModel` lee `TokenStore` y Room.
- Con sesión entra de inmediato con la caché y valida detrás con `GET /usuarios/yo`. Si el servidor rechaza el refresh, `SessionEvents` lleva a Login.

**Difiere de lo especificado**

- No se fuerza la duración de 0.8 a 2 s: el splash dura lo que tarde en leer los tokens.
- Sin red no va a `SinConexion` (no implementada): con sesión entra con la caché; sin sesión, a Bienvenida.

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/auth/ui/WelcomeScreen.kt`
- `navigation/MainViewModel.kt`
- `MainActivity.kt`

**Pruebas**

- `navigation/MainViewModelTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: el arranque sin red.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada y probada en un teléfono, sin revisor.
