# NV-38 — Negocio: perfil, fotos y cobros

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 11 · Negocio. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Administrar datos, fotos, cobros y facturación del negocio.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como negocio quiero mantener mis datos y fotos.

- [ ] Reordeno fotos y elijo portada.
- [ ] Cada foto se revisa antes de publicarse.
- [ ] Puedo pausar Recibir reservaciones.

**HU propuesta.** Como negocio quiero ver y recibir mis cobros.

- [ ] Veo saldo, en espera y depósitos.
- [ ] Registro mi CLABE, verificada con un depósito.
- [ ] Guardo datos fiscales.

### UI

| Frame | Contenido |
|---|---|
| `58_NegPerfil` | Portada, Sello, datos y horario, fotos, cobros, Recibir reservaciones, Volver a Wherever, facturación, Cerrar sesión, Eliminar mi cuenta. |
| `87_NegGaleria` | Portada, galería de 6 de 12 con arrastrar, consejo, Guardar cambios. |
| `88_NegAgregarFoto` | Hoja: Tomar foto, Elegir de la galería, Importar de Instagram, Cancelar. |
| `57_NegCobros` | Disponible para depositar, en espera, depósitos, Descargar estado de cuenta. |
| `89_NegBanco` | Titular, banco, CLABE con validación, depósito de prueba, cuenta principal, Guardar cuenta. |
| `90_NegFiscal` | Razón social, RFC, C.P. fiscal, régimen, correo, constancia, factura mensual, Guardar datos. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto; avance de subida por foto. |
| Vacío | Sin depósitos: mensaje (sin frame). |
| Error | 422 bajo el campo. `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Datos guardados. |

### Validaciones

- CLABE de 18 dígitos con dígito verificador.
- RFC con formato válido.
- C.P. de 5 dígitos.
- Hasta 12 fotos, hasta 6 a la vez.

### Dudas abiertas

- **D27** Cuenta de negocio (`specs/README.md`)
- **D33** Datos fiscales y bancarios (`specs/README.md`)
- **D34** Subir fotos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → `NegPerfil` → `NegGaleria` → hoja `NegAgregarFoto`.
- `NegPerfil` → `NegCobros`, `NegBanco`, `NegFiscal`, `EliminarCuenta`.
- Volver a Wherever → `POST /usuarios/yo/modo` con `explorar` → `Inicio`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/usuarios/yo/modo` | JWT | modo: explorar | negocio | 200: modo_actual | 400, 401, 403, 422 |
| `POST` | `/api/v1/auth/logout` | JWT | — | 204 | 401, 503 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Datos del negocio y pausa de reservaciones.
- Galería y revisión de fotos.
- Importar de Instagram.
- Saldos, depósitos y estado de cuenta.
- Cuenta bancaria y datos fiscales.

## 4. Modelo de datos

Solo los últimos 4 dígitos de la cuenta en caché.

## 5. Seguridad y privacidad

- CLABE, RFC y constancia fuera de Room y logs; `FLAG_SECURE`.
- 403 si el rol no corresponde.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: CLABE y RFC.
- Security: sin datos fiscales en logs.
- UI: reordenar fotos.
- Data: la configuración del negocio se guarda en Room tras el PATCH y se lee sin red.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
