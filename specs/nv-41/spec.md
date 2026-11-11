# NV-41 — Identificador de la app y build de release para Play Console

> Rector: `.specify/memory/constitution.md` (§8: release manual, sin CD). Plantilla: `.specify/templates/spec-template.md`. Rama `chore/application-id`.
> **No publica nada.** Deja listo y documentado lo que Play Console fija para siempre (el identificador) y lo que la build de release necesita.

## 1. Objetivo

Confirmar el nombre del paquete con el que se subirá la app a Google Play y dejar la build de release firmable sin keystore en el repositorio, con R8 sin romper Retrofit ni kotlinx.serialization.

## 2. Alcance / No-alcance

### Estado verificado (2026-11-11)

| Dato | Dónde | Valor |
|---|---|---|
| `namespace` | `app/build.gradle.kts` | `com.wherever.mobile` |
| `applicationId` | `app/build.gradle.kts` | `com.wherever.mobile` |
| Atributo `package` | `app/src/main/AndroidManifest.xml` | **no existe** (correcto) |
| `versionCode` / `versionName` | `app/build.gradle.kts` | `1` / `0.1.0` |
| Firma de release | `app/build.gradle.kts` | antes: ninguna; ahora: por variables de entorno (§5) |
| `isMinifyEnabled` / `isShrinkResources` | release | `true` / `true` |

### Por qué el paquete va en Gradle y no en el manifiesto

- Play Console identifica la app por su **`applicationId`**. Una vez subida la primera versión, **no se puede cambiar**: otro `applicationId` es otra app, con otra ficha, sin reseñas ni usuarios.
- `namespace` es el paquete de las clases generadas (`R`, `BuildConfig`) y no lo ve Play. Se mantiene igual al `applicationId` para no confundir, aunque podrían ser distintos.
- Con AGP 8 o posterior, el atributo `package` del manifiesto ya no se usa: el build falla si aparece (`Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported`). El paquete vive solo en Gradle.
- **Decisión:** `com.wherever.mobile` ya es el valor en ambos. **No se cambia**; esta spec lo confirma y lo documenta. El sufijo `.qa` que se usó el 2026-11-04 vivía en un init script fuera del repo y no es parte del build.

### Qué cambia

- `signingConfigs.release` lee `WHEREVER_KEYSTORE_PATH`, `WHEREVER_KEYSTORE_PASSWORD`, `WHEREVER_KEY_ALIAS` y `WHEREVER_KEY_PASSWORD` del entorno. Sin ellas, `assembleRelease` genera un APK **sin firmar** (no se cae el build ni CI). Con ellas, firma.
- `.gitignore` ya ignoraba `*.jks` y `*.keystore`; la prueba lo fija.
- `proguard-rules.pro` documenta por qué no hace falta ninguna regla propia hoy (ver §5) y conserva los números de línea para leer los fallos.

### No-alcance

- Subir a Play Console, crear la ficha, Play App Signing, AAB (`bundleRelease`), CD o firma en CI: spec posterior (constitución §8).
- URL de producción: `release` usa la de staging (`specs/nv-40`).

## 3. Contratos (API y navegación)

Sin cambios de API ni de rutas.

## 4. Modelo de datos

Sin cambios.

## 5. Seguridad y privacidad

- **Sin keystore ni contraseñas en el repositorio** (constitución §6): solo variables de entorno; `.gitignore` ya bloquea `*.jks` y `*.keystore`; `ReleaseConfigTest` lo comprueba.
- R8: Retrofit 3, OkHttp 5, kotlinx.serialization 1.12 y Hilt traen sus reglas de consumidor en el AAR. Los DTO son `@Serializable` y el plugin genera los `serializer()` que esas reglas conservan. Por eso `proguard-rules.pro` no lleva reglas propias. Verificado el 2026-11-11: `assembleRelease` compila y R8 termina; R8 (AGP 9.0.1) avisa que no lee el metadata de Kotlin 2.4.21, así que **falta** probar la app de release contra staging en un teléfono (`task.md` T2) antes de confiar en la build minificada.
- `release` no es depurable (`isDebuggable` por defecto) y no registra tráfico (`BuildConfig.DEBUG` falso).

## 6. Tests / Gates

- **Unit**: `ReleaseConfigTest`: `applicationId` y `namespace` = `com.wherever.mobile`, `versionCode` ≥ 1, sin `package` en el manifiesto.
- **Data**: sin capa de datos nueva. La integración con MockWebServer existente (`AuthIntegrationTest`, `ConnectionIntegrationTest`) corre sobre los mismos DTO que R8 conserva; la build minificada aún no se prueba contra staging (`task.md` T2, pendiente: sin dispositivo).
- **Security**: `ReleaseConfigTest`: firma solo por variables de entorno, sin contraseñas en `build.gradle.kts`, `*.jks`/`*.keystore` ignorados y ningún keystore versionado; `release` minificado.

## 7. Plan de entrega

PR "chore: identificador de la app y build de release". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
