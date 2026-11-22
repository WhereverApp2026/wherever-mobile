# Jorge — Social, Explorar y lo compartido

> GitHub: `JaRe0528`. Reglas: `CONTRIBUTING.md`. Revisor: Osmar. Actualizado: 2026-11-22.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR. Sigues a cargo de lo compartido (theme, `core/designsystem`, navegación, cliente de API y CI) y de atender los Issues que abran los demás sobre eso.

## nv-25 — Compartir, grupos y chats

- [ ] Probar en el teléfono contra staging lo que ya está en `main`: `44_MisGrupos`, `45_GrupoChat` y `GrupoChat · *` (PR #40; `docs/pruebas-telefono.md` #9 y #10)
- [ ] Pantalla `43_CrearGrupo`
- [ ] Pantalla `46_Grupo`
- [ ] Pantalla `47_PuntoMedio`
- [ ] Pantalla `48_Votar`
- [ ] Pantalla `ChatAmigo` y `ChatAmigo · *`
- [ ] Hoja `42_Compartir`
- [ ] Hoja `InvitarPlan`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## nv-20 — Amigos (lo que falta)

- [ ] Pantalla `07_EncuentraAmigos` y `EncuentraAmigos · solicitud enviada` (contactos)
- [ ] Variantes de `PerfilAmigo · *` que falten
- [ ] Compuertas, teléfono y PR

## nv-21 — Explorar (lo que falta)

- [ ] Pantalla `32_ExpMapa`: espera la decisión del proveedor de mapa (D25)
- [ ] Pantalla `34_Sello`: espera que `GET /lugares` filtre por Sello y traiga `sello_excelencia` (Osmar, backend)
- [ ] Compuertas, teléfono y PR

## Notas

- Lo bloqueado por backend va en un Issue con la etiqueta del área; Osmar lo atiende del lado de `wherever-backend`.
- `google-services.json` sigue saliendo de ti: pásaselo a Borre.
