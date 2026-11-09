package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.network.PublicEndpoint
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsersApi {
    @GET("usuarios/yo")
    suspend fun getMe(): UserProfileDto

    @PATCH("usuarios/yo")
    suspend fun patchMe(@Header("If-Match") ifMatch: String, @Body body: ProfilePatchDto): UserProfileDto

    @PUT("usuarios/yo/gustos")
    suspend fun putTastes(@Body body: TastesDto): TastesDto

    @PATCH("usuarios/yo/preferencias")
    suspend fun patchPreferences(@Body body: PreferencesPatchDto): PreferencesDto

    @PATCH("usuarios/yo/privacidad")
    suspend fun patchPrivacy(@Body body: PrivacyPatchDto): PrivacyDto

    @HTTP(method = "DELETE", path = "usuarios/yo", hasBody = true)
    suspend fun deleteMe(@Body body: DeleteAccountDto): DeleteAccountResponseDto

    @POST("usuarios/yo/cancelar-eliminacion")
    suspend fun cancelDeletion(): UserProfileDto

    @PublicEndpoint
    @GET("ciudades/autocompletar")
    suspend fun autocompleteCities(@Query("q") query: String, @Query("limite") limit: Int): List<CitySuggestionDto>
}
