# Cómo contribuir a Wherever Móvil

Corto y obligatorio. Manda la constitución (`.specify/memory/constitution.md`); el detalle técnico está en `AGENTS.md`.

## Equipo y reparto

| Persona | Área | Specs |
|---|---|---|
| Jorge | Social, Planes, Reserva y lo compartido (theme, navegación, cliente de API) | nv-17 a nv-30 y la base |
| Osmar | Modo negocio | nv-34 a nv-38 (`docs/equipo/osmar.md`) |
| Diego | Recuerdos, Reseñas y Ayuda | nv-31 a nv-33 (`docs/equipo/diego.md`) |

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
4. **PR a `main`** con Jorge como revisor (`CODEOWNERS` lo pide solo) y `CHECKLIST_PR.md` completo. Se integra solo con el CI en verde.
5. **Nadie hace push directo a `main`.** La rama está protegida.

## Lo que no se toca

- **Theme y `core/designsystem`**, **navegación compartida** (`navigation/`, barra inferior, `NavMotion`) y **cliente de API** (`core/network`, `core/di`, `app/build.gradle.kts`).
- Si necesitas algo de ahí, **abre un Issue** y sigue con otra tarea.
- Excepción permitida, que Jorge revisa en el PR: registrar tus rutas (una línea en `WhereverNavHost` y tus objetos en `Routes.kt`) y agregar la fila o el botón de entrada que pide tu spec en la pantalla indicada.

## Reglas de calidad

- **Diseño igual a Figma** (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2»), en claro y oscuro. **Sin valores sueltos**: espacios, radios, colores y tipografía solo desde `core/designsystem`; componentes `Wherever*`.
- **API real y nada local**: sin datos simulados ni listas fijas. Si falta un endpoint, el control no se dibuja y se anota en la spec.
- **Estados**: cargando, error con Reintentar, vacío y sin red en cada pantalla.
- **Textos en `strings.xml`** (español); código y nombres en inglés.
- **Pruebas**: unitarias del ViewModel, de datos con MockWebServer, de UI con Compose y la captura en `ScreenshotTest`.
- Sin secretos, tokens ni datos personales en el código ni en logs.

## Ayuda

Dudas de la spec: pregunta en el Issue de tu spec. Bloqueos del backend: Issue con la etiqueta de tu área y el endpoint que falta.
