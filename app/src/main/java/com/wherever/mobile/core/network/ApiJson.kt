package com.wherever.mobile.core.network

import kotlinx.serialization.json.Json

/** La API agrega campos con el tiempo y varios pueden ser null. */
val ApiJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
