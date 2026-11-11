package com.wherever.mobile.core.di

import android.util.Log
import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.core.network.AuthHeaderInterceptor
import com.wherever.mobile.core.network.RedactedLoggingInterceptor
import com.wherever.mobile.core.network.RefreshApi
import com.wherever.mobile.core.network.RequestIdInterceptor
import com.wherever.mobile.core.network.SessionEvents
import com.wherever.mobile.core.network.TokenRefreshAuthenticator
import com.wherever.mobile.core.security.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Datos móviles: el primer handshake con CloudFront puede tardar varios segundos (specs/nv-40).
internal const val CONNECT_TIMEOUT_SECONDS = 15L
internal const val IO_TIMEOUT_SECONDS = 30L
private const val LOG_TAG = "WhereverNet"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private fun OkHttpClient.Builder.baseConfig(): OkHttpClient.Builder = this
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    private fun OkHttpClient.Builder.debugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) addInterceptor(RedactedLoggingInterceptor { Log.d(LOG_TAG, it) })
    }

    private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
        .build()

    /** Sin AuthHeader ni Authenticator: evita recursión al renovar. */
    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshClient(): OkHttpClient = OkHttpClient.Builder()
        .baseConfig()
        .addInterceptor(RequestIdInterceptor())
        .debugLogging()
        .build()

    @Provides
    @Singleton
    fun provideRefreshApi(@RefreshClient client: OkHttpClient): RefreshApi =
        retrofit(client).create(RefreshApi::class.java)

    /** Orden fijo de la constitución §4: RequestId → AuthHeader → RedactedLogging. */
    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideClient(tokenStore: TokenStore, refreshApi: RefreshApi, sessionEvents: SessionEvents): OkHttpClient =
        OkHttpClient.Builder()
            .baseConfig()
            .addInterceptor(RequestIdInterceptor())
            .addInterceptor(AuthHeaderInterceptor(tokenStore))
            .debugLogging()
            .authenticator(TokenRefreshAuthenticator(tokenStore, refreshApi, sessionEvents))
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(@AuthenticatedClient client: OkHttpClient): Retrofit = retrofit(client)
}
