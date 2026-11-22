package com.wherever.mobile.feature.business.data.di

import com.wherever.mobile.feature.business.data.BusinessOnboardingApi
import com.wherever.mobile.feature.business.data.BusinessOnboardingRepositoryImpl
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class BusinessModule {
    @Binds
    abstract fun bindOnboarding(impl: BusinessOnboardingRepositoryImpl): BusinessOnboardingRepository

    companion object {
        @Provides
        fun provideOnboardingApi(retrofit: Retrofit): BusinessOnboardingApi =
            retrofit.create(BusinessOnboardingApi::class.java)
    }
}
