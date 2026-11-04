# Contexto para Claude Code: Sprint 1 móvil + API v1

Este archivo junta lo que la app móvil necesita para conectar las primeras pantallas con la API real.
Parte 1: historias del Sprint 1 (3 al 16 nov 2026) que tocan la app. Parte 2: referencia completa de la API v1.

## Parte 1: Historias del Sprint 1 (backlog v3)

Meta del Sprint 1: cuenta y acceso funcionando de punta a punta en el teléfono contra el servidor real.

| Historia | Responsable | Pantalla (maqueta) | Criterios de aceptación |
| --- | --- | --- | --- |
| US-158 Autenticación con tokens y roles (lado móvil) | Jorge | Sin pantalla | JWT con refresco; roles en cada endpoint. En la app: guardar tokens cifrados, interceptor que agrega Bearer y hace refresh una vez ante 401. |
| US-012 Registro con correo y contraseña | Jorge | 05_Registro | Campos nombre, fecha de nacimiento (18+), correo y contraseña (mín. 8, letra y número); validación al salir de cada campo; aviso si el correo ya existe; envía correo de verificación. |
| US-013 Iniciar sesión y recordar la sesión | Jorge | 03_Login | Refresh token cifrado (DataStore + Keystore) válido 30 días; bloqueo de 15 min tras 5 intentos fallidos (423); regresa al destino del enlace profundo tras el login. |
| US-014 Recuperar contraseña por correo | Jorge | 03_Recuperar, 04_RevisaCorreo | Envía enlace; reenvío disponible a los 60 s; el enlace abre la app en Nueva contraseña y termina con sesión iniciada; el enlace vence en 1 h. |
| US-019 Cerrar sesión | Jorge | 85_Ajustes | Botón en Ajustes con confirmación; borra tokens y caché personal; regresa a Iniciar sesión. |
| US-020 Elegir gustos con chips (mínimo 3) | Alexander | 06_Gustos | 15 chips con emoji; mínimo 3 para continuar; botón Omitir; se guardan en el perfil. |
| US-021 Gasto habitual por salida | Alexander | 06_Gustos | Deslizador 0 a 1,000+ MXN, inicial 150 a 300; si no hay ubicación, pide ciudad con autocompletado. |
| US-022 Dieta, alergias, accesibilidad | Alexander | 06_Gustos | Chips opcionales; Alergias abre un campo de texto. |
| US-007 Ver, descargar y borrar mis datos | Alexander | 28_PrivSocial, EliminarCuenta | Interruptores de ubicación, aprendizaje, ánimo y datos de uso; Descargar mis datos (enlace por correo, 24 h); Borrar historial; Eliminar cuenta escribiendo ELIMINAR, efectiva en 30 días y cancelable. |
| US-236 Decidir quién puede encontrarme | Alexander | 28_PrivSocial | Interruptor buscable por teléfono (apagado por defecto); cambios al instante. |
| US-017 Editar perfil (Should) | Sin asignar | 82_EditarPerfil, 84_DatosPers | Cambios se guardan al instante; cambiar correo pide verificación; tema Claro, Oscuro o Automático. |
| US-001 Splash | Osmar | 01_Bienvenida | SplashScreen API; 0.8 a 2 s; manda a Bienvenida, Inicio (sesión válida), Login (sesión vencida) o Sin conexión con Reintentar. |

Fuera de alcance por ahora: US-232/233 (amigos) no tiene endpoints publicados todavía; Google Sign-In (POST /auth/google) está diferido.


Fuente: documentación del equipo de backend (artifact "Wherever API v1 para front"). Los valores son ejemplos; los nombres y tipos de campo son los reales.

## Parte 2: Referencia de la API v1

### Reglas generales

- Base staging: `https://18-227-230-50.sslip.io` · Local: `http://localhost:8080` · Prefijo: `/api/v1`
- Staging se apaga diario a las 22:00 y enciende lunes a viernes a las 08:00 (hora del centro de México). Fines de semana usar la API local.
- Rutas con JWT: `Authorization: Bearer <access_token>`. El access token dura 15 min (`expira_en`). Con 401, llamar a `/auth/refresh` una vez y repetir la petición.
- El refresh token dura 30 días y ROTA: guardar siempre el nuevo. Reusar uno viejo cierra todas las sesiones del dispositivo.
- JSON en snake_case y en español, fechas ISO 8601 UTC, `Content-Type: application/json`, cuerpo máximo 10 MB. Cada respuesta trae `X-Request-Id`.
- `PATCH /usuarios/yo` exige `If-Match: "<version>"`. Con 409 mostrar "tus datos cambiaron en otro dispositivo" y volver a pedir el perfil.
- Cuenta con eliminación pendiente: solo responden `GET /usuarios/yo`, cancelar eliminación y logout; lo demás da 403.
- Enlaces de correo (verificar, restablecer, exportación) traen el token en el fragmento `#token=XYZ`; la app lo manda en el cuerpo.
- Pendiente: `POST /auth/google` (diferido).

### Forma de los errores

```json
{ "codigo": "validacion", "mensaje": "datos inválidos", "campos": [ { "campo": "correo", "mensaje": "no es un correo válido" } ], "traza_id": "f6bd9485da30dcf0fbcd3eb34cfe4915" }
```

- 400 `solicitud_invalida`
- 401 `no_autenticado`
- 403 `prohibido`
- 404 `no_encontrado`
- 409 `conflicto`
- 410 `vencido`
- 422 `validacion`
- 423 `bloqueado`
- 429 `demasiadas_peticiones`
- 503 `no_disponible`

Nota del perfil: Perfil completo (mismo objeto que GET /usuarios/yo). `ciudad` y `gustos` pueden ser null; `horario_silencioso` también.

### Salud

No requieren sesión. Útiles para saber si el servidor está arriba.

#### GET /health

- Auth: no
- El proceso responde. No consulta bases.

Response 200:

```json
{
  "estado": "ok",
  "version": "d2dc808"
}
```

#### GET /ready

- Auth: no
- El servidor y sus bases están listos. Con Redis caído responde 200 con estado "degraded".

Response 200:

```json
{
  "estado": "ok",
  "dependencias": {
    "mongodb": "ok",
    "redis": "ok",
    "neo4j": "ok",
    "indices_mongodb": "ok"
  }
}
```

Response 503:

```json
{
  "estado": "error",
  "dependencias": {
    "mongodb": "ok",
    "redis": "ok",
    "neo4j": "error",
    "indices_mongodb": "ok"
  }
}
```

#### GET /api/v1/ping

- Auth: no
- Comprueba que el prefijo /api/v1 resuelve.

Response 200:

```json
{
  "estado": "pong"
}
```

### Autenticación y sesiones

Hoja J1. Las respuestas con tokens tienen siempre la misma forma.

#### POST /api/v1/auth/login

- Auth: no
- Límite: 5 cada 5 min por IP
- Inicia sesión con correo y contraseña y abre una sesión para el dispositivo.
- Al 5.º fallo del mismo correo (exista o no) responde 423 durante 15 min.
- No revela si el correo existe: correo o contraseña mal = 401.

Request:

```json
{
  "correo": "ana@ejemplo.com",
  "password": "Wherever2026",
  "dispositivo": {
    "modelo": "Pixel 8",
    "sistema": "Android 15",
    "app_version": "0.1.0"
  }
}
```

Response 200:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi…",
  "refresh_token": "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd",
  "expira_en": "2026-11-04T15:15:00Z",
  "usuario": {
    "id": "6728f0c1a4b2c3d4e5f60718",
    "nombre": "Ana López",
    "correo": "ana@ejemplo.com",
    "correo_verificado": false,
    "roles": [
      "consumidor"
    ]
  }
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion, 423 bloqueado, 429 demasiadas_peticiones, 503 no_disponible

#### POST /api/v1/auth/refresh

- Auth: refresh_token
- Límite: 10/min por IP
- Cambia el refresh token por tokens nuevos. El refresh enviado deja de servir.

Request:

```json
{
  "refresh_token": "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd"
}
```

Response 200:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi…",
  "refresh_token": "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd",
  "expira_en": "2026-11-04T15:15:00Z",
  "usuario": {
    "id": "6728f0c1a4b2c3d4e5f60718",
    "nombre": "Ana López",
    "correo": "ana@ejemplo.com",
    "correo_verificado": false,
    "roles": [
      "consumidor"
    ]
  }
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion, 429 demasiadas_peticiones, 503 no_disponible

#### POST /api/v1/auth/logout

- Auth: JWT
- Cierra la sesión actual. El access token deja de valer al instante.

Response 204:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 503 no_disponible

#### POST /api/v1/auth/logout-todos

- Auth: JWT
- Cierra todas las sesiones del usuario en todos sus dispositivos.

Response 204:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 503 no_disponible

#### GET /api/v1/auth/sesiones

- Auth: JWT
- Lista las sesiones abiertas. `actual` marca la de este dispositivo.

Response 200:

```json
[
  {
    "id": "6728f2aa0b1c2d3e4f506172",
    "dispositivo": {
      "modelo": "Pixel 8",
      "sistema": "Android 15",
      "app_version": "0.1.0"
    },
    "ultimo_uso": "2026-11-03T19:05:00Z",
    "actual": true
  },
  {
    "id": "6728f3bb0b1c2d3e4f506173",
    "dispositivo": {
      "modelo": "iPhone 15",
      "sistema": "iOS 19",
      "app_version": "0.1.0"
    },
    "ultimo_uso": "2026-11-01T02:10:00Z",
    "actual": false
  }
]
```

Errores: 401 no_autenticado

#### DELETE /api/v1/auth/sesiones/{id}

- Auth: JWT
- Cierra una sesión propia por su id. Una ajena o inexistente da 404.

Response 204:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 404 no_encontrado

#### POST /api/v1/auth/recuperar

- Auth: no
- Límite: 10/min por IP
- Manda el correo para restablecer la contraseña (enlace de 1 h).
- Responde 202 exista o no la cuenta: la pantalla siempre dice "si el correo existe, te llegará un enlace".
- No reenvía antes de 60 s.

Request:

```json
{
  "correo": "ana@ejemplo.com"
}
```

Response 202:

```json
(sin cuerpo)
```

Errores: 400 solicitud_invalida, 422 validacion, 429 demasiadas_peticiones

#### POST /api/v1/auth/restablecer

- Auth: token del enlace
- Límite: 10/min por IP
- Fija una contraseña nueva con el token del correo e inicia sesión. Cierra las otras sesiones.
- Contraseña: 8 a 128 caracteres con al menos una letra y un número.
- `dispositivo` es opcional.
- El token es de un solo uso.

Request:

```json
{
  "token": "token-del-fragmento-del-enlace",
  "password_nuevo": "NuevaClave2026",
  "dispositivo": {
    "modelo": "Pixel 8",
    "sistema": "Android 15",
    "app_version": "0.1.0"
  }
}
```

Response 200:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi…",
  "refresh_token": "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd",
  "expira_en": "2026-11-04T15:15:00Z",
  "usuario": {
    "id": "6728f0c1a4b2c3d4e5f60718",
    "nombre": "Ana López",
    "correo": "ana@ejemplo.com",
    "correo_verificado": false,
    "roles": [
      "consumidor"
    ]
  }
}
```

Errores: 400 solicitud_invalida, 422 validacion, 429 demasiadas_peticiones

#### POST /api/v1/auth/verificar-correo

- Auth: token del enlace
- Límite: 10/min por IP
- Confirma el correo con el token del enlace (24 h, un solo uso).

Request:

```json
{
  "token": "token-del-fragmento-del-enlace"
}
```

Response 200:

```json
{
  "correo_verificado": true
}
```

Errores: 400 solicitud_invalida, 422 validacion, 429 demasiadas_peticiones

#### POST /api/v1/auth/reenviar-verificacion

- Auth: JWT
- Vuelve a mandar el correo de verificación. Máximo uno cada 60 s (429 con `Retry-After`).

Response 202:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 429 demasiadas_peticiones

#### PUT /api/v1/auth/password

- Auth: JWT
- Cambia la contraseña conociendo la actual. Cierra las otras sesiones; esta sigue abierta.

Request:

```json
{
  "password_actual": "Wherever2026",
  "password_nuevo": "NuevaClave2026"
}
```

Response 204:

```json
(sin cuerpo)
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion

### Usuarios, perfil y privacidad

Hoja A1. Todas las rutas de /usuarios/yo actúan sobre el usuario del token.

#### POST /api/v1/usuarios

- Auth: no
- Límite: 5/h por IP
- Crea la cuenta (rol consumidor, 50 puntos), manda el correo de verificación e inicia sesión.
- Mayor de 18 años según `fecha_nacimiento` (AAAA-MM-DD).
- `acepta_terminos` debe ser true.
- Nombre de 2 a 40 caracteres. `dispositivo` es opcional.
- Correo ya registrado = 409.

Request:

```json
{
  "nombre": "Ana López",
  "fecha_nacimiento": "1999-04-12",
  "correo": "ana@ejemplo.com",
  "password": "Wherever2026",
  "acepta_terminos": true,
  "version_terminos": "2026-10",
  "promociones_correo": false,
  "dispositivo": {
    "modelo": "Pixel 8",
    "sistema": "Android 15",
    "app_version": "0.1.0"
  }
}
```

Response 201:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi…",
  "refresh_token": "q8Zs3v1nX0r4bE7yTk2mW9pLhA6cUfGd",
  "expira_en": "2026-11-04T15:15:00Z",
  "usuario": {
    "id": "6728f0c1a4b2c3d4e5f60718",
    "nombre": "Ana López",
    "correo": "ana@ejemplo.com",
    "correo_verificado": false,
    "roles": [
      "consumidor"
    ]
  }
}
```

Errores: 400 solicitud_invalida, 409 conflicto, 422 validacion, 429 demasiadas_peticiones

#### GET /api/v1/usuarios/correo-disponible?correo=ana@ejemplo.com

- Auth: no
- Límite: 20/min por IP
- Dice si un correo ya está registrado, para validar el formulario antes de enviarlo.

Response 200:

```json
{
  "disponible": true
}
```

Errores: 400 solicitud_invalida, 422 validacion, 429 demasiadas_peticiones

#### GET /api/v1/usuarios/yo

- Auth: JWT
- Perfil completo del usuario. Trae `version` y la cabecera `ETag`; guarda la versión para editar.
- Perfil completo (mismo objeto que GET /usuarios/yo). `ciudad` y `gustos` pueden ser null; `horario_silencioso` también.

Response 200:

```json
{
  "id": "6728f0c1a4b2c3d4e5f60718",
  "nombre": "Ana López",
  "correo": "ana@ejemplo.com",
  "correo_verificado": true,
  "foto_url": "",
  "codigo_amigo": "K7M2Q9XA",
  "roles": [
    "consumidor"
  ],
  "modo_actual": "explorar",
  "ciudad": {
    "id": "66f1a2b3c4d5e6f708192a3b",
    "nombre": "Guadalajara"
  },
  "gustos": {
    "categorias": [
      "cafes",
      "museos",
      "parques"
    ],
    "presupuesto_min": 100,
    "presupuesto_max": 500,
    "restricciones": [
      "vegetariano"
    ],
    "alergias": "nueces"
  },
  "preferencias": {
    "movilidad_default": "a_pie",
    "tema": "sistema",
    "unidades": "km",
    "horario_silencioso": {
      "desde": "22:00",
      "hasta": "07:00"
    },
    "notificaciones": {
      "reservas": true,
      "cupones": true,
      "grupos": true,
      "resenas": true,
      "promociones": false
    }
  },
  "privacidad": {
    "ubicacion": true,
    "aprendizaje_personalizado": true,
    "estado_animo": false,
    "datos_uso": true,
    "buscable_por_telefono": false,
    "version_terminos": "2026-10",
    "fecha_aceptacion": "2026-11-03T18:20:00Z",
    "promociones_correo": false
  },
  "telefono_registrado": false,
  "puntos": 50,
  "nivel": "Explorador",
  "racha_semanas": 0,
  "estado": "activo",
  "eliminar_en": null,
  "version": 3,
  "creado_en": "2026-11-03T18:20:00Z",
  "actualizado_en": "2026-11-03T19:02:11Z"
}
```

Errores: 401 no_autenticado

resHeaders: {
  "ETag": "\"3\""
}

#### PATCH /api/v1/usuarios/yo

- Auth: JWT
- Edita nombre, foto o ciudad. Solo cambia lo que mandas.
- Sin `If-Match` = 400. Con una versión vieja = 409: recarga el perfil.
- `foto_url` debe empezar con https://. `ciudad_id` sale de /ciudades/autocompletar.
- Cabeceras: {
  "If-Match": "\"3\""
}

Request:

```json
{
  "nombre": "Ana L.",
  "foto_url": "https://cdn.ejemplo.com/ana.jpg",
  "ciudad_id": "66f1a2b3c4d5e6f708192a3b"
}
```

Response 200 (Perfil completo con la versión nueva.):

```json
{
  "id": "6728f0c1a4b2c3d4e5f60718",
  "nombre": "Ana L.",
  "correo": "ana@ejemplo.com",
  "correo_verificado": true,
  "foto_url": "https://cdn.ejemplo.com/ana.jpg",
  "codigo_amigo": "K7M2Q9XA",
  "roles": [
    "consumidor"
  ],
  "modo_actual": "explorar",
  "ciudad": {
    "id": "66f1a2b3c4d5e6f708192a3b",
    "nombre": "Guadalajara"
  },
  "gustos": {
    "categorias": [
      "cafes",
      "museos",
      "parques"
    ],
    "presupuesto_min": 100,
    "presupuesto_max": 500,
    "restricciones": [
      "vegetariano"
    ],
    "alergias": "nueces"
  },
  "preferencias": {
    "movilidad_default": "a_pie",
    "tema": "sistema",
    "unidades": "km",
    "horario_silencioso": {
      "desde": "22:00",
      "hasta": "07:00"
    },
    "notificaciones": {
      "reservas": true,
      "cupones": true,
      "grupos": true,
      "resenas": true,
      "promociones": false
    }
  },
  "privacidad": {
    "ubicacion": true,
    "aprendizaje_personalizado": true,
    "estado_animo": false,
    "datos_uso": true,
    "buscable_por_telefono": false,
    "version_terminos": "2026-10",
    "fecha_aceptacion": "2026-11-03T18:20:00Z",
    "promociones_correo": false
  },
  "telefono_registrado": false,
  "puntos": 50,
  "nivel": "Explorador",
  "racha_semanas": 0,
  "estado": "activo",
  "eliminar_en": null,
  "version": 4,
  "creado_en": "2026-11-03T18:20:00Z",
  "actualizado_en": "2026-11-03T19:02:11Z"
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 409 conflicto, 422 validacion

#### PUT /api/v1/usuarios/yo/correo

- Auth: JWT
- Cambia el correo. Pide la contraseña; el correo nuevo queda sin verificar y se manda su verificación.

Request:

```json
{
  "correo_nuevo": "ana.nuevo@ejemplo.com",
  "password": "Wherever2026"
}
```

Response 200 (Perfil completo.):

```json
{
  "id": "6728f0c1a4b2c3d4e5f60718",
  "nombre": "Ana López",
  "correo": "ana.nuevo@ejemplo.com",
  "correo_verificado": false,
  "foto_url": "",
  "codigo_amigo": "K7M2Q9XA",
  "roles": [
    "consumidor"
  ],
  "modo_actual": "explorar",
  "ciudad": {
    "id": "66f1a2b3c4d5e6f708192a3b",
    "nombre": "Guadalajara"
  },
  "gustos": {
    "categorias": [
      "cafes",
      "museos",
      "parques"
    ],
    "presupuesto_min": 100,
    "presupuesto_max": 500,
    "restricciones": [
      "vegetariano"
    ],
    "alergias": "nueces"
  },
  "preferencias": {
    "movilidad_default": "a_pie",
    "tema": "sistema",
    "unidades": "km",
    "horario_silencioso": {
      "desde": "22:00",
      "hasta": "07:00"
    },
    "notificaciones": {
      "reservas": true,
      "cupones": true,
      "grupos": true,
      "resenas": true,
      "promociones": false
    }
  },
  "privacidad": {
    "ubicacion": true,
    "aprendizaje_personalizado": true,
    "estado_animo": false,
    "datos_uso": true,
    "buscable_por_telefono": false,
    "version_terminos": "2026-10",
    "fecha_aceptacion": "2026-11-03T18:20:00Z",
    "promociones_correo": false
  },
  "telefono_registrado": false,
  "puntos": 50,
  "nivel": "Explorador",
  "racha_semanas": 0,
  "estado": "activo",
  "eliminar_en": null,
  "version": 4,
  "creado_en": "2026-11-03T18:20:00Z",
  "actualizado_en": "2026-11-03T19:02:11Z"
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 409 conflicto, 422 validacion

#### PUT /api/v1/usuarios/yo/gustos

- Auth: JWT consumidor
- Guarda los gustos del onboarding. Reemplaza los anteriores.
- `categorias`: de 3 a 10, sin repetir.
- `presupuesto_max` ≥ `presupuesto_min` (enteros, MXN).
- `restricciones`: hasta 10. `alergias`: texto de hasta 200.

Request:

```json
{
  "categorias": [
    "cafes",
    "museos",
    "parques"
  ],
  "presupuesto_min": 100,
  "presupuesto_max": 500,
  "restricciones": [
    "vegetariano"
  ],
  "alergias": "nueces"
}
```

Response 200:

```json
{
  "categorias": [
    "cafes",
    "museos",
    "parques"
  ],
  "presupuesto_min": 100,
  "presupuesto_max": 500,
  "restricciones": [
    "vegetariano"
  ],
  "alergias": "nueces"
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 403 prohibido, 422 validacion

#### PATCH /api/v1/usuarios/yo/preferencias

- Auth: JWT
- Ajustes de la app. Parcial: manda solo lo que cambia.
- `movilidad_default`: a_pie | bicicleta | auto | transporte_publico.
- `tema`: claro | oscuro | sistema. `unidades`: km | mi.
- Horas en formato HH:MM. `notificaciones.reservas: false` = 422 (no se pueden apagar).

Request:

```json
{
  "tema": "oscuro",
  "horario_silencioso": {
    "desde": "23:00",
    "hasta": "07:30"
  },
  "notificaciones": {
    "promociones": true
  }
}
```

Response 200:

```json
{
  "movilidad_default": "a_pie",
  "tema": "oscuro",
  "unidades": "km",
  "horario_silencioso": {
    "desde": "23:00",
    "hasta": "07:30"
  },
  "notificaciones": {
    "reservas": true,
    "cupones": true,
    "grupos": true,
    "resenas": true,
    "promociones": true
  }
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion

#### PATCH /api/v1/usuarios/yo/privacidad

- Auth: JWT
- Interruptores de privacidad y aceptación de términos. Parcial.

Request:

```json
{
  "estado_animo": true,
  "buscable_por_telefono": true
}
```

Response 200:

```json
{
  "ubicacion": true,
  "aprendizaje_personalizado": true,
  "estado_animo": true,
  "datos_uso": true,
  "buscable_por_telefono": true,
  "version_terminos": "2026-10",
  "fecha_aceptacion": "2026-11-03T18:20:00Z",
  "promociones_correo": false
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion

#### PUT /api/v1/usuarios/yo/telefono

- Auth: JWT
- Registra el teléfono. Nunca se devuelve en claro: solo `telefono_registrado`.
- Se quitan espacios, guiones, puntos y paréntesis; debe quedar en formato internacional (+ y lada), p. ej. +5215512345678.
- Teléfono de otra cuenta = 409.

Request:

```json
{
  "telefono": "+52 1 55 1234 5678"
}
```

Response 200:

```json
{
  "telefono_registrado": true
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 409 conflicto, 422 validacion

#### POST /api/v1/usuarios/yo/modo

- Auth: JWT
- Cambia el modo con el que abre la app.
- `modo`: explorar | negocio. Negocio sin rol comerciante = 403.

Request:

```json
{
  "modo": "explorar"
}
```

Response 200:

```json
{
  "modo_actual": "explorar"
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 403 prohibido, 422 validacion

#### POST /api/v1/usuarios/yo/exportar

- Auth: JWT
- Pide la copia de "mis datos". Se arma de noche y llega un correo con enlace de 24 h.
- Una por día; si no, 429 con `Retry-After`.

Response 202:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 429 demasiadas_peticiones

#### POST /api/v1/usuarios/exportaciones/descargar

- Auth: token del enlace
- Límite: 10/min por IP
- Descarga la copia con el token del correo. Un solo uso: después de descargarla se borra.

Request:

```json
{
  "token": "token-del-fragmento-del-enlace"
}
```

Response 200 (Un objeto por colección del usuario; con más módulos aparecen más claves.):

```json
{
  "usuarios": {
    "id": "6728f0c1a4b2c3d4e5f60718",
    "nombre": "Ana López",
    "correo": "ana@ejemplo.com",
    "correo_verificado": true,
    "foto_url": "",
    "codigo_amigo": "K7M2Q9XA",
    "roles": [
      "consumidor"
    ],
    "modo_actual": "explorar",
    "ciudad": {
      "id": "66f1a2b3c4d5e6f708192a3b",
      "nombre": "Guadalajara"
    },
    "gustos": {
      "categorias": [
        "cafes",
        "museos",
        "parques"
      ],
      "presupuesto_min": 100,
      "presupuesto_max": 500,
      "restricciones": [
        "vegetariano"
      ],
      "alergias": "nueces"
    },
    "preferencias": {
      "movilidad_default": "a_pie",
      "tema": "sistema",
      "unidades": "km",
      "horario_silencioso": {
        "desde": "22:00",
        "hasta": "07:00"
      },
      "notificaciones": {
        "reservas": true,
        "cupones": true,
        "grupos": true,
        "resenas": true,
        "promociones": false
      }
    },
    "privacidad": {
      "ubicacion": true,
      "aprendizaje_personalizado": true,
      "estado_animo": false,
      "datos_uso": true,
      "buscable_por_telefono": false,
      "version_terminos": "2026-10",
      "fecha_aceptacion": "2026-11-03T18:20:00Z",
      "promociones_correo": false
    },
    "telefono_registrado": false,
    "puntos": 50,
    "nivel": "Explorador",
    "racha_semanas": 0,
    "estado": "activo",
    "eliminar_en": null,
    "version": 3,
    "creado_en": "2026-11-03T18:20:00Z",
    "actualizado_en": "2026-11-03T19:02:11Z"
  },
  "sesiones": [
    {
      "creado_en": "2026-11-03T18:20:00Z",
      "dispositivo": {
        "modelo": "Pixel 8",
        "sistema": "Android 15",
        "app_version": "0.1.0"
      },
      "ip": "200.79.176.0",
      "metodo": "correo",
      "ultimo_uso": "2026-11-03T19:05:00Z"
    }
  ]
}
```

Errores: 400 solicitud_invalida, 410 vencido, 422 validacion, 429 demasiadas_peticiones

#### DELETE /api/v1/usuarios/yo/historial

- Auth: JWT
- Borra el historial de todos los módulos. La cuenta se conserva.

Response 204:

```json
(sin cuerpo)
```

Errores: 401 no_autenticado, 503 no_disponible

#### DELETE /api/v1/usuarios/yo

- Auth: JWT
- Programa la eliminación de la cuenta a 30 días y cierra todas las sesiones.
- `confirmacion` debe ser exactamente ELIMINAR.

Request:

```json
{
  "confirmacion": "ELIMINAR"
}
```

Response 202:

```json
{
  "eliminar_en": "2026-12-03T19:10:00Z"
}
```

Errores: 400 solicitud_invalida, 401 no_autenticado, 422 validacion

#### POST /api/v1/usuarios/yo/cancelar-eliminacion

- Auth: JWT
- Cancela la eliminación pendiente. Fuera de plazo o sin eliminación pendiente = 409.

Response 200 (Perfil completo, otra vez con `estado: "activo"` y `eliminar_en: null`.):

```json
{
  "id": "6728f0c1a4b2c3d4e5f60718",
  "nombre": "Ana López",
  "correo": "ana@ejemplo.com",
  "correo_verificado": true,
  "foto_url": "",
  "codigo_amigo": "K7M2Q9XA",
  "roles": [
    "consumidor"
  ],
  "modo_actual": "explorar",
  "ciudad": {
    "id": "66f1a2b3c4d5e6f708192a3b",
    "nombre": "Guadalajara"
  },
  "gustos": {
    "categorias": [
      "cafes",
      "museos",
      "parques"
    ],
    "presupuesto_min": 100,
    "presupuesto_max": 500,
    "restricciones": [
      "vegetariano"
    ],
    "alergias": "nueces"
  },
  "preferencias": {
    "movilidad_default": "a_pie",
    "tema": "sistema",
    "unidades": "km",
    "horario_silencioso": {
      "desde": "22:00",
      "hasta": "07:00"
    },
    "notificaciones": {
      "reservas": true,
      "cupones": true,
      "grupos": true,
      "resenas": true,
      "promociones": false
    }
  },
  "privacidad": {
    "ubicacion": true,
    "aprendizaje_personalizado": true,
    "estado_animo": false,
    "datos_uso": true,
    "buscable_por_telefono": false,
    "version_terminos": "2026-10",
    "fecha_aceptacion": "2026-11-03T18:20:00Z",
    "promociones_correo": false
  },
  "telefono_registrado": false,
  "puntos": 50,
  "nivel": "Explorador",
  "racha_semanas": 0,
  "estado": "activo",
  "eliminar_en": null,
  "version": 3,
  "creado_en": "2026-11-03T18:20:00Z",
  "actualizado_en": "2026-11-03T19:02:11Z"
}
```

Errores: 401 no_autenticado, 409 conflicto

### Ciudades

Catálogo para el selector de ciudad del perfil.

#### GET /api/v1/ciudades/autocompletar?q=guada&limite=8

- Auth: no
- Sugiere ciudades mientras se escribe. Primero salen las que tienen cobertura.
- `q`: 2 letras o más; acentos y mayúsculas dan igual.
- `limite`: 8 por defecto, máximo 50.

Response 200:

```json
[
  {
    "id": "66f1a2b3c4d5e6f708192a3b",
    "nombre": "Guadalajara",
    "estado": "Jalisco"
  },
  {
    "id": "66f1a2b3c4d5e6f708192a3c",
    "nombre": "Guadalupe",
    "estado": "Nuevo León"
  }
]
```

Errores: 400 solicitud_invalida, 422 validacion
