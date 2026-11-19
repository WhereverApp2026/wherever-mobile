# plan.md — NV-51 — Cuenta: contraseña, correo y mis datos

> Deriva de `specs/nv-51/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/account/domain/AccountRepository.kt`
- `feature/account/data/{AccountApi,AccountRepositoryImpl}.kt` y `data/di/AccountModule.kt`
- `feature/account/ui/{ChangePassword,ChangeEmail,MyData}{Screen,ViewModel}.kt`
- `feature/settings/ui/SettingsScreen.kt` (filas Correo y Contraseña) y `PrivacyScreen.kt` (fila Mis datos)
- `navigation/{Routes,WhereverNavHost}.kt`, `docs/routes.md`, `res/values/strings.xml`
- `app/build.gradle.kts`: versión 6 (0.4.0)

## 2. Guardarraíles
- Repositorio aparte de `AuthRepository` y `ProfileRepository` (detekt `TooManyFunctions`).
- La cadena de interceptores no cambia.

## 3. Pruebas
### Unitarias
- `AccountViewModelsTest` (11)

### Integración
- `AccountIntegrationTest` (6)

### UI
- Sin pruebas nuevas

### Seguridad
- `ArchitectureTest`, `SourceSecurityTest`

## 4. Secuencia
1. Contrato del backend (`origin/main`).
2. Datos, ViewModels y pantallas; pruebas.
3. Compuertas por partes; push a `main`.

## 5. Riesgos
- Staging no manda correos (P6): el enlace de verificación y el de la copia no llegan todavía, aunque la API responda bien.
