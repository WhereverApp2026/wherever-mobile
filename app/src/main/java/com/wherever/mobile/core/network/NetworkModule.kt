package com.wherever.mobile.core.network

import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.health.HealthApi
import com.wherever.mobile.core.security.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideRequestIdInterceptor(): RequestIdInterceptor = RequestIdInterceptor()

    @Provides
    @Singleton
    fun provideAuthHeaderInterceptor(tokenStore: TokenStore): AuthHeaderInterceptor = AuthHeaderInterceptor(tokenStore)

    @Provides
    @Singleton
    fun provideHttpLogger(logger: AndroidHttpLogger): HttpLogger = logger

    @Provides
    @Singleton
    fun provideRedactedLoggingInterceptor(logger: HttpLogger): RedactedLoggingInterceptor = RedactedLoggingInterceptor(logger)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(tokenStore: TokenStore, refresher: TokenRefresher): TokenAuthenticator =
        TokenAuthenticator(tokenStore = tokenStore, refresher = refresher)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        requestIdInterceptor: RequestIdInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        redactedLoggingInterceptor: RedactedLoggingInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(requestIdInterceptor)
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(redactedLoggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
