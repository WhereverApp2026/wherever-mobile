# NV-43 — Consentimiento de datos sensibles para alergias

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Corrige `specs/nv-10` (Gustos, US-022) y amplía `specs/nv-15` (Privacidad). Rama `fix/datos-sensibles`.

## 1. Objetivo

Hoy, guardar Gustos con alergias da 422: el backend (nv-15 del backend) exige `privacidad.datos_sensibles = true` para guardar `alergias`, y la app nunca lo pide ni lo envía. Las alergias son un dato de salud, así que la app pide un consentimiento expreso con una casilla aparte, y el usuario puede retirarlo desde Privacidad.

Decisión de Jorge (2026-11-12): casilla en Gustos más interruptor en Privacidad.

## 2. Alcance / No-alcance

### Alcance
- **Gustos (`06_Gustos`, US-022).** Al activar el chip Alergias aparece, bajo el campo de texto, la casilla "Acepto que Wherever guarde mis alergias. Son un dato de salud y puedo retirar este permiso en Privacidad." (`tastes_allergies_consent`).
  - La casilla abre marcada si el perfil ya tiene `datos_sensibles = true`.
  - Con alergias escritas y la casilla sin marcar, Guardar/Continuar no envía nada y muestra bajo la casilla "Marca la casilla para guardar tus alergias" (`tastes_allergies_consent_required`).
  - Al guardar con alergias y sin consentimiento previo en el perfil: primero `PATCH /usuarios/yo/privacidad {datos_sensibles: true}` y después `PUT /usuarios/yo/gustos`. Si el `PATCH` falla, no se envían los gustos.
  - Sin alergias (chip apagado o campo vacío), la casilla no importa y no se toca `datos_sensibles`.
- **Privacidad (`28_PrivSocial`, nv-15).** Interruptor nuevo "Datos sensibles" con el detalle "Guardar tus alergias en Gustos" (`privacy_sensitive`, `privacy_sensitive_detail`).
  - Encenderlo manda `datos_sensibles: true`.
  - Apagarlo con alergias guardadas pide confirmación: "¿Retirar el permiso?" / "Se borrarán las alergias que guardaste en Tus gustos." / "Retirar" (`privacy_sensitive_off_*`). Sin alergias guardadas se apaga directo.
  - El backend borra las alergias al retirar el permiso; la app vuelve a pedir el perfil (`ProfileRepositoryImpl.applied`), y Room queda con `alergias` vacío.
- **Datos.** `datos_sensibles` en `PrivacyDto`, `PrivacyPatchDto`, `Privacy`, `PrivacyPatch` y `PrivacyEntity`. Room pasa a la versión 2 con la migración `1 → 2` (columna `priv_sensitiveData`, `0` por defecto).

### No-alcance
- Otros datos sensibles: hoy el backend solo condiciona `alergias` a este permiso.
- Texto legal del aviso de privacidad: lo define el equipo legal, no esta spec.

## 3. Contratos (API y navegación)

| Método | Ruta | Cambio |
|---|---|---|
| `PATCH` | `/api/v1/usuarios/yo/privacidad` | Acepta y devuelve `datos_sensibles` (booleano) |
| `GET` | `/api/v1/usuarios/yo` | `privacidad.datos_sensibles`; sin él, `gustos.alergias` llega vacío |
| `PUT` | `/api/v1/usuarios/yo/gustos` | `alergias` no vacío sin el permiso → 422 en `alergias` |

Fuente: `docs/contexto_api_sprint1.md` (cotejo del 2026-11-08). Sin rutas de navegación nuevas.

## 4. Modelo de datos

- `PrivacyEntity.sensitiveData: Boolean` con `@ColumnInfo(defaultValue = "0")`.
- `WhereverDatabase` versión 2; `MIGRATION_1_2`: `ALTER TABLE user_profile ADD COLUMN priv_sensitiveData INTEGER NOT NULL DEFAULT 0`. Esquema exportado en `app/schemas/.../2.json`. Sin `fallbackToDestructiveMigration`.
- `TastesUiState`: `allergiesConsent`, `consentSaved` y `consentError`. `PrivacyUiState`: `hasAllergies`, `confirmSensitiveOff`.

## 5. Seguridad y privacidad

- Consentimiento expreso para un dato de salud (LFPDPPP, datos personales sensibles): una casilla aparte, sin marcar por defecto, y la forma de retirarlo en Privacidad.
- Las alergias y `datos_sensibles` siguen fuera de los logs (`specs/nv-10` §5).
- La migración conserva los datos del perfil en caché.

## 6. Tests / Gates

- **Unit**: `test/.../feature/tastes/ui/TastesViewModelTest.kt`: sin casilla no se envía nada y aparece el error; con casilla, `PATCH` antes que `PUT`; con permiso previo no se repite el `PATCH`; si el `PATCH` falla no se envían los gustos; sin alergias no se toca el permiso; la casilla abre marcada con el permiso guardado. `test/.../feature/settings/ui/PrivacyViewModelTest.kt`: encender manda `true`; apagar con alergias pide confirmación y no envía hasta confirmar; apagar sin alergias envía directo; cancelar no envía.
- **Data**: `test/.../feature/profile/data/ProfileRepositoryImplTest.kt`: `datos_sensibles` se lee del perfil, se envía en el `PATCH` y queda en Room.
- **Migración**: `androidTest/.../core/database/MigrationTest.kt` con `MigrationTestHelper`: un perfil de la versión 1 sobrevive a `1 → 2` con `priv_sensitiveData = 0`.
- **Security**: sin superficie nueva; `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes.

## 7. Plan de entrega

PR "fix: consentimiento de datos sensibles para alergias". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
