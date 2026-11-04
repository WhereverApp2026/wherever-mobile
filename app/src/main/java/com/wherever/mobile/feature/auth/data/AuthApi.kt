package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<JsonElement>

    @POST("usuarios")
    suspend fun register(@Body request: RegisterRequestDto): Response<JsonElement>

    @POST("auth/recuperar")
    suspend fun requestRecovery(@Body request: RecoverRequestDto): Response<ResponseBody>

    @POST("auth/verificar-correo")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): Response<ResponseBody>

    @POST("auth/restablecer")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<ResponseBody>

    @GET("auth/sesiones")
    suspend fun sessions(): Response<JsonElement>

    @POST("auth/logout-todos")
    suspend fun logoutAll(): Response<ResponseBody>
}

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): Response<JsonElement>
}
