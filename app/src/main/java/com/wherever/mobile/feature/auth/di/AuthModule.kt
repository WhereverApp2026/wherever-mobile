package com.wherever.mobile.feature.auth.di

import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.network.TokenRefresher
import com.wherever.mobile.feature.auth.data.AndroidDeviceInfoProvider
import com.wherever.mobile.feature.auth.data.AuthApi
import com.wherever.mobile.feature.auth.data.DeviceInfoProvider
import com.wherever.mobile.feature.auth.data.RefreshApi
import com.wherever.mobile.feature.auth.data.RemoteTokenRefresher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    @Named("public")
    fun providePublicRetrofit(json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideRefreshApi(@Named("public") retrofit: Retrofit): RefreshApi = retrofit.create(RefreshApi::class.java)

    @Provides
    @Singleton
    fun provideDeviceInfoProvider(provider: AndroidDeviceInfoProvider): DeviceInfoProvider = provider

    @Provides
    @Singleton
    fun provideTokenRefresher(api: RefreshApi): TokenRefresher = RemoteTokenRefresher(api)
}
