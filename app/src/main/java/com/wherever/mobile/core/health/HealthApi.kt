package com.wherever.mobile.core.health

import retrofit2.Response
import retrofit2.http.GET

interface HealthApi {
    @GET("/health")
    suspend fun health(): Response<Void>

    @GET("/ready")
    suspend fun ready(): Response<Void>
}
