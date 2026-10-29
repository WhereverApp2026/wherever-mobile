# MASVS Checklist — vivo

> Constitución §6. Trazable a `nv-01`. Referencia: OWASP MASVS.

- [ ] STORAGE: tokens cifrados con Keystore; sin datos sensibles en logs, capturas ni respaldos
- [ ] CRYPTO: sin cripto propia; solo APIs de Keystore y bibliotecas mantenidas
- [ ] AUTH: refresh seguro, un solo reintento ante 401, sesión limpia si falla
- [ ] NETWORK: solo HTTPS (`networkSecurityConfig`), sin confiar certificados a ciegas
- [ ] PLATFORM: `allowBackup` controlado, componentes no exportados salvo necesidad, permisos mínimos
- [ ] CODE: dependencias revisadas, `lint` y `detekt` como error, sin código de depuración en release
- [ ] RESILIENCE: R8 activo en release; sin datos de depuración en el APK
- [ ] PRIVACY: aviso de privacidad y eliminación de cuenta (posterior), permisos en contexto
