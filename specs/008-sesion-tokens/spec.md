# 008 — Sesión y tokens

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: Sin pantalla. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Guardar tokens cifrados y renovarlos una sola vez ante un 401, sin que la UI lo note.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-158.** Como usuario quiero que mi sesión sea segura y no se interrumpa.

- [x] Tokens cifrados con DataStore + Keystore.
- [x] El interceptor agrega Bearer en rutas con JWT.
- [x] Ante 401 se renueva una sola vez y se repite la petición.
- [x] Guarda siempre el refresh nuevo, porque rota.
- [x] Si el servidor rechaza el refresh, limpia la sesión y va a Login.

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

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md` y `task.md`.

## Implementado (2026-11-04)

**Hecho**

- Cadena `RequestId → AuthHeader → RedactedLogging` + `TokenRefreshAuthenticator`, con lock y cliente aparte para el refresh.
- `DataStore` cifrado con AES-256-GCM de Android Keystore; descifrado fallido equivale a sin sesión.
- Las rutas públicas se marcan con `@PublicEndpoint`.
- El log solo tiene método, ruta sin parámetros, estado e ids; nunca cabeceras ni cuerpos.
- 429 llega como `AppError.RateLimited(retryAfterSeconds)`; cada pantalla hace la cuenta atrás. No se reintenta solo.

**Difiere de lo especificado**

- Refresh con red caída, 429 o 503: conserva la sesión, no va a Login (D16). Solo 401 o 422 del refresh la terminan.

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `core/network/*`
- `core/security/*`
- `core/di/NetworkModule.kt`
- `core/di/SecurityModule.kt`

**Pruebas**

- `core/network/TokenRefreshAuthenticatorTest.kt` (incluye dos 401 simultáneos = un solo refresh; se comprobó que falla sin el lock)
- `core/network/InterceptorsTest.kt`
- `core/network/SafeApiCallTest.kt`
- `core/security/EncryptedTokensSerializerTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: la renovación con un token vencido real; esa parte solo tiene pruebas unitarias.
- `KeystoreTokenCipher` no tiene prueba automatizada. En el teléfono, la sesión sobrevivió a detener y reabrir la app, lo que ejercita Keystore de verdad.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada y probada en un teléfono, sin revisor.
