# NV-53 — Amigos: buscar, solicitudes, lista, perfil de la persona y privacidad

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Implementa EP-19 con la API ya publicada (backend nv-34, nv-35, nv-36, nv-53, nv-64 y nv-70). Rama `feat/amigos`, desde `main`.

## 1. Objetivo

Que la persona encuentre y agregue amigos, responda solicitudes, vea su lista y el perfil de cada quien, y decida cómo la encuentran (US-232, US-233, US-235, US-236, US-238). Además, los avisos y push sociales de nv-46/nv-52 ya abren el perfil de la persona.

## 2. Alcance / No-alcance

### Alcance
- **Perfil → Amigos (24_Amigos):** pestañas Amigos, Solicitudes (con número de pendientes) y Enviadas; filtro local sin acentos; aceptar o rechazar desde la fila; cancelar una enviada. Se recarga al volver.
- **Agregar amigos (26_AgregarAmigos):** buscar por nombre de usuario o teléfono (mínimo 3 caracteres); buscar por código de amigo (8 del alfabeto del backend, sin 0, 1, I, L ni O); sugerencias por amigos en común; mi nombre de usuario (elegirlo o cambiarlo; 409 = ocupado) y mi código de amigo, que se comparte con la hoja del sistema. Un 409 al agregar cuenta como "Solicitud enviada".
- **Perfil de la persona (25_PerfilAmigo):** foto, nombre, @usuario, relación; con amistad, nivel, amigos y planes en común. Acción según relación: Agregar, Cancelar solicitud, Aceptar/Rechazar, o Eliminar amigo y Bloquear (ambos con confirmación; bloquear cierra la pantalla).
- **Privacidad:** interruptor "Buscable por nombre de usuario" (`buscable_por_usuario`).
- **Push y avisos:** solicitud de amistad y amistad aceptada abren `PersonRoute(usuario_id)`.
- Room versión 4 (`MIGRATION_3_4`): `username`, `friendCode` y `priv_searchableByUser` (por defecto 1). Versión `versionCode 8`, `versionName 0.6.0`.

### No-alcance
- Dibujar y escanear el QR (US-234): sin librería de QR ni cámara en el stack; el código se comparte como texto y se escribe a mano.
- Buscar contactos del teléfono (`POST /personas/contactos`): requiere `READ_CONTACTS`.
- Silenciar a un amigo (`PUT /amistades/{id}/silencio`) e invitar amigos a un plan (US-237): llegan con planes grupales.

## 3. Contratos (API y navegación)

wherever-backend `docs/endpoints.md` (`origin/main`):

| Método | Ruta | Uso |
|---|---|---|
| GET | `/amistades?estado=amigos\|recibidas\|enviadas` | Listas y `pendientes` |
| GET | `/amistades/sugerencias` | Quizá los conoces |
| GET | `/personas/buscar?q=` | Usuario (prefijo) o teléfono |
| GET | `/personas/codigo/{codigo}` | Por código; 404 igual para todo |
| GET | `/personas/{id}` | Perfil con relación |
| POST | `/amistades {usuario_id}` | Agregar; 409 repetida |
| PATCH | `/amistades/{id} {accion}` | Aceptar o rechazar |
| DELETE | `/amistades/{id}` | Eliminar o cancelar |
| POST | `/amistades/{id}/bloquear` | Bloquear |
| PATCH | `/usuarios/yo {nombre_usuario}` | Con `If-Match`; 409 ocupado |
| PATCH | `/usuarios/yo/privacidad {buscable_por_usuario}` | Interruptor |

`GET /personas/{id}` no trae el id de la amistad: el perfil lo busca en la lista que toca según la relación.

Rutas nuevas en `docs/routes.md`: `FriendsRoute`, `AddFriendRoute` y `PersonRoute(id)`.

## 4. Modelo de datos

`UserProfile.username`, `UserProfile.friendCode` y `Privacy.searchableByUser`, guardados en Room (versión 4). `feature/friends` con `FriendsRepository`. `UiState`: `FriendsUiState`, `AddFriendUiState` y `PersonUiState`.

## 5. Seguridad y privacidad

- La API nunca devuelve correo ni teléfono de otra persona; la app tampoco los pide ni los muestra.
- Rechazar, eliminar y bloquear no avisan a la otra persona (backend nv-36); los textos lo dicen.
- El id del push se valida en `PushDestination.from` antes de abrir el perfil.
- Sin permisos nuevos.

## 6. Tests / Gates

- **Unit:** `test/.../feature/friends/ui/FriendsViewModelsTest.kt` (11): filtro sin acentos, aceptar y rechazar con falla, buscar y agregar, 409 como enviada, código inválido e inexistente, nombre de usuario normalizado y ocupado, reglas de usuario y código, perfil con confirmación de eliminar, bloquear, aceptar desde el perfil y 404.
- **Data:** `test/.../integration/FriendsIntegrationTest.kt` (5) con la cadena real.
- **UI:** `androidTest/.../core/database/MigrationTest.kt` (+1): 3 → 4 conserva el perfil y deja "buscable por usuario" encendido. Corre en la CI.
- **Security:** `SourceSecurityTest` y `ArchitectureTest` siguen verdes.

## 7. Plan de entrega

Commit directo a `main` a pedido de Jorge (2026-11-19). Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
