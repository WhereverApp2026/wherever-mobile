# 001 — Flujo general

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`.
> Fuente: `maqueta/figma-frames/` (revisados los 162 frames) y `docs/contexto_api_sprint1.md`. Índice y dudas: `specs/README.md`.

## 1. Objetivo

Describir cómo se conectan las pantallas: el mapa de navegación y los flujos de usuario y de negocio. Cada pantalla se detalla en su propia spec (`002` a `034`).

## 2. Alcance / No-alcance

**In:** mapa completo de la maqueta, flujos principales y reglas transversales.

**Out:** detalle por pantalla y endpoints, que están en cada spec.

### 2.1 Secciones de la maqueta

Las anotaciones `label · …` agrupan los frames en 11 secciones:

| # | Sección | Specs |
|---|---|---|
| 1 | Acceso y registro | 002, 003, 004, 005, 006, 007 |
| 2 | Inicio, feed y notificaciones | 013, 014, 015 |
| 3 | Amigos | 016 |
| 4 | Explorar y lugares | 017, 018 |
| 5 | Crear plan con IA | 019, 020 |
| 6 | Compartir, grupos y chats | 021, 022 |
| 7 | Reserva, pago y mis planes | 023, 024, 025 |
| 8 | El día del plan | 026 |
| 9 | Recuerdos y reseñas | 027, 028 |
| 10 | Perfil, ajustes y estados | 009, 010, 011, 012, 029 |
| 11 | Negocio | 030, 031, 032, 033, 034 |
| — | Sin pantalla: sesión y tokens | 008 |

### 2.2 Flujo de usuario

1. **Arranque.** El splash decide: Bienvenida, Inicio, Login o Sin conexión.
2. **Alta.** Registro → gustos → encuentra amigos → permisos → Inicio. Son 4 pasos en la maqueta (ver D12 para el Sprint 1).
3. **Descubrir.** Inicio muestra lugares cercanos y actividad de amigos. Explorar filtra por lista o mapa.
4. **Armar.** El botón + abre: plan con IA, plan a mano, planear con amigos, compartir un momento o reseñar. La IA recibe una palabra y genera el plan en 4 pasos visibles.
5. **Ajustar.** En el plan se cambian, quitan y agregan paradas. Se comparte o se invita.
6. **Decidir en grupo.** Chat, votación del lugar, punto medio y Vaca virtual, que solo calcula: Wherever no mueve dinero.
7. **Reservar.** Elegir hora, cupón automático, pagar anticipo en la pasarela y recibir código y QR. Se puede cambiar la hora o cancelar.
8. **El día del plan.** Mapa, cómo llegar, navegación y check-in con QR escaneado por el negocio.
9. **Después.** Postal, reseña verificada, publicación en el feed y recuerdos. "¿Repetimos?" vuelve a empezar el ciclo.

### 2.3 Flujo de negocio

1. **Alta en 5 pasos.** Cuenta, negocio, ubicación, operación, fotos y verificación. Se puede guardar y salir.
2. **Revisión.** Wherever revisa en 24 a 48 horas. Mientras tanto ya se entra al panel.
3. **Operación diaria.** Panel con reservas, anticipos y ocupación. Reservaciones por día. Check-in por código o QR.
4. **Reputación.** Responder reseñas, cuidar el Sello y agradecer los momentos públicos de clientes.
5. **Atraer clientes.** Cupones y cupo por hora para llenar las horas tranquilas.
6. **Administrar.** Perfil, galería, cobros semanales, cuenta bancaria y datos fiscales. "Volver a Wherever" regresa al modo consumidor (`POST /usuarios/yo/modo`).

## 3. Contratos (API y navegación)

**[Decisión propuesta]** Cuatro grafos anidados (`specs/000-arquitectura §2.4`). Las rutas se registran en `docs/routes.md` al implementar cada spec. Barra inferior según D22.

### 3.1 Vista general

```mermaid
flowchart TD
  START(["Abrir la app"]) --> GATE{"SessionGate"}
  GATE -->|"sin tokens"| AUTH["Grafo auth"]
  GATE -->|"sesión vencida"| AUTH
  GATE -->|"sin red"| OFF["Sin conexión"]
  GATE -->|"sesión y modo explorar"| CONSUMER["Grafo consumer"]
  GATE -->|"sesión y modo negocio"| BUSINESS["Grafo business"]
  GATE -->|"eliminación pendiente"| DEL["Eliminación pendiente"]
  OFF -->|"Reintentar"| GATE
  AUTH -->|"registro"| ONB["Grafo onboarding"]
  AUTH -->|"login o restablecer"| CONSUMER
  ONB --> CONSUMER
  CONSUMER -->|"Tengo un negocio"| BUSINESS
  BUSINESS -->|"Volver a Wherever"| CONSUMER
  CONSUMER -->|"Cerrar sesión o 401 definitivo"| AUTH
  BUSINESS -->|"Cerrar sesión o 401 definitivo"| AUTH
  DEL -->|"Cancelar eliminación"| CONSUMER
```

### 3.2 Acceso y onboarding

```mermaid
flowchart LR
  B["01_Bienvenida"] -->|"Crear cuenta"| R["05_Registro"]
  B -->|"Ya tengo cuenta"| L["03_Login"]
  L -->|"Crea una"| R
  L -->|"Olvidé mi contraseña"| RC["03_Recuperar"]
  RC --> RV["04_RevisaCorreo"]
  RV -->|"Usar otro correo"| RC
  LINK(["Enlace del correo"]) --> NC["NuevaContrasena"]
  NC --> I["10_Inicio"]
  L --> I
  R --> G["06_Gustos"]
  G --> A["07_EncuentraAmigos"]
  A --> P["08_Permisos"]
  P --> S["06_PermisoSys"]
  S --> I
  P -->|"Ahora no"| I
```

### 3.3 Consumidor

```mermaid
flowchart TD
  subgraph TABS["Barra inferior"]
    I["10_Inicio / 11_InicioAmigos / 12_InicioMas"]
    E["30_Explorar"]
    PL["24_MisPlanes"]
    PF["81_Perfil"]
    C["15_Crear (botón +)"]
  end

  I --> N["18_Notif"]
  I --> BU["19_Buscar"]
  BU --> BP["20_BuscarPersonas"]
  BU --> SR["21_SinResultados"]
  I --> H["16_Historia"]
  I --> PD["17_PostDetalle"]
  I --> RE["13_Reacciones"]
  I --> PA["29_PlanAmigo"]
  PA --> MA["14_MeApunto"]
  I --> AL["22_AmigosLista"]
  AL --> AS["23_AmigosSolic"]
  AL --> AA["24_AgregarAmigo"]
  AA --> MC["25_MiCodigo"]
  AL --> PM["26_PerfilAmigo"]
  PM --> AO["27_AmigoOpc"]
  PM --> CA["ChatAmigo"]
  CA --> IP["InvitarPlan"]

  E --> F["31_Filtros"]
  E --> EM["32_ExpMapa"]
  E --> D["33_Detalle"]
  EM --> D
  D --> SE["34_Sello"]
  D --> RL["80_ResLugar"]
  D --> RV["22_Reserva"]

  C --> IA["IAPalabra"]
  C --> CP["CrearPlan"]
  C --> CG["43_CrearGrupo"]
  C --> CPU["70_CrearPub"]
  C --> RP["77_ResPend"]
  IA --> AJ["17_AjustaPlan"]
  IA --> GE["Generando 1 a 4"]
  AJ --> GE
  GE --> PLA["39_Plan"]
  CP --> PLA

  PLA --> CAM["40_Cambiar"]
  PLA --> AP["41_AgregarParada"]
  PLA --> CO["42_Compartir"]
  PLA --> RV
  PLA --> MF["14_MapaFull"]
  PLA --> LL["15_Llegar"]

  PL --> PV["41_PlanesVacio"]
  PL --> PLA
  PL --> MG["44_MisGrupos"]
  MG --> GC["45_GrupoChat"]
  GC --> GR["46_Grupo"]
  GC --> VO["48_Votar"]
  GC --> PLA
  GR --> PU["47_PuntoMedio"]
  GR --> VA["28_Vaca"]
  VA --> AG["45_AgregarGasto"]

  RV --> PS["35_Pasarela"]
  PS --> OK["23_Confirmacion"]
  PS --> ER["36_PagoError"]
  ER --> PS
  OK --> DR["26_DetReserva"]
  DR --> CN["27_Cancelar"]
  DR --> CH["38_CambiarHora"]
  DR --> LL

  MF --> LL
  LL --> NV["18_Navegando"]
  M13["13_Mapa"] --> LL
  NV --> CK["26_CheckinOk"]
  CK --> CM["71_Camara"]
  CK --> LL
  CM --> PO["72_Postal"]
  PO --> AB["73_Album"]
  AB --> RCP["74_RecPlan"]
  AB --> PVE["75_PostalVer"]
  AB --> RCV["76_RecVacio"]

  RP --> PVI["68_PostVisita"]
  PVI --> RC["78_ResCom"]
  PVI --> PB["69_Publicado"]

  PF --> EP["82_EditarPerfil"]
  PF --> IN["83_Insignias"]
  PF --> AJU["85_Ajustes"]
  PF --> MR["25_MisReservas"]
  PF --> FA["35_Favoritos"]
  PF --> AB
  PF --> CU["29_Cupones"]
  PF --> MRE["79_MisRes"]
  FA --> FV["36_FavVacio"]
  MR --> DR
  CU --> DC["47_DetCupon"]
  DC --> RV
  AJU --> DP["84_DatosPers"]
  AJU --> PR["28_PrivSocial"]
  AJU --> AY["86_Ayuda"]
  DP --> EL["EliminarCuenta"]
```

### 3.4 Negocio

```mermaid
flowchart TD
  AJ["85_Ajustes: Tengo un negocio"] --> N1["42_NegCuenta"]
  N1 --> N2["43_NegRegistro"]
  N2 --> N3["44_NegUbic"]
  N3 --> N4["45_NegOper"]
  N4 --> N5["46_NegVerif"]
  N5 --> N6["47_NegListo"]
  N6 --> PA["48_NegPanel"]
  N6 -->|"Editar mi solicitud"| N2

  subgraph BAR["Barra de negocio"]
    PA
    RE["49_NegReservas"]
    CU["53_NegCupones"]
    RS["51_NegResenas"]
    PE["58_NegPerfil"]
  end

  PA --> ST["56_NegStats"]
  RE --> CK["50_NegCheckin"]
  RS --> RP["52_NegResp"]
  RS --> MO["NegMomentos"]
  CU --> CR["54_NegCrear"]
  PE --> HO["55_NegHoras"]
  PE --> GA["87_NegGaleria"]
  GA --> AF["88_NegAgregarFoto"]
  PE --> CO["57_NegCobros"]
  CO --> BA["89_NegBanco"]
  PE --> FI["90_NegFiscal"]
  PE -->|"Volver a Wherever"| CONS(["10_Inicio"])
```

**Relación entre ambos lados:** una reserva del consumidor aparece en `49_NegReservas`. El QR de `26_DetReserva` se escanea en `50_NegCheckin`, lo que produce `26_CheckinOk` y habilita la reseña verificada. Una reseña (`68_PostVisita`) llega a `51_NegResenas`, y su respuesta (`52_NegResp`) se ve en `79_MisRes`. Un cupón de `54_NegCrear` aparece en `29_Cupones`.

### 3.5 Destinos ajenos a la navegación normal

| Origen | Destino |
|---|---|
| Enlace de restablecer (`#token=`) | `NuevaContrasena` |
| Enlace de verificar correo | Confirmación (sin frame) |
| Enlace de exportación | Descarga de datos (sin frame) |
| Enlace de plan (`42_Compartir`) | `29_PlanAmigo` |
| Push (`87_PushBloqueo`, `88_PushBanner`) | Reservación, votación, reseña o `15_Llegar` |

Si la sesión venció, pasan primero por Login y retoman el destino (US-013).

## 4. Modelo de datos

Sin modelo propio. Ver cada spec.

## 5. Seguridad y privacidad

- Rutas sin parámetros sensibles; los tokens de enlaces no entran en la navegación (`specs/000-arquitectura §2.4`).
- Los grafos no protegen datos por sí solos: el rol se valida en el servidor (403).

## 6. Tests / Gates

- UI: `SessionGate` lleva a cada destino de §3.1.
- `scripts/check_routes.sh` cuando exista (`nv-01`), con las rutas de `docs/routes.md`.

## 7. Plan de entrega

Documento de referencia. Se actualiza cuando una spec cambie la navegación. Orden sugerido para el Sprint 1: `008`, `002`, `003`, `005`, `004`, `006`, `012`, `009`, `010`, `011`.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
