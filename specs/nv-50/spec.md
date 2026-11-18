# NV-50 — Fotos: foto de perfil desde galería o cámara

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Amplía `specs/nv-14` (perfil). Rama `feat/fotos-perfil`, sobre `feat/inicio-recomendaciones` (nv-47, por Coil).

## 1. Objetivo

Jorge reportó que la app no dejaba poner foto de perfil ni guardar las fotos que se toman. En el teléfono se confirmó que no existía ninguna pantalla de fotos: el avatar era solo la inicial del nombre (duda D34, "la API aún no permite subir fotos"). La API ya lo permite (backend nv-45), así que ahora la persona toca su avatar, elige una foto de la galería o toma una con la cámara, la app la comprime y la sube, y la foto queda guardada en el perfil.

Historias: US-017 (editar perfil: foto). La subida queda como servicio reutilizable (`PhotoRepository`) para las fotos de reseñas (US-118) y postales, que llegan con sus specs.

## 2. Alcance / No-alcance

### Alcance
- **Avatar editable en Perfil** (81_Perfil): al tocarlo abre "Cambiar foto" con "Elegir de la galería" y "Tomar foto".
  - Galería: Photo Picker del sistema (`PickVisualMedia`, solo imágenes). No pide permiso de almacenamiento: ni `READ_MEDIA_IMAGES` en Android 13+ ni `READ_EXTERNAL_STORAGE` antes.
  - Cámara: `TakePicture` con un archivo en `cache/fotos/` compartido por `FileProvider` (`${applicationId}.fotos`). No pide el permiso `CAMERA`, porque la app no usa la cámara directamente: abre la app de cámara del sistema.
  - Mientras sube se ve la foto elegida con un indicador y el avatar no responde a toques. Al terminar, un mensaje dice si se guardó, si quedó en revisión o si se rechazó.
- **Compresión** (`AndroidImageCompressor`): JPEG con calidad 85 y lado mayor de 1600 px como mucho. En Android 9+ con `ImageDecoder`, que respeta la orientación de la cámara; en 8 con `BitmapFactory` y submuestreo.
- **Subida** (`PhotoRepositoryImpl`), contrato de wherever-backend `docs/endpoints.md` (nv-45):
  1. `POST /imagenes/subida {tipo: "image/jpeg"}` → `{id, url, campos, max_bytes}`.
  2. `POST` multipart a `url` (S3) con todos los `campos` y al final el archivo en `file`. Se hace con un cliente sin `Authorization` (`@UploadClient`).
  3. `POST /imagenes/{id}/confirmar` → `{estado, url_1080, url_320}`.
  4. Si quedó `publicada`: `PATCH /usuarios/yo {foto_url: url_1080}` con `If-Match`.
- **Mostrar** la foto con Coil (`ProfileAvatar`) en Perfil. Sin foto, la inicial de siempre.
- **Room 3**: columna `photoUrl` en `user_profile` con `MIGRATION_2_3` (sin borrar la caché).
- Versión `versionCode 3`, `versionName 0.2.1`.

### No-alcance
- Fotos de reseñas y de postales: necesitan el check-in y la reseña (EP-11), que llegan en su spec y reusan `PhotoRepository`.
- Fotos de lugares: en la API solo el dueño del negocio las sube (backend nv-55, modo negocio).
- Quitar la foto: `PATCH /usuarios/yo` no acepta borrar `foto_url` (pide `https`).
- Recortar la foto: el backend la recodifica a 1080 y 320 px; el avatar se recorta en círculo al mostrarla.

## 3. Contratos (API y navegación)

Sin rutas nuevas. Endpoints: `POST /imagenes/subida`, `POST /imagenes/{id}/confirmar` y `PATCH /usuarios/yo` (`foto_url`). Contra staging (2026-11-18, 17:08, con la cuenta de prueba): `POST /imagenes/subida` → 201 (`X-Request-Id` `8cb1087d0bc8b2000ae881bb71854fa3`), con un formulario de S3 (`key`, `policy`, `X-Amz-*`, `Content-Type`) que vence en 5 min y un tope de 5 242 880 bytes.

## 4. Modelo de datos

- `UserProfileDto.photoUrl` (`foto_url`), `ProfilePatchDto.photoUrl`, `UserProfileEntity.photoUrl` (columna nueva), `UserProfile.photoUrl`.
- `ProfileRepository.updatePhoto(url)`.
- `feature/photos/domain`: `UploadedImage(id, state, url, thumbnailUrl)`, `ImageState` y `PhotoRepository`.
- Room pasa a la versión 3 con `MIGRATION_2_3` (`ALTER TABLE user_profile ADD COLUMN photoUrl TEXT`). Esquema exportado en `app/schemas/…/3.json`.

## 5. Seguridad y privacidad

- Sin permisos nuevos (Photo Picker y cámara del sistema). Fila de `CAMERA` en `docs/permissions-matrix.md` actualizada: no se pide.
- El `FileProvider` solo comparte `cache/fotos/` y no se exporta (`exported=false`, `grantUriPermissions=true`).
- El token nunca va a S3: el cliente de subida no tiene `AuthHeader` ni `Authenticator`.
- La foto se recodifica sin metadatos en el teléfono (sin EXIF ni GPS) y otra vez en el backend.

## 6. Tests / Gates

- **Unit:** `test/.../feature/profile/ui/ProfilePhotoViewModelTest.kt` (4): publicada se guarda en el perfil; cancelar no sube; rechazada no toca el perfil; foto ilegible con su mensaje.
- **Data:** `test/.../integration/PhotoUploadIntegrationTest.kt` (6) con MockWebServer como API y como S3: los campos van antes del archivo y sin `Authorization`, y luego confirma; en revisión sin URL; ilegible no llama a la API; más de `max_bytes` no se sube; S3 rechaza y no confirma; 503 sin bucket.
- **UI/migración:** `androidTest/.../core/database/MigrationTest.kt` (+1): de 2 a 3 conserva el perfil con `photoUrl` nulo. Corre en la CI del PR.
- **Security:** `PlatformSecurityTest` (permisos sin cambios) y `SourceSecurityTest` siguen verdes; `ArchitectureTest` con el paquete `feature/photos`.

## 7. Plan de entrega

PR "feat: foto de perfil desde galería o cámara (nv-50)". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
