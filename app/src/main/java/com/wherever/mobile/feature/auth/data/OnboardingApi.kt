package com.wherever.mobile.feature.auth.data

import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface OnboardingApi {

    @GET("me")
    suspend fun me(): Response<JsonElement>

    @PUT("me/preferences")
    suspend fun savePreferences(@Body request: PreferencesRequestDto): Response<ResponseBody>

    @POST("me/onboarding/complete")
    suspend fun complete(): Response<ResponseBody>

    @GET("friend-suggestions")
    suspend fun suggestions(@Query("limit") limit: Int = 20): Response<JsonElement>

    @POST("friend-requests")
    suspend fun sendFriendRequest(@Body request: FriendRequestDto): Response<ResponseBody>
}
