# Avance de Wherever Móvil contra el backlog

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
