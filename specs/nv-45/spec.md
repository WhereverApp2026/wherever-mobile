# NV-45 — Transiciones entre pantallas y pulido de UI

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Amplía `specs/nv-05` (sistema de diseño y navegación). Rama `feat/transiciones-ui`.

## 1. Objetivo

Al cambiar de pantalla, la nueva aparecía de golpe, y un doble toque podía abrir la misma pantalla dos veces. También fallaban otras cosas: el paso de cargando a contenido era brusco, los errores de campo aparecían sin animación y los títulos salían en peso 400. Ahora un solo archivo (`Motion.kt`) fija tiempos y curvas, la navegación ignora los toques repetidos y las fuentes muestran su peso real.

## 2. Alcance / No-alcance

### Alcance
- **Transiciones entre pantallas** (`NavMotion`), en `WhereverNavHost`:
  - Al avanzar, la nueva entra desde la derecha y la de atrás se corre ¼ a la izquierda (300 ms, curvas "emphasized" de Material 3).
  - Al regresar, ocurre lo contrario.
  - Revisa tu correo y Eliminación pendiente aparecen con fundido y una escala de 0.92 a 1.
- **Atrás predictivo**: `android:enableOnBackInvokedCallback="true"`, con `tools:targetApi="33"` (lint `UnusedAttribute` con minSdk 26).
- **Fases de la pantalla** (`PhaseCrossfade`): fundido de 220 ms entre cargando, error y contenido en Perfil, Editar perfil, Ajustes, Sesiones y Privacidad. Solo se anima cuando cambia la fase, no con cada dato nuevo, y no se anima el tamaño: el `SizeTransform` que `togetherWith` trae por defecto dejaba a la vista la fase anterior aunque las animaciones estuvieran apagadas.
- **Botones**: el texto y el indicador de carga cambian con fundido y escala (150 ms).
- **Errores de campo** (`FieldErrorText`): el error aparece y se va con animación (150 ms) y mantiene el texto mientras se cierra. Lo usan `LabeledTextField` y el aviso de consentimiento de Gustos.
- **Animaciones apagadas**: con la escala del animador del sistema en 0, todo cambia sin animación (`LocalMotionEnabled`).
- **Navegación sin doble toque** (`navigation/SafeNavigation.kt`):
  - Un toque solo navega si su pantalla está `RESUMED` (`navigateFrom`, `popFrom`). Durante una transición, ninguna de las dos lo está.
  - Lo que llega como resultado de la API (sesión iniciada, cuenta registrada, correo enviado, gustos guardados, sesión cerrada, cuenta borrada, eliminación cancelada) navega si su pantalla sigue arriba de la pila, aunque la app esté en segundo plano (`navigateOnResult`, `popOnResult`). El evento ya se consumió, y si se descartara la pantalla se quedaría atorada.
- **Tipografía**: Sora (600 y 700) y DM Sans (400, 500, 600 y 700) pasan a archivos estáticos, uno por peso, sacados de las fuentes variables con fontTools (licencia OFL). Con la fuente variable y `FontVariation`, el teléfono dibujaba todos los pesos en 400.
- **Pulido contra la maqueta**:
  - "Cerrar sesión" va en gris en Ajustes (85_Ajustes), con el nuevo parámetro `color` de `WhereverTextLink`.
  - El engrane de Perfil va en un círculo blanco con borde (81_Perfil).
  - El nombre de Perfil va en negrita.

### No-alcance
- Elementos compartidos entre pantallas (shared element): todavía no hay una imagen o tarjeta que pase de una pantalla a otra.
- Animaciones dentro de Inicio y en el mapa: llegan con sus specs.

## 3. Contratos (API y navegación)

Sin cambios en la API ni rutas nuevas (`docs/routes.md` igual). Cambia solo cómo se navega: con la guarda y con transiciones.

## 4. Modelo de datos

Sin cambios en Room ni en los `UiState`. `ScreenPhase` (`LOADING`, `ERROR`, `CONTENT`) es un enum de presentación en `core/designsystem/Motion.kt`.

## 5. Seguridad y privacidad

Sin datos nuevos ni permisos. Las fuentes nuevas son OFL, igual que las anteriores. No se registra nada en los logs.

## 6. Tests / Gates

- **Unit:**
  - `test/.../core/designsystem/MotionTest.kt` (6): regla de animaciones apagadas, transiciones `None` sin animación, transiciones reales con animación, el orden de duraciones y la escala, y el cambio de fase sin animación de tamaño.
  - `test/.../navigation/SafeNavigationTest.kt` (4): solo `RESUMED` navega por toque; un resultado navega si su pantalla sigue arriba, y se ignora si ya navegó o la pila está vacía.
- **Data:** sin superficie nueva (no cambian repositorios ni la API). Las suites `integration/*` siguen verdes.
- **UI:**
  - `androidTest/.../navigation/SafeNavigationUiTest.kt` (3): un doble toque abre una vez; dos toques en atrás regresan una vez; el atrás del sistema regresa.
  - `androidTest/.../core/designsystem/MotionComponentsTest.kt` (3): un error de campo aparece y se va, el botón cambia a cargando, y con las animaciones apagadas la fase cambia sin animación.
  - `androidTest/.../core/designsystem/TypographyWeightTest.kt` (2): Sora Bold es más ancha que SemiBold, y los pesos de DM Sans se distinguen.
- **Security:** sin superficie nueva. `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes.

## 7. Plan de entrega

PR "feat: transiciones entre pantallas y pulido de UI". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
