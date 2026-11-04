# Checklist - nv-05 Acceso, registro y onboarding

> Trazabilidad: `spec R1-R11 → plan §3-§5 → task T0-T8`. Estado 2026-11-04: auth y onboarding R5-R8 implementados y verificados; aprobación de revisor abierta.
> Los checks de requisitos acreditan que el requisito está **especificado**, no necesariamente implementado. La implementación se marca por separado.

- [x] R1 - Bienvenida/auth gate definidos e implementados con reanudación de onboarding.
- [x] R2 - Login email/contraseña, errores, 429 y Google diferido implementados.
- [x] R3 - Registro, términos, política, 409 y verificación de correo implementados.
- [x] R4 - Recuperación genérica, reenvío, código manual y reset implementados.
- [x] R5 - Preferencias de onboarding implementadas con guardado y omisión.
- [x] R6 - Racional y permiso de ubicación implementados; denegar continúa.
- [x] R7 - Sugerencias y solicitud de amigos implementadas con estados vacío/enviado.
- [x] R8 - Notificaciones y cámara informativa implementadas; permisos opcionales.
- [x] R9 - `TokenStore`, refresh único, rotación, sesión offline y limpieza implementados.
- [x] R10 - 14 rutas tipadas auth implementadas; salida a `home` sigue `[planificado nv-06]`.
- [x] R11 - Tests, documentación viva y gates de auth ejecutados; cobertura pendiente.
- [x] Frames auth y onboarding cubiertos (preferencias, ubicación, amigos, permisos).
- [x] Rutas reales verificadas; respuestas exitosas de onboarding pendientes de OpenAPI.
- [x] `docs/routes.md` contiene las 14 rutas auth/onboarding implementadas.
- [x] `docs/permissions-matrix.md` actualizado con ubicación, notificaciones y cámara informativa.
- [ ] `README.md`, `CLAUDE.md`, `CHECKLIST_PR.md` y `docs/figma-inventory.md` reconciliados.
- [x] `git diff --check` sin errores (solo avisos de fin de línea si aplica).
- [x] Implementación auth R1-R4/R9-R10 verificada con APK y emulador.
- [x] Tests JVM 75 + instrumentados 17 verdes; 0 fallos.
- [x] `assembleDebug`/lint/ktlint/detekt sin errores; cobertura ≥45 % pendiente `[planificado nv-03]`.
- [x] Onboarding R5-R8 implementado y probado.
- [ ] `MigrationTestHelper` de `1→2`, enmienda constitucional `1.2.0`, cobertura y aprobación firmada de revisor.
