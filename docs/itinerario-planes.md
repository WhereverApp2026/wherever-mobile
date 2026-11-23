# Itinerario — Planes (reparar y completar)

> Responsable: Borre (`Borre4023`), desde el 2026-11-22. Revisor: Osmar. Reglas: `CONTRIBUTING.md`.
> Rama de este itinerario: `spec/planes-itinerario` (solo documentación).
> Specs: `nv-23`, `nv-24`, `nv-26`, `nv-28`, `nv-30`. Implementación previa: `nv-48`.

## Punto de partida (2026-11-22)

9 de 22 frames funcionan (41 %). Todo viene de `nv-48`, que cubrió en parte `nv-23`, `nv-24` y `nv-28`.

| Spec | Frames | Funcionan | Lo que falta |
|---|---|---|---|
| nv-23 Crear plan con IA | 6 | 4 | `17_AjustaPlan` completo (día, hora exacta, número de paradas) y el 4.º paso "¡Plan listo!" de `Generando *` (hoy hay 3 pasos) |
| nv-24 Detalle y edición | 5 | 3 | `CrearPlan` (plan a mano) y la pestaña Sugeridas de `41_AgregarParada`; en `39_Plan`, el mapa, Invitar amigos y Reservar todo |
| nv-28 Mis planes | 2 | 2 | Criterio "disponible sin red" (sin Room) |
| nv-26 Vaca virtual | 4 | 0 | Todo |
| nv-30 Día del plan | 5 | 0 | Todo |

Lo que falta de nv-23 y nv-24 se dedujo del código; la Fase 0 lo confirma frame por frame.

## Problemas que se arreglan antes de escribir código nuevo

1. **Specs sin conciliar.** `nv-23`, `nv-24` y `nv-28` dicen "sin código" y "Pendiente de backend", aunque `nv-48` las implementó en parte. `specs/README.md` marca los 18 frames de Planes como "Pendiente de backend".
2. **Specs con contrato viejo.** `nv-26` y `nv-30` dicen "Pendiente de backend", pero la API ya tiene Vaca (`docs/avance.md`, P10) y `/checkins` (lo usa nv-32).
3. **Room.** La constitución y las specs piden `PlanEntity` y `PlanStopEntity`; `nv-48` decidió "Sin cambios en Room" sin excepción documentada. Mis planes no funciona sin red, y el Día del plan lo necesita.
4. **Pruebas que piden las specs y no existen:** borrador que sobrevive sin red y `privacidad.aprendizaje_personalizado` (nv-23); 403 al editar un plan ajeno (nv-24); planes borrados de Room al cerrar sesión (nv-28).
5. **nv-48 sin firma** y sin la prueba "en el teléfono con catálogo cargado".

### Lo que aclaró el contrato (`endpoints.md`, 2026-11-22)

- **Vaca:** contrato completo (backend nv-58): gastos por grupo con `id_cliente` (permite encolar sin red), corte con deudas y `PATCH /vaca/deudas/{id}` para marcar pagado.
- **Check-in:** lo inicia la persona (`POST /checkins` con QR del mostrador o ubicación), no el negocio. La spec decía lo contrario. El lector QR ya está en el stack (ZXing, nv-20).
- **Sin red:** existe `GET /planes/{id}/paquete-offline` (plan, fichas y reservas de hoy o mañana). `nv-56` lo usa.
- **Rutas paso a paso:** no hay endpoint; `GET /geo/direcciones` está diferido "hasta elegir proveedor de mapas" (D25).
- **`POST /planes`:** acepta de 1 a 6 paradas; la maqueta pide de 2 a 4.
- **`POST /planes/generar`:** no recibe el número de paradas; acepta `fecha` de ahora a 30 días.
- **Sugeridas en `41_AgregarParada`:** no hay endpoint por plan; propuesta: `GET /recomendaciones` con la ubicación de la última parada.
- **Mis planes:** `GET /planes` no documenta el estado (Reservado, En grupo, Sin reservar) que pide la maqueta.
- **`aprendizaje_personalizado`:** lo aplica el servidor; la app no lo manda en `generar`. Se reformuló la prueba de nv-23.

## Fases

Cada fase es una rama desde `main` actualizado y un PR aprobado por Osmar. No se empieza una fase con su spec sin conciliar.

### Fase 0 — Specs al día (`spec/planes-itinerario`, solo `.md`)
- [x] Este itinerario y el reparto (`CONTRIBUTING.md`, `docs/pendientes.md`, `docs/equipo/`).
- [x] `nv-23`, `nv-24`, `nv-28`: marcado en `spec.md`/`plan.md`/`task.md`/`gates.md` lo que cumplió `nv-48`, con rutas reales de código y pruebas; endpoints reales en §3; lo que falta, abierto.
- [x] `nv-26`, `nv-30`: §3 con los endpoints reales de `wherever-backend/docs/endpoints.md` (copia del 2026-11-22).
- [x] `specs/README.md`: estado real de los 18 frames de Planes; D29 resuelta y D31 en curso. Las filas de otras áreas siguen viejas (fuera de este itinerario).
- [x] `docs/avance.md`: notas de qué falta en nv-23, nv-24, nv-26, nv-28 y nv-30.
- [ ] Decisiones 1 a 5 (ver "Decisiones") tomadas con Osmar en el PR.
- [x] Spec `nv-56` escrita en borrador (`specs/nv-56`), pendiente de Osmar junto con la decisión 1.
- [x] Compuertas: `check_specs.sh` (SPECS-OK), `check_sdd.sh`, `check_routes.sh`, `critic-guardrails.sh`.

### Fase 1 — Planes sin conexión (`feat/nv-56-planes-room`)
Spec: `specs/nv-56`. Sin red se lee; las ediciones se intentan y, si fallan, no cambian nada. No se encolan, porque la API pide `If-Match` y un 409 obliga a recargar.
- [ ] Antes: Issue a Jorge por `core/di/AppModule.kt` (registrar la migración), `app/build.gradle.kts` (versión) y `MainActivity.kt`.
- [ ] Tablas `plan`, `plan_tab_entry`, `plan_stop`, `plan_transfer` y `plan_draft`, `PlanDao`, `MIGRATION_5_6` y esquema `6.json` exportado.
- [ ] `PlansRepository` guarda lo que llega de la red y la UI lee de Room (Mis planes y detalle).
- [ ] Planes de hoy y mañana con `GET /planes/{id}/paquete-offline` (`ETag`, 30 por hora).
- [ ] Planes borrados al cerrar sesión (`sessionCleaners`).
- [ ] Pruebas: Data (lectura por pestaña, sin red), `MigrationTest` 5→6, Security (logout limpia).
- [ ] 89_SinConexion: "Disponible sin internet" con planes guardados.

Va primero porque las fases 2 a 5 escriben sobre el mismo repositorio. Si otra rama sube la versión de Room antes, se renumera la migración.

### Fase 2 — Detalle y edición completos (`feat/nv-24-plan`)
- [ ] `CrearPlan`: nombre (≤ 40), quién va, día, hora, presupuesto, paradas (1 a 4), Visible para mis amigos → `POST /planes`.
- [ ] `41_AgregarParada`: pestañas Sugeridas (`GET /recomendaciones`) y Buscar; el plan recalculado llega después de agregar, con Deshacer.
- [ ] Prueba Security: 403/404 al editar un plan ajeno no cambia nada en local.
- [ ] Rutas nuevas en `docs/routes.md`.

### Fase 3 — Crear con IA completo (`feat/nv-23-ia`)
- [ ] `17_AjustaPlan`: día y hora exactos (`fecha`, hasta 30 días); número de paradas dentro de la frase; "Crear plan a mano" → `CrearPlan`.
- [ ] `Generando *`: los 4 pasos de la maqueta (D29 resuelta: animación, no avance real del servidor).
- [ ] Pruebas: Cancelar con dispatcher de prueba; el borrador persiste en Room y sobrevive sin red; ni la frase ni los gustos en logs.

### Fase 4 — Vaca virtual (`feat/nv-26-vaca`)
- [ ] Requiere `46_Grupo` de nv-25 (Jorge) en `main`: la Vaca se abre desde el grupo.
- [ ] `28_Vaca`, variantes y `45_AgregarGasto`; montos en centavos enteros, nunca `Float`.
- [ ] Contrato: `GET /grupos/{id}/vaca`, `/grupos/{id}/gastos`, `PATCH /vaca/deudas/{id}` (backend nv-58).
- [ ] Unit: ejemplo de la maqueta ($760 entre 4 = $190; Jorge debe $70) y validaciones en centavos.
- [ ] Data: gasto encolado sin red y reenviado con el mismo `id_cliente`.

### Fase 5 — El día del plan (`feat/nv-30-dia`)
- [ ] Requiere la decisión D25, acordada con Jorge (`32_ExpMapa` usa la misma).
- [ ] `26_CheckinOk` primero: no depende del mapa. `POST /checkins` con el QR del mostrador (ZXing) o la ubicación; 409 y 422 con su mensaje.
- [ ] `13_Mapa`, `14_MapaFull`, `15_Llegar`, `18_Navegando` según D25.
- [ ] Security: sin histórico de ubicación en disco ni en logs.

## Decisiones que se toman en la Fase 0

| # | Decisión | Recomendación | Por qué |
|---|---|---|---|
| 1 | Room para planes | Sí, en `nv-56`, con `paquete-offline` | La constitución lo exige, nv-28 lo pide y nv-30 lo necesita sin red |
| 2 | D25 mapas | Abrir la app de mapas del teléfono hasta tener proveedor | Sin licencias ni librería nueva; el backend también difiere `/geo/direcciones` |
| 3 | D29 pasos de "Generando" | Resuelta: animación de 4 pasos; Cancelar solo descarta la respuesta | La API no publica avance ni cancelación |
| 4 | Paradas al editar o crear a mano | De 1 a 4 (la API acepta de 1 a 6) | Igual que `17_AjustaPlan`; nv-48 ya deja al menos 1 |
| 5 | Estado de cada plan en Mis planes | Pedir el campo al backend (Issue a Osmar); mientras, sin etiqueta | Cruzar reservas y grupos por plan cuesta varias llamadas por tarjeta |

## Lo que bloquea desde fuera

| Bloqueo | Afecta | Quién |
|---|---|---|
| Staging sin negocios (`POST /planes/generar` → 422) | Probar en el teléfono las fases 1 a 3 | Osmar (importar negocios) |
| Estado del plan en `GET /planes` (decisión 5) | Etiqueta de nv-28 | Osmar (backend) |
| `AppModule.kt`, `app/build.gradle.kts` y `MainActivity.kt` (código compartido) | Fase 1 | Jorge (Issue) |
| `46_Grupo` | Fase 4 | Jorge (nv-25) |
| D25 | Fase 5 | Borre y Jorge |
| Firma de nv-48 | Cierre de la Fase 0 | Osmar |

## Avance esperado

| Al cerrar | Frames | % de Planes |
|---|---|---|
| Hoy | 9 | 41 % |
| Fases 2 y 3 | 13 | 59 % |
| Fase 4 | 17 | 77 % |
| Fase 5 | 22 | 100 % |
