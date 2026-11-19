# NV-49 — Autenticación en el teléfono: formularios visibles, errores a la vista y reintento de DNS

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Corrige `specs/nv-07`, `nv-08` y `nv-09` tras probarlas en el teléfono de Jorge (Xiaomi 2207117BPG). Rama `fix/auth-telefono`, desde `main`.

## 1. Objetivo

Jorge reportó que en su teléfono fallaban iniciar sesión, crear cuenta y recuperar contraseña. Se reprodujo cada flujo con `adb` y logcat contra staging (2026-11-18, 16:40 a 17:05):

| Flujo | Qué mandó la app | Respuesta | Qué vio la persona |
|---|---|---|---|
| Iniciar sesión, 1.er intento | `POST /auth/login` | `UnknownHostException` (DNS) | "Sin conexión", aunque había red |
| Iniciar sesión, correo inexistente | `POST /auth/login` | 401 (`2fee4da8…`) | Nada: el error quedaba debajo del teclado |
| Iniciar sesión, teléfono en horizontal | — | — | Solo el título y "Entrar" deshabilitado: los campos quedaban fuera de la vista |
| Crear cuenta | `POST /usuarios` → `GET /usuarios/yo` | 201 (`a32f4bc3…`) y 200 | Funciona |
| Iniciar sesión con la cuenta nueva | `POST /auth/login` | 200 (`199c6dbe…`) | Funciona |
| Cerrar sesión | `POST /auth/logout` | 204 (`07e5ba1c…`) | Funciona |
| Recuperar contraseña | `POST /auth/recuperar` | 202 (`f84c9cf7…`) | "Revisa tu correo", pero el correo no llega |

La app sí llama a los endpoints correctos de la documentación. Lo que fallaba en la app era la presentación: el formulario y el error quedaban ocultos. Además faltaba reintentar un fallo de DNS momentáneo. El correo que no llega es del servidor (§3).

## 2. Alcance / No-alcance

### Alcance
- **Formulario en pantalla baja** (`FormScreen`): con menos de 520 dp de alto (horizontal o teclado abierto), el pie con el botón entra al scroll. Así los campos, el error y el botón se alcanzan. Con altura normal, el pie sigue fijo como en la maqueta. Aplica a todas las pantallas con `FormScreen`.
- **Enviar cierra el teclado** (`rememberFormSubmit`) en Login, Registro y Recuperar, para que se vea el resultado (cargando, error o éxito).
- **Teclado de Login**: "Siguiente" en el correo y "Listo" en la contraseña, que envía si se puede.
- **Correo sin espacios**: al escribir se quitan los espacios. El autocorrector del teclado puede meterlos y el correo quedaba inválido.
- **Reintento de DNS** (`RetryingDns`): un `UnknownHostException` se reintenta una vez tras 400 ms en todos los clientes de OkHttp. La petición aún no salió, así que también es seguro en `POST /auth/login`.
- Versión `versionCode 2`, `versionName 0.1.1` (corrección para la versión publicada).

### No-alcance
- Bloquear la rotación: la app sigue funcionando en horizontal.
- El envío de correos de staging (§3): es del backend.
- Restablecer contraseña desde el enlace del correo (US-014, `POST /auth/restablecer`): depende de que el correo llegue y de un App Link verificado.

## 3. Contratos (API y navegación)

Sin cambios de contrato ni rutas. Endpoints verificados en el teléfono: `POST /auth/login`, `POST /usuarios`, `GET /usuarios/yo`, `POST /auth/logout` y `POST /auth/recuperar` (wherever-backend `docs/endpoints.md`, nv-04 y nv-05).

**Para el equipo de API:**
- El correo de recuperación no sale en staging. `configs/.env.example` y `specs/nv-06` del backend dicen que sin `SMTP_HOST` dev y staging solo registran el correo en el log (`LogMailer`), y en `infra/aws-dev` las variables `SMTP_*` son opcionales. La app recibe 202 (`X-Request-Id` `e6a59ed52dba70c566eefc543070ccc6` por curl y `f84c9cf7-2051-4b36-ac98-1af61764e337` desde el teléfono). Hace falta configurar `SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD` y `MAIL_FROM` en staging. Sin eso tampoco llega la verificación de correo.

## 4. Modelo de datos

Sin cambios en Room, en DTO ni en los `UiState`.

## 5. Seguridad y privacidad

- Sin permisos nuevos. El reintento de DNS no cambia la cadena de interceptores (constitución §4): se configura en `baseConfig()` de `NetworkModule`, igual en el cliente de refresh.
- Las pruebas en el teléfono usaron una cuenta de prueba con dominio `example.com`. No se escribió en ningún archivo y el logcat se limpió.
- Capturas en `docs/pruebas/` sin datos reales: el correo de prueba es inexistente o sale enmascarado.

## 6. Tests / Gates

- **Unit:**
  - `test/.../core/network/RetryingDnsTest.kt` (3): un fallo y luego éxito resuelve con una pausa; dos fallos llegan como `UnknownHostException`; sin fallos no espera.
  - `test/.../core/common/ValidatorsTest.kt` (+1): el correo pierde los espacios.
- **Data:** sin superficie nueva de red. `integration/*` siguen verdes con el `Dns` del sistema.
- **UI:** `androidTest/.../core/designsystem/FormScreenCompactTest.kt` (2): con 360 dp de alto el campo se ve y el botón se alcanza con scroll; con 800 dp el pie queda fijo. Corre en la CI del PR.
- **Security:** sin superficie nueva. `ConnectionSecurityTest`, `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes.
- **En el teléfono** (debug 0.1.1, 2026-11-18): error de login visible tras enviar, registro, login, cerrar sesión y recuperar. Capturas en `docs/pruebas/antes_*.png` y `despues_*.png`.

## 7. Plan de entrega

PR "fix: autenticación en el teléfono (nv-49)". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
