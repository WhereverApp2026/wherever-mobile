# 010 — Perfil, edición e insignias

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Ver y editar la identidad de la cuenta, con su nivel y logros.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-017 (Should).** Como usuario quiero editar mi perfil para mantenerlo al día.

- [ ] Los cambios se guardan al instante.
- [ ] Cambiar correo pide verificación.
- [x] Tema Claro, Oscuro o Automático.

**HU propuesta.** Como usuario quiero ver mi nivel y mis insignias para motivarme.

- [x] Muestra `nivel`, `puntos` y `racha_semanas`.
- [ ] Las insignias salen del backend cuando exista.

### UI

| Frame | Contenido |
|---|---|
| `81_Perfil` | Avatar, nombre, @usuario, ciudad, bio, Editar perfil, compartir, contadores, insignias, accesos (Reservas, Favoritos, Recuerdos, Cupones), Mis reseñas, pestañas Publicaciones, Planes, Guardados. |
| `82_EditarPerfil` | Cancelar, Listo, foto, Nombre, Usuario, Bio, Mis gustos, Privacidad social. |
| `83_Insignias` | Tarjeta de nivel con puntos y progreso, Ganadas, Por desbloquear. |
| `84_DatosPers` | Foto, Nombre, Correo con Verificado, Teléfono opcional, Ciudad, Eliminar mi cuenta, Guardar cambios. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Datos de Room al instante; actualiza detrás. Sin caché: esqueleto. |
| Vacío | Sin publicaciones: mensaje e invitación a publicar. |
| Error | 409 en `PATCH`: "Tus datos cambiaron en otro dispositivo" y recarga. 409 en teléfono: ya pertenece a otra cuenta. 422 bajo el campo. |
| Sin red | Lectura desde Room; edición deshabilitada con aviso. |
| Éxito | Room actualizado con la `version` nueva. |

### Validaciones

- Nombre de 2 a 40 caracteres.
- `foto_url` empieza con https://.
- `PATCH` lleva `If-Match` con la `version` guardada.
- Teléfono internacional, con lada; se quitan espacios y signos.
- Cambiar correo pide contraseña.

### Dudas abiertas

- **D07** Funciones de API sin pantalla (`specs/README.md`)
- **D08** Perfil: campos que la API no tiene (`specs/README.md`)
- **D09** Guardar al instante o con botón (`specs/README.md`)
- **D34** Subir fotos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → `Perfil`.
- `Perfil` → `EditarPerfil`, `Insignias`, `Ajustes`, `Notificaciones`, `MisReservas`, `Favoritos`, `Recuerdos`, `Cupones`, `MisResenas`.
- `DatosPersonales` → `EliminarCuenta` (`specs/011`).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `GET` | `/api/v1/usuarios/yo` | JWT | — | 200: Perfil completo: id, nombre, correo, correo_verificado, foto_url, codigo_amigo, roles[], modo_actual, ciudad{id,nombre}|null, gustos{...}|null, preferencias{...}, privacidad{...}, telefono_registrado, puntos, nivel, racha_semanas, estado, eliminar_en, version, creado_en, actualizado_en. Cabecera ETag | 401 |
| `PATCH` | `/api/v1/usuarios/yo` | JWT | Cabecera If-Match: "<version>". nombre, foto_url, ciudad_id (parcial) | 200: Perfil completo con versión nueva | 400, 401, 409, 422 |
| `PUT` | `/api/v1/usuarios/yo/correo` | JWT | correo_nuevo, password | 200: Perfil completo | 400, 401, 409, 422 |
| `PUT` | `/api/v1/usuarios/yo/telefono` | JWT | telefono (internacional) | 200: telefono_registrado | 400, 401, 409, 422 |
| `GET` | `/api/v1/ciudades/autocompletar` | no | ?q= (2+ letras) &limite= (8, máx. 50) | 200: [id, nombre, estado] | 400, 422 |
| `PATCH` | `/api/v1/usuarios/yo/preferencias` | JWT | movilidad_default, tema, unidades, horario_silencioso{desde,hasta}, notificaciones{reservas,cupones,grupos,resenas,promociones} (parcial) | 200: preferencias completas | 400, 401, 422 (notificaciones.reservas:false) |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Insignias ganadas y por desbloquear.
- Puntos para el siguiente nivel.
- Contadores de planes, amigos y reseñas.
- Campos `usuario` y `bio`.
- Subida de foto.

## 4. Modelo de datos

`UserProfileEntity` con `version`. `ETag` guardado como `version`.

## 5. Seguridad y privacidad

- Teléfono nunca en Room ni en logs: solo `telefono_registrado`.
- Correo y nombre se redactan de los logs.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Data: `If-Match` correcto y 409 con recarga.
- Unit: `EditProfileViewModel`.
- UI: tema aplicado.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md` y `task.md`.

## Implementado (2026-11-04)

**Hecho**

- Perfil lee de Room y actualiza con `GET /usuarios/yo`. Sin red conserva la caché y muestra el error.
- Editar perfil une `82_EditarPerfil` y `84_DatosPers` (D09): nombre y ciudad, con `PATCH /usuarios/yo` e `If-Match`.
- Ante 409 avisa "Tus datos cambiaron en otro dispositivo", recarga y deja al usuario revisar.
- Aviso de correo sin verificar, que lleva a Revisa tu correo (D20).
- Room: `UserProfileEntity` con esquema exportado en `app/schemas`.

**Difiere de lo especificado**

- Guarda con botón, no al instante (D09).
- Sin `@usuario`, bio, contadores de planes, amigos ni reseñas, ni teléfono: la API no los da (D08).
- Sin foto: la API no permite subirla (D34). Se muestra la inicial.
- Sin `83_Insignias`: faltan datos (se muestran `puntos`, `nivel` y `racha`).
- Sin cambio de correo: no estaba pedido.

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/profile/ui/ProfileScreen.kt`
- `feature/profile/ui/ProfileViewModel.kt`
- `feature/profile/ui/EditProfileScreen.kt`
- `feature/profile/ui/EditProfileViewModel.kt`
- `feature/profile/data/*`

**Pruebas**

- `feature/profile/ui/ProfileViewModelsTest.kt`
- `feature/profile/data/ProfileRepositoryImplTest.kt`
- `androidTest/.../UserProfileDaoTest.kt` (Room real; sin correr, requiere emulador)

**Límites de la verificación**

- No probado en dispositivo ni contra staging con una cuenta real: no hay emulador configurado. Verificado con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada, sin revisor (falta prueba en dispositivo).
