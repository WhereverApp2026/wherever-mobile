# NV-50 — Foto de perfil desde galería o cámara

## T1 — Implementación
- [x] Compresión a JPEG ≤ 1600 px con orientación correcta.
- [x] Subida: formulario firmado, S3 sin token y confirmación.
- [x] `foto_url` en DTO, Room 3, dominio y `updatePhoto`.
- [x] Avatar editable con galería (Photo Picker) y cámara (`FileProvider`); vista previa, carga y mensajes.
- [x] Versión 3 (0.2.1).

## T2 — Verificación
- [x] En el teléfono de Jorge (2026-11-18, 17:45): galería y cámara suben (201 → S3 204 → 200 → `PATCH` 200) y la foto se ve en Perfil. La foto de cámara se borró después del servidor (`DELETE /imagenes/{id}` 204).
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] `MigrationTest` en la CI del PR.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `ProfilePhotoViewModelTest` (4)
- [x] Integración: `PhotoUploadIntegrationTest` (6)
- [x] UI: `MigrationTest` (+1)
- [x] Seguridad: sin superficie nueva
- **No hacer**: mandar el token a S3, pedir `CAMERA` o `READ_MEDIA_IMAGES` para esto, o subir fotos personales al probar.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
