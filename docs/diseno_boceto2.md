# Diseño del Boceto 2 para la app móvil

> 21 nov 2026. Fuente: Figma "Wherever — Maqueta móvil", archivo `9IjBujkogqaUPAkDbluclQ`, página **Boceto 2** (`96:130`), la única página del archivo. Revisión de solo lectura: no se modificó el Figma ni el repo.
> Comparado contra: `analisis/guia_estilo_wherever.md`, `analisis/revision_figma_maqueta.md`, `analisis/revision_maqueta2.md` y el repo `wherever-mobile` en `main` (`cd6bb60`, 21 nov): `core/designsystem/*.kt`, `docs/diseno.md`, `docs/routes.md`, `docs/avance.md` y las capturas de `docs/pruebas/`.
> Regla de Jorge (21 nov): **la app debe ser igual al Boceto 2**: mismas funciones, mismos botones en la misma posición y las mismas imágenes.

## 0. Qué cambia, en corto

1. **La identidad no cambia.** El Boceto 2 usa los mismos 13 colores de la colección de variables "Wherever" y las mismas fuentes (Sora Bold y DM Sans) que ya están en la app. No hay que rehacer el tema, sino afinarlo.
2. **La app tomó los colores "a ojo" de los PNG**, así que 7 tonos secundarios están un poco corridos (tabla 2.2). Se corrigen con los hex exactos de Figma.
3. **Medidas a corregir:** botones de 56 a **52 dp** de alto; campos de **52 dp** con esquina 14; tarjetas con esquina 18; títulos de Sora en **Bold** (la app usa SemiBold en varios).
4. **Lo más visible que falta en la app:**
   - Barra inferior: la app tiene 4 pestañas; Figma tiene **5 con el botón naranja "+" al centro** (abre la hoja 15_Crear).
   - Inicio: Figma tiene **foto de portada**, buscador con botón naranja de IA, pestañas "Cerca de ti / Amigos", carrusel de lugares con foto y "Planes de tus amigos". La app hoy muestra clima, chips de compañía y "Para ti" sin fotos.
   - Login y Registro: falta **"Continuar con Google"**.
   - Fotos: Figma muestra foto en casi todas las tarjetas (lugares, planes, paradas, publicaciones, postales y perfiles). La app solo carga fotos en 4 archivos.
5. **Avance:** de las 162 pantallas del Boceto 2, la app tiene 63 con UI (38.9 %, `docs/avance.md`). Faltan completas las áreas de grupos y chats, Vaca, cupones, día del plan, recuerdos, reseñas, ayuda y todo Negocio.

## 1. Cómo está armado el archivo (importante para leerlo con el MCP)

- 162 frames de **390 × 844** en 11 módulos. Cada módulo tiene una etiqueta de texto `label · N · …` en el lienzo (no son secciones de Figma).
- **Las pantallas están aplanadas**: cada frame trae un `Vector` de fondo y un `Clip path group` con el dibujo. No hay componentes, auto-layout, estilos de texto ni sombras como efectos. Los textos sí son texto real (3,909 nodos), con su fuente, tamaño y color.
- **Los botones del prototipo son rectángulos transparentes** (`#FFFFFF` al 1 %) llamados `→ Destino (detalle)`, encima del dibujo. Su nombre dice a qué pantalla lleva; su posición y tamaño son el área tocable. La sección 6 los lista por pantalla.
- Solo hay 3 colores enlazados a variables; el resto son hex sueltos. Los hex coinciden con las variables, salvo los nuevos de la tabla 2.3.
- Consecuencia práctica: `get_design_context` devuelve mucho vector. Para cada pantalla conviene usar `get_screenshot` (referencia visual) y `get_metadata` (posiciones y textos), y `download_assets` para fotos e íconos en SVG.

## 2. Color

### 2.1 Variables de Figma (colección "Wherever", modo "Base")

| Variable | Hex | Token en la app | Uso |
|---|---|---|---|
| color/azul-ruta | `#2455E8` | `RouteBlue` ✓ | primario, enlaces, pestaña activa, botón "Ver plan" |
| color/tinta | `#10243B` | `Ink` ✓ | títulos y texto; texto encima del naranja |
| color/atardecer | `#FF7A3D` | `Sunset` ✓ | botón principal (uno por pantalla), botón "+" y botón IA |
| color/menta | `#12A37F` | `Mint` ✓ | confirmado, éxito, poca gente |
| color/dorado | `#F2B531` | `Gold` ✓ | Sello, estrellas |
| color/niebla | `#F4F7FC` | `Mist` ✓ | fondo de pantallas |
| color/borde | `#DCE3EF` | `OutlineSoft` ✗ (`#E0E6F0`) | bordes de tarjetas y campos, divisores |
| color/gris-azulado | `#55627A` | `InkMuted` ✗ (`#5A6B80`) | texto secundario, placeholder, pestaña inactiva |
| color/blanco | `#FFFFFF` | `Color.White` ✓ | tarjetas y superficies |
| color/azul-claro | `#E8EEFF` | `RouteBlueTint` ✗ (`#E8EEFD`) | indicador de pestaña activa, fondo de chips e íconos |
| color/naranja-claro | `#FFE9DA` | `SunsetTint` ✗ (`#FFE6D8`) | tarjetas naranjas |
| color/menta-claro | `#D9F4EA` | `MintTint` ✗ (`#D9F2EA`) | estados confirmados |
| color/dorado-claro | `#FDF0CF` | `GoldTint` ✗ (`#FCF1D2`) | fondo del Sello |

### 2.2 Correcciones exactas para `Color.kt`

| Token | Hoy | Boceto 2 |
|---|---|---|
| `InkMuted` | `#5A6B80` | **`#55627A`** |
| `OutlineSoft` | `#E0E6F0` | **`#DCE3EF`** |
| `RouteBlueTint` | `#E8EEFD` | **`#E8EEFF`** |
| `SunsetTint` | `#FFE6D8` | **`#FFE9DA`** |
| `MintTint` | `#D9F2EA` | **`#D9F4EA`** |
| `GoldTint` | `#FCF1D2` | **`#FDF0CF`** |
| `DangerTint` | `#FBE6E6` | **`#FBE4E4`** |
| `Danger` | `#D64545` | `#D64545` ✓ |

`SegmentTrack` (`#E2E7F0`) e `InkDeep` (`#16305C`) no salen en Figma; se quedan para el riel del selector y el modo oscuro. **Figma no tiene modo oscuro**: el que ya existe en la app se conserva, recalculado con los tonos nuevos.

### 2.3 Colores que usa Figma y no están en las variables

| Hex | Dónde | Propuesta de token |
|---|---|---|
| `#D64545` | corazón de "me gusta", errores, eliminar (81 usos) | `Danger` (ya existe) |
| `#FBE4E4` | fondo de PagoError, FavVacio, eliminar | `DangerTint` |
| `#5B6B80` | texto secundario en 55 nodos | es casi `InkMuted`: usar `InkMuted` |
| `#E9EEF8`, `#BFD8F5`, `#CDEFE0` | mapa dibujado: calles, agua, parques (Mapa, MapaFull, Llegar, Navegando, ExpMapa, NegUbic, Cambiar, PermisoSys) | `MapLand`, `MapWater`, `MapPark` (solo si se dibuja mapa falso; con Google Maps real se usa un estilo de mapa con esos tonos) |
| `#7A4DE0` / `#EFE8FF` | un ícono morado en la hoja 15_Crear | `Grape` / `GrapeTint` |

### 2.4 Degradados

| Degradado | Dónde |
|---|---|
| `#2455E8` 0 % → `#1B3FB8` 55 % → `#10243B` 100 % | fondo de 01_Bienvenida |
| `#4A74F5` 0 % → `#2455E8` 55 % → `#1B3FB8` 100 % | Generando 1/4 a 4/4 |
| `#3A63E8` 0 % → `#1B2F8F` 55 % → `#10243B` 100 % | 87_PushBloqueo (pantalla bloqueada) |
| `#1B3A63` → `#10243B` | cabecera de 48_NegPanel y tarjeta de nivel en 83_Insignias |
| `#FFB184` → `#FF7A3D` | íconos y portadas naranjas (25 usos: NegPerfil, MisPlanes, NegGaleria) |
| `#6E92FF` → `#2455E8` | íconos azules en círculo (Reserva, Confirmación, NegCheckin) |
| `#49D1AA` → `#0E8C6C` | íconos verdes en círculo (Cambiar, PerfilAmigo) |
| `#FFD66B` → `#F2B531` | insignias doradas |
| `#2455E8` → `#FF7A3D` | anillo de historias y avatar grande de perfil (21 usos) |
| Tinta 55 % → 5 % (45 %) → 60 % | foto de portada de 10_Inicio (390 × 244) |
| Tinta 0 % → 78 % | parte baja de las tarjetas con foto (lugares 160 × 110, planes 292 × 80) |
| Tinta 70 % → 0 % arriba y 0 % → 85 % abajo | 16_Historia a pantalla completa |

## 3. Tipografía

Figma usa **solo Sora Bold** para títulos y **DM Sans "9pt" Regular y Bold** para todo lo demás (9pt es el tamaño óptico de DM Sans; la app usa el corte de 14, diferencia mínima). DM Sans Medium no aparece en el Boceto 2.

| Uso | Fuente | Tamaño | Color | Ejemplo |
|---|---|---|---|---|
| Título grande de pantalla | Sora Bold | 28 (a veces 26, 30, 34) | Ink o blanco | "Qué bueno verte de nuevo", "¿Qué hacemos hoy, Osmar?" |
| Título de pantalla con atrás | Sora Bold | 24 / 22 | Ink | "Plan en pareja", "Mis planes" |
| Título de sección | Sora Bold | 19 / 20 | Ink | "Cerca de ti", "Planes de tus amigos" |
| Nombre en tarjeta | Sora Bold | 16 / 17 | Ink o blanco sobre foto | "Café Raíz", "Sábado de cafés" |
| Botón principal | DM Sans Bold | 17 | Ink sobre naranja | "Entrar", "Reservar todo" |
| Botón chico, etiqueta, enlace | DM Sans Bold | 13 / 14 / 15 | Azul o Ink | "Ver todo" 13, "Olvidé mi contraseña" 14, "Ver plan" 14 blanco |
| Etiqueta de campo | DM Sans Bold | 14 | Ink | "Correo", "Contraseña" |
| Cuerpo | DM Sans Regular | 16 / 15 / 14 | gris-azulado o Ink | subtítulos 16, placeholder 16 |
| Meta y apoyo | DM Sans Regular | 12 / 13 | gris-azulado o blanco | "8 min a pie · 4.8", "3 paradas" |
| Chip sobre foto, barra inferior | DM Sans Bold 11 / 12 | 11–12 | Ink / azul | "Para ti", "Inicio" (activo Bold azul; inactivo Regular gris) |
| Hora de la barra de estado | DM Sans Bold | 15 | — | 9:41 (no se dibuja: es del sistema) |

Alto de línea y espaciado de letras: automáticos en todo el archivo (0 %).

**Cambio en `Type.kt`:** `headlineSmall`, `titleLarge`, `titleMedium` y `titleSmall` pasan de `SemiBold` a **`Bold`** (Figma no usa Sora SemiBold). Sugerencia de escala: `headlineLarge` 28, `headlineMedium` 24, `headlineSmall` 20, `titleLarge` 19, `titleMedium` 17, `titleSmall` 16 (Sora Bold); `bodyLarge` 16, `bodyMedium` 14, `bodySmall` 12 (DM Sans Regular); `labelLarge` 17 Bold (botones), `labelMedium` 14 Bold, `labelSmall` 12 Bold.

## 4. Medidas, formas y componentes

### 4.1 Rejilla y posiciones estándar (lienzo 390 × 844)

| Elemento | Posición y tamaño en Figma | En la app |
|---|---|---|
| Margen lateral | 20 a cada lado; contenido de 350 | `Spacing.XL` 20 ✓ |
| Botón atrás | círculo blanco de 44 con borde `#DCE3EF` 1, en (20, 62); área tocable 62 × 62 en (14, 46) | `BackCircleButton` |
| Acción de la esquina (compartir, ajustes, campana, favorito, +) | círculo de 44 en (326, 54–64) | `CircleIconButton` |
| Título grande | x = 20, y ≈ 104–190 | — |
| **Botón principal abajo** | 350 × **52**, píldora, en y = **764** (pie) o 746 / 730 / 724 | `ButtonHeight` 56 → **52** |
| Dos botones abajo | principal en y = 702 y secundario en y = 764 (o al revés), 12 entre ellos | — |
| Dos botones lado a lado | 170 × 44 en (29, 770) y (195, 770) — Plan: "Invitar amigos" y "Reservar todo"; Detalle: 160 × 44 "A mi plan" y 170 × 44 "Reservar mesa" | — |
| Enlace bajo los botones | centrado, área de 120–200 × 44 en y ≈ 778–784 ("Ahora no", "Omitir por ahora", "Guardar y salir") | `WhereverTextLink` |
| Campo de texto | 350 × **52**, blanco, borde `#DCE3EF` 1, esquina **14**; etiqueta DM Sans Bold 14 encima a 24 px; ícono a la izquierda y texto en x = 67 | `Field` 16 → **14** |
| Buscador de Inicio | 350 × 54, blanco, píldora, con botón naranja circular de 42 a la derecha (ícono de chispa) | — |
| Fila de lista o tarjeta de lista | 350 × 64 (notificaciones, amigos), 350 × 76 (grupos, reservas del negocio), 350 × 100–110 (planes, lugares, reservas) | `RowMinHeight` 52 |
| Tarjeta | blanca, borde `#DCE3EF` 1, esquina **18** (sin sombra visible) | `Card` 20 → **18** |
| Barra inferior | ver 4.3 | — |

### 4.2 Botones

| Tipo | Aspecto | Ejemplos |
|---|---|---|
| Principal | naranja `#FF7A3D`, píldora, 52 alto, texto Ink DM Sans Bold 17 | Entrar, Crear cuenta, Reservar todo, Armar mi plan |
| Secundario con borde | blanco, borde `#DCE3EF` 1, texto Ink Bold 17; sobre azul: transparente con borde blanco y texto blanco | Continuar con Google, Ya tengo cuenta |
| Azul chico | `#2455E8`, píldora 88 × 40, texto blanco Bold 14 | Ver plan (tarjeta de plan de amigo) |
| Con borde azul | blanco, borde azul, texto azul Bold, con ícono | + Agregar parada (178 × 38), Invitar amigos |
| De texto | azul `#2455E8` Bold 13–15 | Ver todo, Ver más, Olvidé mi contraseña, Crea una |
| Peligro | texto o fondo `#D64545`, fondo claro `#FBE4E4` | Eliminar mi cuenta, Eliminar reseña, Sí, cancelar |
| Chip sobre foto | blanco 92 %, 24 alto, píldora, DM Sans Bold 11 Ink | Para ti, Cerca, Cupón 2x1 |
| Chip translúcido sobre foto | blanco 20 %, 40 alto, con ícono | Tula de Allende (ubicación), campana |
| Contador sobre foto | blanco 92 %, 45 × 26, corazón rojo + número | ♥ 11 |

### 4.3 Barra inferior del usuario (frame 10_Inicio)

- Fondo blanco, **76 de alto** (y = 768), 5 espacios de 78 de ancho.
- Orden: **Inicio** (casa), **Explorar** (brújula), **+** (crear), **Planes** (calendario), **Perfil** (persona).
- Activa: ícono y texto azul `#2455E8`, texto DM Sans Bold 12, con píldora `#E8EEFF` de **52 × 30** detrás del ícono. Inactiva: gris-azulado `#55627A`, Regular 12.
- **Botón "+" central**: círculo naranja de **56** en (167, 767) con anillo blanco de 4, sobresale sobre el borde de la barra, sin texto. Abre la hoja **15_Crear** con 5 opciones: Armar un plan con IA → AjustaPlan/IAPalabra; Crear un plan a mano → CrearPlan; Nuevo grupo → CrearGrupo; Compartir un momento → CrearPub; Escribir una reseña → ResPend.
- La app hoy: 4 pestañas (Inicio, Explorar con lupa, Planes, Perfil) y sin "+". **Hay que agregar el "+" y cambiar la lupa por la brújula.**
- Ojo, error de Figma: 24_MisPlanes, 25_MisReservas, 29_Cupones y 41_PlanesVacio tienen una barra vieja sin "+" (Inicio, Explorar, Planes, Amigos/Cupones, Perfil). Usar la barra de 10_Inicio en todas.

### 4.4 Barra inferior del negocio (48_NegPanel)

Panel, Reservas, Cupones, Reseñas y Negocio; mismos 76 de alto y 5 espacios de 78, sin botón central.

### 4.5 Íconos

- Estilo de línea redondeada, trazo de unos 2 px, 24 px (parecido a Lucide/Feather). La app usa Material Icons: para igualar, exportar los SVG de Figma con `download_assets` y guardarlos como Vector Drawables en `res/drawable` (`ic_wh_*`), o usar la familia Material **Rounded/Outlined** más parecida. Recomendado: SVG de Figma para la barra inferior, chispa de IA, atrás, compartir, campana, corazón, ubicación, cambiar parada (⇆) y quitar (✕).
- Íconos dentro de círculos de color (44–52) con los degradados de 2.4.

### 4.6 Logo

- Cuadro redondeado **azul `#2455E8` de 56 × 56** con un **pin blanco** dentro (Login, en (20, 124)). En Bienvenida va al revés: cuadro **blanco** con pin azul, junto a la palabra **"Wherever" en Sora Bold blanca**.
- Bienvenida además dibuja una ruta punteada blanca con 3 puntos: taza (blanco), pin (naranja) y diana (verde).
- La app ya tiene este logo en Login (captura `03_login_claro.png`); revisar el de Bienvenida.

## 5. Pantalla por pantalla: lo que hoy no coincide en la app

| Pantalla | Boceto 2 | App hoy | Qué hacer |
|---|---|---|---|
| 10_Inicio (`292:331`) | Portada con foto (390 × 244, esquinas de abajo redondeadas) y degradado; chip "Tula de Allende"; campana; "¿Qué hacemos hoy, Osmar?" Sora 28 blanco; buscador con botón IA naranja; pestañas "Cerca de ti / Amigos"; carrusel "Cerca de ti" con tarjetas-foto de 160 × 184 y "Ver todo"; "Planes de tus amigos" con tarjetas de 294 × 176 y "Ver más" | Degradado azul sin foto, chip de clima, "Buenas tardes", chips Solo/En pareja/Con amigos/En familia, chips "Para este momento", lista "Para ti" sin fotos | Rehacer según Figma. El clima y los chips no están en el Boceto 2 (ver nota 5.1) |
| Barra inferior | 5 con "+" naranja | 4 sin "+" | Ver 4.3 |
| 03_Login (`120:2389`) | Entrar en y = 639; "o"; **Continuar con Google** en y = 732; "¿No tienes cuenta? Crea una" | Igual, sin Google | Agregar el botón de Google (aunque al inicio muestre "Próximamente") |
| 05_Registro (`292:2`) | Crear cuenta en y = 671 y Google en y = 764; enlaces a términos y aviso | — | Revisar Google y los dos enlaces |
| 30_Explorar (`292:5378`) | Buscador de 240 × 50 + botón de filtros 52 × 52; "Ver en mapa"; tarjetas de 350 × 110 con foto de 84 × 96 | — | Comparar con captura |
| 39_Plan (`292:5544`) | Mapa con ruta numerada y "Ver mapa completo"; fila de 4 datos (paradas, horas, traslados, $ por persona); paradas con foto redonda, hora azul, ⇆ y ✕; traslados entre paradas con "Cómo llegar"; "+ Agregar parada"; abajo "Invitar amigos" y "Reservar todo" | Plan con línea de tiempo | Ajustar a la captura |
| 22_Reserva (`120:4289`) | Una sola pantalla con "Pagar anticipo y reservar" | Spec nv-54: día, personas, turno, nota y resumen | Mantener los datos, pero con el orden y aspecto de Figma |

### 5.1 Regla para lo que está en la app y no en el Boceto 2

Jorge pidió "las mismas funcionalidades que aparecen ahí". Lo que la app tiene de más por la especificación (clima, chips de compañía en Inicio, sesiones abiertas, cambiar correo, mis datos…) **no se borra en silencio**: se quita de la vista principal y se anota en `docs/revision-jorge.md` para que Jorge decida si se queda en otra pantalla (por ejemplo Ajustes) o se elimina.

## 6. Inventario por pantalla: botones, destino e imágenes

Formato: `Texto del botón → pantalla destino`. "Barra" = barra inferior estándar. "Fotos" = cuántas imágenes y sus tamaños. Coordenadas (x, y) en el lienzo de 390 × 844 solo donde no siguen la posición estándar de 4.1. Las variantes `Nombre · algo` son el mismo frame con otro dato o estado.

### 1 · Acceso y registro

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 01_Bienvenida | `120:2281` | Crear cuenta → Registro (y 690); Ya tengo cuenta → Login (y 756) | ilustración vectorial de ruta |
| 03_Login | `120:2389` | atrás; Entrar → Inicio (y 639); Olvidé mi contraseña → Recuperar; Continuar con Google → Inicio (y 732); Crea una → Registro | — |
| 03_Recuperar | `189:538` | atrás; Enviar enlace → RevisaCorreo (y 724); Volver a iniciar sesión → Login | — |
| 04_RevisaCorreo | `189:586` | Abrir correo → Login (y 684); Usar otro correo → Recuperar | — |
| NuevaContrasena | `325:541` | atrás; Guardar contraseña → Login; tiene barra de fuerza de 4 segmentos de 80 × 6 | — |
| 05_Registro | `292:2` | atrás; Crear cuenta → Gustos (y 671); Continuar con Google → Gustos (y 764); términos y aviso de privacidad → Ayuda | — |
| 06_Gustos | `292:76` | atrás; Continuar → EncuentraAmigos (y 728); Omitir por ahora | — |
| 07_EncuentraAmigos (+ · solicitud enviada `340:1301`) | `292:170` | atrás; Más tarde (arriba derecha); Permitir (contactos); 4 × Agregar → enviada; Seguir → Permisos | — |
| 08_Permisos | `292:250` | atrás; Permitir y continuar → PermisoSys (y 728); Ahora no → Inicio | — |
| 06_PermisoSys | `187:18` | Permitir al usar la app (y 542); Permitir y continuar (y 728); Ahora no → Inicio | mapa dibujado |

### 2 · Inicio, feed y notificaciones

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 10_Inicio | `292:331` | campana → Notif; buscador → Buscar; botón IA naranja → AjustaPlan; pestaña Amigos → InicioAmigos; Ver todo → Explorar; tarjetas de lugar → Detalle; Ver más → InicioAmigos; tarjeta de plan y Ver plan → PlanAmigo; Barra | 14: portada 390 × 244, lugares 160 × 184, planes 292 × 118, avatares 30 |
| 11_InicioAmigos | `292:3157` | amigos (44) → AmigosLista; campana → Notif; Cerca de ti → Inicio; 4 historias (60 × 86) → Historia; Falta tu voto / Votar → Votar; tarjeta del plan → PlanAmigo; ♥ 11 → Reacciones; Me apunto → MeApunto; Hacer mi versión → AjustaPlan; Ver más publicaciones → InicioMas; Barra | 5: cabecera 390 × 150, publicación 350 × 160, avatares 34 |
| 12_InicioMas (+ · solicitud enviada `340:1393`) | `292:3348` | Cerca de ti → Inicio; publicación (350 × 230) → PostDetalle; Planear aquí → Detalle; sugerencias Luis/Sofía (150 × 120) → PerfilAmigo; Agregar → enviada; Barra | 1: 350 × 230 |
| 13_Reacciones (+ · solicitud enviada `340:1522`) | `292:3460` | hoja: cerrar; personas → PerfilAmigo; Agregar; Me apunto; Hacer mi versión | 5 |
| 14_MeApunto | `292:2135` | hoja: Apuntarme a 2 paradas (y 718); Ahora no | 8 |
| 15_Crear | `292:2381` | hoja de 5 opciones de 350 × 64 (ver 4.3); cerrar | fondo = Inicio |
| 16_Historia (+ Mariana `349:1089`, Alexander `349:1136`, Paola `349:1183`) | `292:2660` | cerrar (avatar arriba izquierda); Hacer mi versión → AjustaPlan (y 694) | 1 foto a pantalla completa |
| 17_PostDetalle | `292:2705` | atrás; avatar → PerfilAmigo | 1: 350 × 210 |
| 18_Notif (+ · solicitud aceptada `340:990`) | `292:2815` | atrás; 6 avisos de 350 × 64 → PostDetalle, PostalVer, PostVisita, Votar, PlanAmigo, DetCupon; Aceptar; Barra | — |
| 19_Buscar | `292:2934` | Cancelar; pestaña Personas; recientes "Tacos" → SinResultados; idea "Un día tranquilo" → AjustaPlan; resultados → Detalle | — |
| 20_BuscarPersonas (+ · solicitud enviada `340:1121`) | `292:3022` | Cancelar; Lugares; persona → PerfilAmigo; Agregar → enviada; "¿Está contigo?" → AgregarAmigo | — |
| 21_SinResultados | `292:3097` | Cancelar; Pedirle a la IA algo parecido → AjustaPlan (y 724); Quitar filtros → Buscar (y 774) | — |

### 3 · Amigos

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 22_AmigosLista | `292:4574` | atrás; + → AgregarAmigo; pestaña Solicitudes; 5 amigos (350 × 64) → PerfilAmigo; Mis grupos → MisGrupos; Barra | — |
| 23_AmigosSolic | `292:4198` | atrás; + ; pestaña Amigos; Aceptar ×2; Cancelar (enviada); Barra | — |
| 24_AgregarAmigo (+ · solicitud enviada `340:1204`) | `292:4295` | atrás; Mi código QR → MiCodigo; Mis contactos → EncuentraAmigos; Buscar → BuscarPersonas; 3 × Agregar; Compartir mi enlace (y 566) | — |
| 25_MiCodigo | `292:4381` | atrás; Compartir mi código (y 702) | QR |
| 26_PerfilAmigo (+ Mariana `346:3`, Paola `346:334`, Alexander `346:665`, Diego `346:996`, Luis `346:1327`, Sofía `346:1658`) | `292:3697` | atrás; ⋯ → AmigoOpc; Invitar a un plan (240 × 44); Mensaje (98 × 30) → ChatAmigo; rejilla → PostDetalle | 9: rejilla 3 × 2 de 120 × 96, avatar 88 con anillo azul-naranja |
| 27_AmigoOpc | `292:3842` | hoja: Silenciar, Invitar a un grupo, Eliminar de mis amigos, Bloquear (350 × 56) | igual a PerfilAmigo |
| 28_PrivSocial | `292:4027` | atrás (falta Guardar, ver revisión anterior) | — |
| 29_PlanAmigo (+ Domingo de jardín `347:376`) | `292:4092` | atrás; compartir; Me apunto (170 × 46 en 32, 768); Hacer mi versión (212, 768) | 4: portada 390 × 240, paradas 56 × 56 |

### 4 · Explorar y lugares

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 30_Explorar | `292:5378` | campana; buscador (240 × 50) → Buscar; filtros (52 × 52) → Filtros; Ver en mapa → ExpMapa; 3 lugares (350 × 110) → Detalle; Barra | 3: 84 × 96 |
| 31_Filtros | `292:4954` | cerrar; Limpiar; Ver en mapa; Ver 24 lugares (y 764) | 3 |
| 32_ExpMapa | `292:5280` | atrás; filtros; Ver detalle (126 × 44); A mi plan (120 × 44) | mapa + 1: 64 × 64 |
| 33_Detalle (+ Jardín del Cerro `347:3`, Fonda Doña Meche `347:124`, Mercado de Tula `347:244`) | `292:5168` | atrás; favorito (326, 54); "Cafetería · $$ · 126 reseñas" → ResLugar; Sello de Excelencia → Sello; "Mariana y 2 amigos han ido" → PlanAmigo; cupón (350 × 64) → DetCupon; A mi plan (160 × 44 en 15, 770); Reservar mesa (170 × 44 en 190, 770; en parque y mercado dice "Cómo llegar") | 1: portada 390 × 262 |
| 34_Sello | `292:4688` | atrás; Café Raíz → Detalle; Ver negocios con Sello (y 764) | 1: 48 × 48 |
| 35_Favoritos | `292:4827` | atrás; 4 lugares (350 × 100) → Detalle; Barra | 4: 64 × 64 |
| 36_FavVacio | `292:4765` | atrás; Explorar lugares (y 455); Barra | ilustración |

### 5 · Crear plan con IA

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| IAPalabra | `331:3` | atrás; Prefiero ajustar los detalles → AjustaPlan (350 × 40 en y 704); ✦ Armar mi plan → Generando (y 764) | — |
| 17_AjustaPlan | `189:844` | atrás; Crear plan a mano → CrearPlan (y 688); Armar mi plan → Generando (y 764) | — |
| CrearPlan | `320:218` | atrás; + Agregar parada (350 × 46 en y 622); Crear plan → Plan | 2: 40 × 40 |
| Generando 1/4 | `120:3270` | Cancelar (200 × 48 en 95, 766) | fondo degradado |
| Generando 2/4, 3/4 | `358:5`, `358:64` | Cancelar | — |
| Generando 4/4 | `358:123` | Ver mi plan → Plan (200 × 48) | — |
| 39_Plan (+ Paseo familiar `347:486`) | `292:5544` | atrás; compartir; Ver mapa completo → Mapa; parada → Cambiar; Cómo llegar → Llegar; + Agregar parada (178 × 38 en 106, 700); Invitar amigos → Compartir (170 × 44 en 29, 770); Reservar todo → Reserva (195, 770) | 3: paradas 38 × 44 |
| 40_Cambiar | `292:5784` | hoja sobre el plan: 2 alternativas (350 × 70) → Plan; Buscar otro lugar; Quitar esta parada; Cambiar parada (y 764) | 5 |
| 41_AgregarParada | `292:5706` | atrás; 3 sugerencias (350 × 90) con Agregar (110 × 44) → Plan | 3: 52 × 52 |

### 6 · Compartir, grupos y chats

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 42_Compartir | `298:6235` | hoja: enlace wherever.app/p/… con Copiar; Invitar amigos; Reservar todo | 6: paradas y avatares 36 |
| 43_CrearGrupo | `298:6582` | atrás; Crear grupo → GrupoChat (y 764) | — |
| 44_MisGrupos | `298:6457` | pestaña Reservas → MisReservas; + → CrearGrupo; 3 grupos (350 × 76) → GrupoChat; Nuevo grupo; + Nuevo mensaje; chat de Jorge → ChatAmigo; Barra | — |
| 45_GrupoChat (+ Nosotros `349:1233`, Familia Reséndiz `349:1345`) | `298:6669` | atrás; info → Grupo; Ver plan; Reservar; tarjeta de votación con Votar → Votar | 2: 52 × 52, 190 × 104 |
| 46_Grupo | `298:6776` | atrás; Punto de encuentro → PuntoMedio; La Vaca virtual → Vaca; Marcar pagado; Ir a la votación del lugar (y 766) | — |
| 47_PuntoMedio | `298:6004` | atrás; Centrar; Usar este punto → Grupo | mapa |
| 48_Votar | `298:6145` | atrás; Enviar mi voto → GrupoChat (y 740) | 3: 64 × 64 |
| ChatAmigo | `341:3` | atrás; cabecera → PerfilAmigo; + Invitar a un plan → InvitarPlan (176 × 38 en y 704) | — |
| InvitarPlan | `341:82` | hoja: + Crear un plan nuevo → CrearPlan; Enviar invitación | — |
| ChatAmigo · invitación enviada | `341:145` | tarjeta del plan (236 × 150) → Plan | 1: 236 × 64 |

### 7 · Reserva, pago y mis planes

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 22_Reserva | `120:4289` | atrás; Pagar anticipo y reservar → Pasarela (y 746) | — |
| 35_Pasarela | `190:1090` | atrás (44 en 20, 58); Pagar $74 → Confirmacion (y 764) | — |
| 36_PagoError | `190:1167` | Intentar con otra tarjeta → Pasarela (y 702); Volver a mi reservación → DetReserva (y 764) | — |
| 23_Confirmacion | `120:4387` | Compartir con el grupo (y 702); Ver mi plan → MisPlanes (y 764) | — |
| 24_MisPlanes | `120:4464` | + (326, 64); pestaña Pasados → RecPlan; 3 planes (350 × 100) → Plan / GrupoChat; Mis grupos → MisGrupos; Barra (vieja, usar la nueva) | — |
| 41_PlanesVacio | `190:1351` | Armar mi primer plan → AjustaPlan (y 455); Explorar lugares (y 509); Barra | ilustración |
| 25_MisReservas | `120:4627` | atrás; 2 reservas (350 × 110) → DetReserva; Ver QR (110 × 44); Pagar $60 → Pasarela; Barra | — |
| 26_DetReserva (+ Café Raíz `349:1461`, Tacos El Güero `349:1745`) | `120:4717` | atrás; Cómo llegar; Compartir; Cambiar hora; Cancelar reservación | QR |
| 38_CambiarHora | `190:1211` | atrás; Confirmar cambio (y 764) | — |
| 27_Cancelar | `120:4996` | atrás; Mantener reservación (y 698); Sí, cancelar (y 764) | — |
| 28_Vaca (+ Mariana pagó `340:491`, todos pagaron `340:583`) | `120:5060` | atrás; Marcar pagado (146 × 44 en 224, 528 / 580); Agregar gasto (y 764) | — |
| 45_AgregarGasto | `193:1416` | atrás; Guardar gasto (y 764) | — |
| 29_Cupones | `120:5218` | atrás; 4 cupones con Usar (110 × 44 en x 280) → DetCupon; Barra | — |
| 47_DetCupon (+ $50 `349:569`, 15 % `349:641`, Postre `349:713`) | `193:1504` | atrás; Reservar con este cupón (y 702); Ver Café Raíz (y 764) | — |

### 8 · El día del plan

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 13_Mapa | `120:3328` | atrás; compartir; mapa → MapaFull; 3 paradas (350 × 52) → Detalle; Cómo llegar a Café Raíz (y 766) | mapa dibujado |
| 14_MapaFull | `120:3502` | atrás; compartir; Ver pasos → Llegar; Iniciar ruta → Navegando | mapa |
| 15_Llegar | `120:3808` | atrás; Iniciar ruta (y 774) | mapa |
| 18_Navegando | `188:216` | salir (44 en 20, 58); tarjeta de llegada (350 × 64 en y 776) → CheckinOk | mapa |
| 26_CheckinOk | `190:1041` | Tomar una postal → Camara (y 702); Ir a la siguiente parada → Mapa (y 764) | — |

### 9 · Recuerdos y reseñas

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 71_Camara | `298:7061` | cerrar; obturador (100 × 100 en 145, 690) → Postal | 2: visor 350 × 490, miniatura 52 |
| 72_Postal | `298:6874` | atrás; Guardar en mi álbum (y 702); Compartir postal (y 764) | 1: 326 × 230 |
| 73_Album | `298:6943` | atrás; cámara; pestaña Por plan → RecPlan; 6 postales (170 × 170) → PostalVer; Barra | 6: 146 × 110 |
| 74_RecPlan | `298:7119` | atrás; cámara; 4 planes → PostalVer; Barra | 12: 96 × 60 |
| 75_PostalVer (+ 6 variantes `349:3` a `349:468`) | `298:7269` | atrás; compartir; Compartir (120 × 44); Eliminar postal (160 × 44) | 1: 326 × 250 |
| 76_RecVacio | `298:7358` | atrás; cámara; Cómo tomar una postal (y 477); Ver mis planes (y 531); Barra | ilustración |
| 68_PostVisita | `298:7592` | atrás; Calificar a detalle → ResCom; Publicar en el feed (y 722); Guardar solo en mis recuerdos (y 774) | 1: 350 × 140 |
| 69_Publicado | `298:7431` | ¿Repetimos? → AjustaPlan; Ver mi publicación (y 702); Volver al inicio (y 764) | 1: 318 × 150 |
| 70_CrearPub | `298:7516` | Cancelar (arriba izquierda); Publicar (y 764) | 1: 350 × 220 |
| 77_ResPend | `298:8229` | atrás; Reseñar (110 × 44) → PostVisita; Barra | 4: 56 × 56 |
| 78_ResCom | `298:8342` | atrás; Listo (y 764) | — |
| 79_MisRes | `298:8446` | atrás; Por reseñar · 3 → ResPend; Editar ×3 → ResCom; Eliminar ×3 → confirmar; Barra | 3: 40 × 40 |
| MisRes · confirmar eliminar | `340:681` | diálogo 310 × 208: Cancelar / Eliminar (124 × 46) | — |
| MisRes · reseña eliminada | `340:831` | aviso con Deshacer (100 × 52 en 270, 690); Barra | — |
| 80_ResLugar | `298:8578` | atrás | — |

### 10 · Perfil, ajustes y estados

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 81_Perfil | `298:8711` | campana (266, 54); ajustes (325, 54); compartir mi código (326, 210); Editar perfil (260 × 44); amigos y reseñas (contadores); nivel e insignias → Insignias; 4 accesos de 80 × 84: Reservas, Favoritos, Recuerdos, Cupones; Mis reseñas (350 × 48); rejilla → PostDetalle; Barra | 6: rejilla 120 × 112 |
| 82_EditarPerfil | `298:8875` | Cancelar / Listo (arriba); Mis gustos → Gustos; Privacidad social → PrivSocial | — |
| 83_Insignias | `298:8945` | atrás | insignias con degradado dorado |
| 84_DatosPers | `298:7692` | atrás; Eliminar mi cuenta (200 × 40); Guardar cambios (y 764) | — |
| 85_Ajustes | `298:7759` | atrás; Datos personales; Contraseña → NuevaContrasena; Tus gustos; Privacidad social; Registrarlo (negocio) → NegCuenta; Ayuda; Cerrar sesión (y 771) | — |
| 86_Ayuda | `298:7902` | atrás | — |
| EliminarCuenta | `325:616` | atrás; Mejor me quedo (y 700); Eliminar mi cuenta (y 764) | — |
| 87_PushBloqueo | `298:7839` | 3 avisos (350 × 100) → DetReserva, Votar, PostVisita | fondo degradado |
| 88_PushBanner | `298:7989` | aviso arriba → Navegando | — |
| 89_SinConexion | `298:8166` | Plan guardado y reserva WH-4821 (350 × 56); Reintentar (y 764) | — |

### 11 · Negocio

| Frame | node-id | Botones | Fotos |
|---|---|---|---|
| 42_NegCuenta → 46_NegVerif | `120:6489`, `120:6903`, `120:6588`, `120:7195`, `120:6790` | atrás; Continuar / Enviar solicitud (y 730); Guardar y salir (137 × 44) | — |
| 47_NegListo | `120:7015` | Ir al panel (y 702); Editar mi solicitud (y 764) | — |
| 48_NegPanel | `120:7089` | Reservas de la semana → NegStats; Ver todas; 2 reservas (350 × 70) → NegCheckin; barra del negocio | 1: portada 396 × 264 |
| 49_NegReservas | `120:7457` | 5 reservas (350 × 76) → NegCheckin; barra | — |
| 50_NegCheckin (+ 4 variantes `349:788` a `349:1010`) | `120:7307` | atrás; Confirmar llegada (y 702); Reprogramar (y 764) | — |
| 51_NegResenas | `120:7549` | Responder reseñas → NegResp; Momentos de clientes → NegMomentos; barra | — |
| 52_NegResp | `120:7378` | atrás; Publicar respuesta (y 764) | — |
| NegMomentos | `311:68` | barra | 4: 169 × 128 |
| 53_NegCupones | `120:1679` | 3 cupones (350 × 80) → NegCrear; Crear cupón (y 704); barra | — |
| 54_NegCrear | `120:1777` | atrás; Publicar cupón (y 764) | — |
| 55_NegHoras | `120:1852` | atrás; Guardar cambios (y 764) | — |
| 56_NegStats | `120:1989` | atrás; barra | — |
| 57_NegCobros | `120:2090` | atrás; cuenta ••••1234 → NegBanco | — |
| 58_NegPerfil | `120:2157` | Cambiar foto → NegAgregarFoto; Datos y horario → NegHoras; Fotos → NegGaleria; Cobros → NegCobros; Facturación → NegFiscal; Volver a Wherever → Inicio (y 507); Cerrar sesión (y 695); Eliminar mi cuenta; barra | portada naranja |
| 87_NegGaleria | `229:877` | atrás; Agregar → NegAgregarFoto; Guardar cambios (y 764) | fotos 111 × 111 |
| 88_NegAgregarFoto | `229:1010` | hoja: Tomar foto, Elegir de la galería, Importar de Instagram (350 × 56); Guardar cambios | — |
| 89_NegBanco | `229:806` | atrás; Guardar cuenta (y 764) | — |
| 90_NegFiscal | `229:1176` | atrás; Guardar datos (y 764) | — |

## 7. Imágenes: cómo cargarlas igual que en Figma

1. **Fotos de contenido** (lugares, planes, paradas, publicaciones, postales, historias, perfiles, galería del negocio): vienen de la API y se cargan con **Coil** (`coil-compose` 3.3.0 ya está en el proyecto) con el recorte, tamaño y esquinas de la tabla 6, y con el degradado de tinta encima cuando el texto va sobre la foto (2.4).
   - Mientras cargan: el esqueleto (`ListSkeleton`) del mismo tamaño. Si falla o no hay foto: un fondo `RouteBlueTint` con el ícono de la categoría, nunca un hueco.
   - Staging no tiene negocios (`docs/avance.md`): para que se vean, sembrar los negocios del Boceto 2 (Café Raíz, Jardín del Cerro, Fonda Doña Meche, Mercado de Tula, Tacos El Güero…) con sus fotos en el backend. Eso es tarea de la API, no del móvil.
2. **Fotos fijas de la app** (portada de Inicio con la calle de Tula, ilustraciones de Bienvenida y de estados vacíos, logo): se exportan de Figma con `download_assets` y se guardan en `res/drawable-nodpi` en WebP (portada a 2x: 780 × 488).
3. **Íconos**: SVG de Figma a Vector Drawable (4.5).
4. **Previsualizaciones y pruebas de captura**: usar las fotos exportadas de Figma como datos de ejemplo, para que la captura del emulador se pueda poner junto a la de Figma.

## 8. Lo que ya estaba en los análisis anteriores

- `guia_estilo_wherever.md` (Boceto 1): **sigue vigente**. Colores y fuentes son los mismos; este documento solo agrega medidas exactas, colores nuevos y degradados.
- `revision_figma_maqueta.md` (Boceto 1): **ya no aplica**. El Boceto 1 se borró y el Boceto 2 cubre casi todo lo que faltaba.
- `revision_maqueta2.md` (27 oct): **sigue vigente** para errores de flujo y datos (fechas de 2025, barra de Planes, pasarela sin enlace al rechazo). Los errores de datos de ejemplo (fechas y montos) **no se copian a la app**: la app muestra datos reales.
