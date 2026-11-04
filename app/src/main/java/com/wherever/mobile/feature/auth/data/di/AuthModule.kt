package com.wherever.mobile.feature.auth.data.di

import android.os.Build
import com.wherever.mobile.BuildConfig
import com.wherever.mobile.feature.auth.data.AuthApi
import com.wherever.mobile.feature.auth.data.AuthRepositoryImpl
import com.wherever.mobile.feature.auth.data.SharedOnboardingPreferences
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.DeviceInfo
import com.wherever.mobile.feature.auth.domain.DeviceInfoProvider
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindOnboarding(impl: SharedOnboardingPreferences): OnboardingPreferences

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

        @Provides
        fun provideDeviceInfo(): DeviceInfoProvider = DeviceInfoProvider {
            DeviceInfo(Build.MODEL, "Android ${Build.VERSION.RELEASE}", BuildConfig.VERSION_NAME)
        }
    }
}
