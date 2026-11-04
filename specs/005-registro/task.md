# 005 — Registro con correo

## T1 — Implementación
- [x] Validación en vivo de nombre, edad y contraseña; el correo se comprueba con `GET /usuarios/correo-disponible` al salir del campo.
- [x] Se agregó la fecha de nacimiento, que la maqueta no tiene y la API exige (D01). Se elige con `DatePicker`.
- [x] `version_terminos` viene de `BuildConfig.TERMS_VERSION`; `promociones_correo` es `false` (D14).
- [x] Los errores `campos[]` de la API se muestran bajo su campo.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
