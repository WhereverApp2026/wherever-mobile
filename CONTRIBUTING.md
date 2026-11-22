# Cómo contribuir a Wherever Móvil

Corto y obligatorio. Manda la constitución (`.specify/memory/constitution.md`); el detalle técnico está en `AGENTS.md`.

## Equipo y reparto

Reparto desde el 2026-11-22. Lo ya hecho (nv-06 a nv-16, nv-18, nv-19, nv-22, nv-28, nv-31 a nv-34) se queda con quien lo hizo para corregir lo que salga.

| Persona | GitHub | Área | Specs | Lista de trabajo |
|---|---|---|---|---|
| Jorge | `JaRe0528` | Social y Explorar; lo compartido (theme, navegación, cliente de API, CI) | nv-20, nv-21, nv-25 | `docs/equipo/jorge.md` |
| Diego | `DiegoLopEs1250` | Planes y cupones del negocio; cerrar sus PR abiertos | nv-23, nv-24, nv-26, nv-30, nv-37 | `docs/equipo/diego.md` |
| Borre | `Borre4023` | Modo negocio | nv-35, nv-36, nv-38 | `docs/equipo/borre.md` |
| Osmar | `OsmarYahir` | Reserva, pago y cupones; feed social de Inicio; endpoints del backend que faltan; **revisor único** | nv-17, nv-27, nv-29 | `docs/equipo/osmar.md` |

## Antes de empezar

- **JDK 17** (`JAVA_HOME` apuntando a él). Con otra versión detekt falla.
- `local.properties` desde `local.properties.example` y `app/google-services.json` (pídeselo a Jorge; no se sube a Git).
- Lee tu spec completa (`specs/nv-XX/spec.md`, `plan.md`, `task.md`) antes de escribir código.

## Flujo de trabajo

1. **Una rama por spec**, desde `main` actualizado:
   ```bash
   git switch main && git pull
   git switch -c feat/nv-XX-<tema>
   ```
2. Trabaja siguiendo `task.md` y marca sus casillas.
3. **Compuertas una por una** (no en paralelo, no juntas en un solo comando):
   ```bash
   ./gradlew --no-daemon --max-workers=1 ktlintCheck
   ./gradlew --no-daemon --max-workers=1 detekt
   ./gradlew --no-daemon --max-workers=1 lintDebug
   ./gradlew --no-daemon --max-workers=1 testDebugUnitTest
   ./gradlew --no-daemon --max-workers=1 koverVerifyDebug
   ./gradlew --no-daemon --max-workers=1 assembleDebug
   ```
   Si una falla, se arregla antes de seguir con la siguiente.
4. **PR a `main`** desde tu propia cuenta de GitHub, con `CHECKLIST_PR.md` completo. `CODEOWNERS` pide la revisión de Osmar sola.
5. **Nadie hace push directo a `main`.** La rama está protegida (ver "Aprobación").

## Aprobación

- **Solo Osmar aprueba.** La regla de `main` (`.github/rulesets/main.json`) exige 1 aprobación de un code owner (`@OsmarYahir` o `@WhereverApp2026`), el CI `gates (constitución §8)` en verde, la rama al día con `main` y las conversaciones resueltas. No hay excepciones, tampoco para la cuenta dueña.
- Si subes commits después de la aprobación, se anula y hay que volver a pedirla.
- Los PR que abre `OsmarYahir` los aprueba la cuenta dueña `WhereverApp2026`, que solo usa Osmar. **Nadie más usa esa cuenta**: cada quien trabaja con la suya.
- Sin aprobación no se puede fusionar. Con aprobación, el autor puede fusionar su PR.

## Lo que no se toca

- **Theme y `core/designsystem`**, **navegación compartida** (`navigation/`, barra inferior, `NavMotion`) y **cliente de API** (`core/network`, `core/di`, `app/build.gradle.kts`).
- Si necesitas algo de ahí, **abre un Issue** y sigue con otra tarea.
- Excepción permitida, que se revisa en el PR: registrar tus rutas (una línea en `WhereverNavHost` y tus objetos en `Routes.kt`) y agregar la fila o el botón de entrada que pide tu spec en la pantalla indicada.

## Reglas de calidad

- **Diseño igual a Figma** (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2»), en claro y oscuro. **Sin valores sueltos**: espacios, radios, colores y tipografía solo desde `core/designsystem`; componentes `Wherever*`.
- **API real y nada local**: sin datos simulados ni listas fijas. Si falta un endpoint, el control no se dibuja y se anota en la spec.
- **Estados**: cargando, error con Reintentar, vacío y sin red en cada pantalla.
- **Textos en `strings.xml`** (español); código y nombres en inglés.
- **Pruebas**: unitarias del ViewModel, de datos con MockWebServer, de UI con Compose y la captura en `ScreenshotTest`.
- Sin secretos, tokens ni datos personales en el código ni en logs.

## Ayuda

Dudas de la spec: pregunta en el Issue de tu spec. Bloqueos del backend: Issue con la etiqueta de tu área y el endpoint que falta.
