# Avance de Wherever Móvil: pantallas que funcionan

> **Métrica (desde 2026-11-19):** pantallas que funcionan de verdad / 162 frames de la maqueta (`specs/README.md`).
> Una pantalla cuenta si tiene código, llama a la API real, maneja carga, error y vacío, tiene pruebas y se probó en el teléfono.
> "Con datos locales" = UI completa con repositorio falso detrás de una interfaz porque falta el endpoint (`docs/revision-jorge.md`); se cuentan aparte y **no** suman al % real.

## Corte actual (main)

| | Pantallas | % de 162 |
|---|---|---|
| Funcionan con la API real | 62 | **38,3 %** |
| Con datos locales (falta endpoint) | 1 | 0,6 % |
| Total con UI terminada | 63 | 38,9 % |

| Spec | Tema | Frames | Funcionan | Datos locales | Nota |
|---|---|---|---|---|---|
| nv-06 | Splash y bienvenida | 1 | 1 | 0 |  |
| nv-07 | Iniciar sesión | 1 | 1 | 0 |  |
| nv-08 | Recuperar contraseña | 3 | 3 | 0 | Nueva contraseña por enlace; sin token real (staging sin SMTP) |
| nv-09 | Registro | 1 | 1 | 0 | probado en teléfono 2026-11-19 |
| nv-10 | Gustos | 1 | 1 | 0 | probado en teléfono 2026-11-19 |
| nv-11 | Permisos en contexto | 2 | 2 | 0 | 08_Permisos probado en teléfono (Android 12) 2026-11-20; diálogo de avisos de Android 13+ sin probar |
| nv-13 | Ajustes y cerrar sesión | 1 | 1 | 0 |  |
| nv-14 | Perfil, edición e insignias | 4 | 3 | 1 | Insignias con catálogo local (sin endpoint) |
| nv-15 | Privacidad, mis datos, eliminar | 2 | 2 | 0 |  |
| nv-16 | Sin conexión | 1 | 1 | 0 | más mantenimiento y versión mínima; probado en teléfono 2026-11-20 |
| nv-17 | Inicio y feed | 15 | 7 | 0 | Amigos y PostDetalle probados en teléfono 2026-11-20 con datos de staging; PlanAmigo y MeApunto sin probar en teléfono (ningún momento de staging enlaza un plan); sin API: historias, reacciones, comentarios |
| nv-18 | Notificaciones | 4 | 4 | 0 | aviso en la app, bloqueo y Aceptar; pushes de reserva/reseña pendientes de backend |
| nv-19 | Búsqueda | 4 | 4 | 0 | Personas: Amigos, Agregar o Solicitud enviada probado en teléfono 2026-11-20 |
| nv-20 | Amigos | 15 | 11 | 0 | 27_AmigoOpc (silenciar en staging) probado en teléfono 2026-11-20; falta QR (librería por acordar), 07_EncuentraAmigos (contactos) y variantes |
| nv-21 | Explorar y lugares | 8 | 5 | 0 | staging sin negocios |
| nv-22 | Favoritos | 2 | 2 | 0 |  |
| nv-23 | Crear plan con IA | 6 | 4 | 0 | nv-48; falta `17_AjustaPlan` y el 4.º paso de `Generando` (spec conciliada 2026-11-22) |
| nv-24 | Plan: detalle y edición | 5 | 3 | 0 | nv-48; faltan `CrearPlan` y `41_AgregarParada` completa (spec conciliada 2026-11-22) |
| nv-25 | Compartir, grupos y chats | 12 | 2 | 2 |  |
| nv-26 | Vaca virtual y gastos | 4 | 0 | 0 | API lista (backend nv-58); espera `46_Grupo` |
| nv-27 | Reserva y pago | 10 | 4 | 0 | sin pago; staging sin negocios |
| nv-28 | Mis planes | 2 | 2 | 0 | nv-48; faltan etiqueta de estado y uso sin red (`nv-56`) |
| nv-29 | Cupones | 5 | 0 | 0 |  |
| nv-30 | El día del plan | 5 | 0 | 0 | check-in con API lista; mapa y rutas esperan D25 |
| nv-31 | Recuerdos y postales | 13 | 13 | 13 |  |
| nv-32 | Reseñas | 8 | 8 | 8 |  |
| nv-33 | Ayuda | 1 | 1 | 1 |  |
| nv-34 | Negocio: registro | 6 | 0 | 0 |  |
| nv-35 | Negocio: panel, reservas y check-in | 8 | 0 | 0 |  |
| nv-36 | Negocio: reseñas y momentos | 3 | 0 | 0 |  |
| nv-37 | Negocio: cupones y cupo | 3 | 0 | 0 |  |
| nv-38 | Negocio: perfil, fotos y cobros | 6 | 0 | 0 |  |

## Registro de cortes

| Fecha | Commit | Área cerrada | Funcionan | % real |
|---|---|---|---|---|
| 2026-11-19 | `30305af` | Punto de partida (nv-54 integrado) | 52 | 32,1 % |
| 2026-11-19 | `c528045` | Perfil: Insignias (datos locales) | 52 (+1 locales) | 32,1 % |
| 2026-11-19 | después de `c528045` | Recuperar contraseña: Nueva contraseña y enlaces del correo | 53 (+1 locales) | 32,7 % |
| 2026-11-19 | después de `7f8866f` | Notificaciones: aviso en la app, bloqueo y Aceptar | 54 (+1 locales) | 33,3 % |
| 2026-11-19 | después de `76718f1` | Búsqueda unificada integrada (las 3 ya contadas pasan a tener su pantalla propia) | 54 (+1 locales) | 33,3 % |
| 2026-11-20 | nv-16 integrado | 89_SinConexion, mantenimiento y versión mínima | 55 (+1 locales) | 34,0 % |
| 2026-11-20 | `099142e` + prueba | 08_Permisos probado en teléfono | 56 (+1 locales) | 34,6 % |
| 2026-11-20 | nv-19 Personas | BuscarPersonas · solicitud enviada (y Amigos) | 57 (+1 locales) | 35,2 % |
| 2026-11-20 | nv-17 feed de amigos | 11_InicioAmigos, 12_InicioMas, InicioMas · solicitud enviada, 17_PostDetalle | 61 (+1 locales) | 37,7 % |
| 2026-11-20 | nv-20 opciones de amigo | 27_AmigoOpc: Silenciar, Eliminar, Bloquear | 62 (+1 locales) | 38,3 % |
| 2026-11-23 | #68 mapa | 32_ExpMapa con osmdroid contra staging | 96 | 59,3 % |
| 2026-11-24 | #68 social nv-17 | 13_Reacciones y 16_Historia contra staging (QA A/B); arreglos de conteo y «Ver más» en comentarios | 98 | 60,5 % |

---

# Histórico: avance por story points (hasta 2026-11-18)


> Fuente: `Wherever_Product_Backlog_v3.xlsx` (hoja Historias): 238 historias y 684 story points (SP). Meta del 50 %: más de 342 SP.
> Se actualiza al abrir cada PR. Los PR los fusiona Jorge; un PR abierto cuenta aparte ("En PR").

## Cómo se cuenta

- **Hecha:** la historia se puede usar de punta a punta. Si tiene pantalla, está en la app y llama a la API real. Si es solo de servidor ("Sin pantalla"), está construida en `wherever-backend` (`origin/main`, nv-01 a nv-75).
- **En PR:** el código está en un PR abierto con las compuertas locales en verde, sin merge.
- **Parcial:** falta algún criterio de aceptación. No suma puntos.
- No se cuentan las historias del panel web de administración, porque no son de esta app y no hay panel. Tampoco las que no tienen construido su lado móvil, aunque su API ya exista.

## Corte del 2026-11-18, al empezar (main = `63746f9`)

| | Historias | SP | % de 684 |
|---|---|---|---|
| Hechas | 31 | 100 | **14,6 %** |
| En PR (#22, push) | 1 | 3 | 0,4 % |
| Must hechas | — | 78 de 336 | 23,2 % |

### Qué está hecho
- Cuenta y acceso (Sprint 1): Bienvenida, Registro, Login con sesión recordada, Recuperar contraseña, Gustos con presupuesto y alergias con consentimiento, Perfil, Editar perfil, Ajustes, Sesiones, Privacidad, Eliminar cuenta y Eliminación pendiente.
- Base técnica: tokens cifrados con Keystore, refresh único, Room, CI con 23 pasos, APK firmada en Play Store y transiciones (nv-45).
- Servidor: la API cubre casi todo el backlog (lugares, recomendaciones, IA, planes, grupos, reservas, pagos, cupones, reseñas, amigos, notificaciones y negocio).

### Qué está en PR
- #22 `feat/notificaciones-push` (nv-46): push con Firebase. CI en verde; falta la prueba en el teléfono y el merge.
- PR de Dependabot #5 a #12 y #23, sin revisar.

### Qué falta
- Lado móvil de Inicio, Explorar, ficha del negocio, plan con IA, itinerario, reservas y pagos, cupones, check-in y reseñas, grupos, amigos, notificaciones, lealtad y modo negocio.
- Arranque: tarjetas de bienvenida, términos, permiso de ubicación y de avisos, y aviso de versión mínima.
- Staging no tiene negocios cargados (`/categorias` da 0 en los 6 giros). Las pantallas de catálogo mostrarán su estado vacío hasta que un administrador cargue `docs/semilla-negocios.csv` del backend.

### Por épica (al empezar)

| Épica | SP | Hechos | En PR | % |
|---|---|---|---|---|
| EP-01 Arranque, permisos y privacidad | 25 | 0 | 0 | 0 % |
| EP-02 Cuenta, acceso y cambio de modo | 24 | 10 | 0 | 42 % |
| EP-03 Onboarding de gustos | 9 | 7 | 0 | 78 % |
| EP-04 Inicio y exploración | 31 | 0 | 0 | 0 % |
| EP-05 Asistente de IA y recomendación contextual | 68 | 5 | 0 | 7 % |
| EP-06 Itinerario editable | 53 | 0 | 0 | 0 % |
| EP-07 Ficha de negocio, calificaciones y afluencia | 29 | 5 | 0 | 17 % |
| EP-08 Reservas y pagos | 62 | 3 | 0 | 5 % |
| EP-09 Cupones | 29 | 0 | 0 | 0 % |
| EP-10 Planes grupales y Vaca virtual | 53 | 0 | 0 | 0 % |
| EP-11 Check-in y reseñas verificadas | 35 | 10 | 0 | 29 % |
| EP-12 Lealtad, pasaporte y recuerdos | 22 | 0 | 0 | 0 % |
| EP-13 Alta y verificación de negocios | 27 | 0 | 0 | 0 % |
| EP-14 Panel y estadísticas del negocio | 18 | 0 | 0 | 0 % |
| EP-15 Notificaciones | 21 | 0 | 0 | 0 % |
| EP-16 Administración de la plataforma | 39 | 0 | 0 | 0 % |
| EP-17 Habilitadores técnicos | 81 | 56 | 3 | 69 % |
| EP-18 Primer y segundo plano | 37 | 2 | 0 | 5 % |
| EP-19 Amigos y círculo social | 21 | 2 | 0 | 10 % |

### Historias hechas o parciales al empezar

| Historia | SP | Estado | Dónde |
|---|---|---|---|
| US-012 Registro | 3 | Hecha | nv-09, nv-42 |
| US-013 Login y sesión recordada | 3 | Hecha | nv-07, nv-12 |
| US-019 Cerrar sesión | 1 | Hecha | nv-13 |
| US-020, US-021, US-022, US-024 Gustos | 7 | Hecha | nv-10, nv-43, nv-44 |
| US-158 Tokens y refresh | 5 | Hecha | nv-12 |
| US-181 Sesión que se renueva sola | 2 | Hecha | nv-12 |
| US-185 Eliminar cuenta | 3 | Hecha | nv-15 |
| US-226 Datos sensibles cifrados | 5 | Hecha | Keystore; backend nv-48/49 |
| US-236 Buscable por teléfono | 2 | Hecha | nv-15 |
| US-156, US-164, US-225 Arquitectura, pruebas y APK firmada | 13 | Hecha | nv-04, nv-39, nv-41 |
| US-157, 159, 160, 161, 166 Datos, IA, clima, pagos y motor | 25 | Hecha (servidor) | backend nv-01 a nv-60 |
| US-115, 119, 189, 190, 197, 200, 211, 212, 228, 229, 231 | 31 | Hecha (servidor) | backend nv-15 a nv-66 |
| US-162 Push con Firebase | 3 | En PR #22 | nv-46 |
| US-001 Splash | 2 | Parcial: falta versión mínima y Sin conexión | nv-06 |
| US-007 Mis datos | 5 | Parcial: falta Descargar y Borrar historial | nv-15 |
| US-010 Consentimiento aparte | 2 | Parcial: falta el de ánimo | nv-43 |
| US-014 Recuperar contraseña | 2 | Parcial: falta Nueva contraseña desde el enlace | nv-08 |
| US-017 Editar perfil | 2 | Parcial: falta foto, correo, contraseña y tamaño de texto | nv-14 |
| US-148 Elegir avisos | 3 | Parcial: falta horario silencioso | nv-13 |

## Pendientes (lista viva, 2026-11-18 17:30)

Se cierran en este orden. ✅ = cerrado, con el PR o el motivo.

| # | Pendiente | Dónde | Estado |
|---|---|---|---|
| P1 | Rama `fix/auth-telefono` (nv-49) sin PR | autenticación en el teléfono | en curso |
| P2 | Fotos: perfil, reseñas y postales | nv-50 | en curso |
| P3 | `/inicio` da 422 sin `lat`/`lng`: Inicio vacío sin permiso de ubicación | PR #24 (nv-47) | abierto |
| P4 | Rama `feat/plan-ia` (nv-48) con un commit WIP, sin compuertas ni PR | plan con IA | abierto |
| P5 | PR #22 (push): CI verde, falta probar el registro del token en el teléfono | nv-46 | abierto |
| P6 | Correo de recuperación y verificación no sale (staging sin SMTP) | equipo de API | anotado |
| P7 | Staging sin negocios ni ciudades (`/categorias` en 0, `ciudades` vacío) | equipo de API | anotado |
| P8 | APK de release sin probar en el teléfono (aviso de R8, nv-41) | release | abierto |
| P9 | Pantallas sin conectar: Descargar datos y Borrar historial (US-007), Nueva contraseña desde el enlace (US-014), cambiar correo y contraseña (US-017), horario silencioso (US-148) | cuenta | abierto |
| P10 | Pantallas sin construir con API lista: notificaciones, amigos, reservas y pagos, cupones, check-in y reseñas, grupos y Vaca, recuerdos, modo negocio | backlog | abierto |
| P11 | Bienvenida en horizontal: los íconos decorativos tapan el texto | nv-06 | abierto |
| P12 | Tareas sin marcar en specs: pruebas de UI en CI y "en el teléfono" de nv-42 a nv-47 | specs | abierto |
| P13 | 9 PR de Dependabot (#5 a #12 y #23) sin revisar; varios son saltos mayores que exigen `compileSdk 37` | dependencias | abierto |
| P14 | `main` sin protección de rama (constitución §8): requiere administrador | GitHub | fuera de alcance (permisos) |
| P15 | Proveedor de mapas (D25) y numeración única de pantallas (D23) | decisiones | abierto |
| P16 | Sin TODO ni FIXME en `app/src` (revisado con grep) | código | ✅ |

## Plan para pasar del 50 %

Orden: Must con pantalla y API lista, en el orden en que las usa una persona.

| PR | Spec | Historias | SP |
|---|---|---|---|
| Inicio, Explorar y ficha del lugar | nv-47 | 22 historias de EP-01, EP-04, EP-05 y EP-07 | 57 |
| Plan con IA e itinerario editable | nv-48 | EP-05 y EP-06 | ~60 |
| Reservas, pagos y cupones | nv-49 | EP-08 y EP-09 | ~45 |
| Check-in, reseñas, notificaciones y amigos | nv-50+ | EP-11, EP-15 y EP-19 | ~45 |

(La tabla crece con cada PR; el registro de cada uno está abajo.)

## Registro de PR

| PR | Spec | Historias | SP | % acumulado (con PR abiertos) |
|---|---|---|---|---|
| Inicio, Explorar y ficha del lugar | nv-47 | US-004, 009, 025, 027, 030, 032, 034, 037, 047, 053, 062, 067, 068, 069, 070, 071, 072, 073, 074, 111, 121, 187 (parciales: US-028, US-029) | 57 | 23,4 % (160 SP) |
| Integración en `main` (push, auth, plan con IA, foto) | nv-46, nv-48, nv-49, nv-50 | US-162; US-010, 033, 035, 039 a 046, 048 a 050, 055 a 058, 061, 063, 064, 066 (parciales: US-017, US-065) | 69 | 33,5 % (229 SP) |
| Cuenta: contraseña, correo y mis datos | nv-51 | US-007 | 5 | 34,2 % (234 SP) |
| Avisos y horario silencioso | nv-52 | US-147, US-148 | 5 | 34,9 % (239 SP) |
| Amigos | nv-53 | US-232, US-233, US-235, US-238 (parcial: US-234 sin QR) | 13 | 36,8 % (252 SP) |
