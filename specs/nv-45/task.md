# NV-45 — Transiciones entre pantallas y pulido de UI

## T1 — Implementación
- [x] `Motion.kt`: duraciones, curvas, transiciones de navegación y fundidos; sin animación si el sistema las apagó.
- [x] Transiciones en `WhereverNavHost` (avanzar, regresar y pantallas de resultado) y atrás predictivo.
- [x] `PhaseCrossfade` en Perfil, Editar perfil, Ajustes, Sesiones y Privacidad.
- [x] Botones con cambio animado a cargando; errores de campo animados (`FieldErrorText`).
- [x] Guarda de navegación: toques solo desde `RESUMED`; resultados si la pantalla sigue arriba.
- [x] Fuentes estáticas por peso (Sora y DM Sans).
- [x] Pulido: "Cerrar sesión" en gris, engrane con borde y nombre en negrita.

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (2026-11-13): `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (240), `koverVerifyDebug` (75,27 %), `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep.
- [x] Pesos de fuente medidos en el teléfono de Jorge antes del cambio: Sora 600 y 700 daban el mismo ancho.
- [ ] Pruebas de UI (`SafeNavigationUiTest`, `MotionComponentsTest`, `TypographyWeightTest`) en la CI del PR.
- [ ] En el teléfono: transiciones, doble toque y atrás predictivo a mano.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `MotionTest` (6), `SafeNavigationTest` (4)
- [x] Integración: sin superficie nueva
- [x] UI: `SafeNavigationUiTest` (3), `MotionComponentsTest` (3), `TypographyWeightTest` (2)
- [x] Seguridad: sin superficie nueva
- **No hacer**: fijar duraciones fuera de `Motion.kt`, descartar un resultado de la API porque la app está en segundo plano, o volver a `FontVariation` con fuentes variables.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
