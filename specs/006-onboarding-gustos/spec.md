# 006 — Onboarding de gustos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 1 · Acceso y registro. **Sprint 1.** Implementada por encargo explícito (2026-11-04); ver "Implementado".

## 1. Objetivo

Conocer gustos, gasto habitual y restricciones para que la IA arme planes a la medida.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-020.** Como usuario nuevo quiero elegir mis gustos con chips para recibir planes que me interesen.

- [x] 15 chips con emoji.
- [x] Mínimo 3 para continuar.
- [x] Botón Omitir.
- [x] Se guardan en el perfil.

**US-021.** Como usuario quiero indicar cuánto suelo gastar por salida.

- [x] Deslizador de 0 a 1,000+ MXN, inicial 150 a 300.
- [x] Sin ubicación, pide ciudad con autocompletado.

**US-022.** Como usuario quiero indicar dieta, alergias y accesibilidad.

- [x] Chips opcionales.
- [x] Alergias abre un campo de texto.

### UI

| Frame | Contenido |
|---|---|
| `06_Gustos` | Paso 2 de 4, "¿Qué te gusta?". Secciones Se me antoja, Cómo me muevo, Presupuesto por persona ($, $$, $$$), Suelo salir. Continuar y Omitir por ahora. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador en Continuar. Autocompletado con indicador discreto. |
| Vacío | Autocompletado sin resultados: "No encontramos esa ciudad". |
| Error | 422: mensaje bajo la sección. 403 si el rol no es consumidor. Fallo al guardar: permite reintentar u omitir. |
| Sin red | Conserva las selecciones y reintenta con WorkManager; deja continuar. |
| Éxito | Pasa a `Permisos` (`specs/007`). |

### Validaciones

- `categorias`: de 3 a 10, sin repetir.
- `presupuesto_max` ≥ `presupuesto_min`, enteros en MXN.
- `restricciones`: hasta 10.
- `alergias`: hasta 200 caracteres.
- `q` de ciudad: 2 letras o más, con espera tras escribir.

### Dudas abiertas

- **D05** Gustos: maqueta, historia y API no coinciden (`specs/README.md`)
- **D12** Onboarding: paso de amigos y cómo saber si terminó (`specs/README.md`)
- **D19** Ciudad al registrarse (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Registro` → `Gustos` → `Permisos` → `Inicio`.
- Omitir → `Permisos`.
- Desde `Ajustes` → Tus gustos abre esta pantalla sin barra de pasos.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `PUT` | `/api/v1/usuarios/yo/gustos` | JWT consumidor | categorias[3..10], presupuesto_min, presupuesto_max, restricciones[..10], alergias(..200) | 200: los mismos campos | 400, 401, 403, 422 |
| `PATCH` | `/api/v1/usuarios/yo/preferencias` | JWT | movilidad_default, tema, unidades, horario_silencioso{desde,hasta}, notificaciones{reservas,cupones,grupos,resenas,promociones} (parcial) | 200: preferencias completas | 400, 401, 422 (notificaciones.reservas:false) |
| `GET` | `/api/v1/ciudades/autocompletar` | no | ?q= (2+ letras) &limite= (8, máx. 50) | 200: [id, nombre, estado] | 400, 422 |
| `PATCH` | `/api/v1/usuarios/yo` | JWT | Cabecera If-Match: "<version>". nombre, foto_url, ciudad_id (parcial) | 200: Perfil completo con versión nueva | 400, 401, 409, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

`UserProfileEntity.gustos` (`@Embedded`). Marca local de onboarding completo por usuario (DataStore).

## 5. Seguridad y privacidad

- Dieta y alergias son datos sensibles: fuera de los logs.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: reglas de mínimo y máximo de chips.
- Data: `PUT` reemplaza y actualiza Room.
- UI: Continuar deshabilitado con menos de 3.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md` y `task.md`.

## Implementado (2026-11-04)

**Hecho**

- Se siguen las historias, no la maqueta, donde difieren (D05): 15 chips con emoji y deslizador de presupuesto.
- Guarda en orden: `PUT /usuarios/yo/gustos`, `PATCH /usuarios/yo/preferencias` (`movilidad_default`) y `PATCH /usuarios/yo` (`ciudad_id`). Si una falla, se muestra el error y se puede reintentar.
- Máximo 10 categorías. Autocompletado de ciudad con espera de 300 ms y mínimo 2 letras.
- Omitir guarda una marca local por usuario (D12).
- También se abre desde Ajustes, con los datos actuales.

**Difiere de lo especificado**

- "Cómo me muevo" es de una sola opción, porque `movilidad_default` es único (D05).
- "Suelo salir" no está: no tiene campo en la API (D05).
- **Las claves de `categorias` y `restricciones` son propuestas:** la API no publica catálogo (D05). Staging aceptó `tacos`, `cafes` y `aire_libre`; las demás no se han enviado.

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/tastes/ui/TastesScreen.kt`
- `feature/tastes/ui/TastesViewModel.kt`
- `feature/profile/ui/CitySearch.kt`

**Pruebas**

- `feature/profile/ui/ProfileViewModelsTest.kt`

**Límites de la verificación**

- Probado el 2026-11-04 contra staging con una cuenta de prueba, en un teléfono Samsung SM-A346M con Android 16, en tema claro y oscuro. Verificado también con `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug`.
- Sin probar en dispositivo: elegir ciudad, porque el catálogo sigue vacío (D38), y las claves distintas de `tacos`, `cafes` y `aire_libre`.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Implementada y probada en un teléfono, sin revisor.
