# Jorge — Social, Explorar y lo compartido

> GitHub: `JaRe0528`. Reglas: `CONTRIBUTING.md`. Revisor: Osmar. Actualizado: 2026-11-22.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR. Sigues a cargo de lo compartido (theme, `core/designsystem`, navegación, cliente de API y CI) y de atender los Issues que abran los demás sobre eso.

## nv-25 — Compartir, grupos y chats

- [ ] Probar en el teléfono contra staging lo que ya está en `main`: `44_MisGrupos`, `45_GrupoChat` y `GrupoChat · *` (PR #40; `docs/pruebas-telefono.md` #9 y #10)
- [x] Pantalla `43_CrearGrupo` (2026-11-23; también invitar a más amigos)
- [x] Pantalla `46_Grupo` (sin mapa: D25)
- [ ] Pantalla `47_PuntoMedio`
- [x] Pantalla `48_Votar` (votar, cambiar y cerrar; crear votación pendiente)
- [ ] Pantalla `ChatAmigo` y `ChatAmigo · *` (bloqueo: no hay chat 1 a 1 en el backend)
- [x] Hoja `42_Compartir` (compartir del sistema en 39_Plan, nv-24)
- [x] Hoja `InvitarPlan` (desde el perfil del amigo)
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## nv-20 — Amigos (lo que falta)

- [x] Pantalla `07_EncuentraAmigos` y `EncuentraAmigos · solicitud enviada` (contactos, 2026-11-22)
- [ ] Variantes de `PerfilAmigo · *` que falten
- [ ] Compuertas, teléfono y PR

## nv-21 — Explorar (lo que falta)

- [ ] Pantalla `32_ExpMapa`: espera la decisión del proveedor de mapa (D25)
- [ ] Pantalla `34_Sello`: espera que `GET /lugares` filtre por Sello y traiga `sello_excelencia` (Osmar, backend)
- [ ] Compuertas, teléfono y PR

## Notas

- Lo bloqueado por backend va en un Issue con la etiqueta del área; Osmar lo atiende del lado de `wherever-backend`.
- `google-services.json` sigue saliendo de ti: pásaselo a Borre.
