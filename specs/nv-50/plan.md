# plan.md — NV-50 — Foto de perfil desde galería o cámara

> Deriva de `specs/nv-50/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/media/ImageCompressor.kt`: `ImageCompressor`, `AndroidImageCompressor`, `CompressedImage`
- `core/di/Qualifiers.kt` (`@UploadClient`) y `core/di/NetworkModule.kt` (cliente sin `AuthHeader`)
- `feature/photos/domain/Photos.kt`, `feature/photos/data/PhotoRepositoryImpl.kt` (con `ImagesApi` y DTO), `data/di/PhotosModule.kt`
- `feature/profile/{data,domain}`: `photoUrl` y `updatePhoto`; `core/database`: versión 3 y `MIGRATION_2_3`; `app/schemas/.../3.json`
- `feature/profile/ui/ProfilePhoto.kt` (`ProfileAvatar`, `EditableProfileAvatar`) y `ProfilePhotoViewModel.kt`; `ProfileScreen.kt`
- `AndroidManifest.xml` (`FileProvider`) y `res/xml/file_paths.xml`; `docs/permissions-matrix.md`; `strings.xml`

## 2. Guardarraíles
- El token no sale a S3: cliente aparte, sin `AuthHeader` ni `Authenticator`.
- Sin permisos nuevos: Photo Picker y cámara del sistema.
- Room migra sin borrar la caché.

## 3. Pruebas
### Unitarias
- `ProfilePhotoViewModelTest` (4)

### Integración
- `PhotoUploadIntegrationTest` (6)

### UI
- `MigrationTest` (+1), en la CI del PR

### Seguridad
- `PlatformSecurityTest`, `SourceSecurityTest` y `ArchitectureTest` sin cambios

## 4. Secuencia
1. Probar `POST /imagenes/subida` contra staging.
2. Compresión, subida y perfil; pruebas.
3. En el teléfono: galería y cámara; capturas; compuertas; PR.

## 5. Riesgos
- Moderación del backend: una foto puede quedar `en_revision`; la app lo avisa y no cambia el perfil.
