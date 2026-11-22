package com.wherever.mobile.feature.business.data.di

import com.wherever.mobile.feature.business.data.BusinessHomeApi
import com.wherever.mobile.feature.business.data.BusinessHomeRepositoryImpl
import com.wherever.mobile.feature.business.data.BusinessOnboardingApi
import com.wherever.mobile.feature.business.data.BusinessOnboardingRepositoryImpl
import com.wherever.mobile.feature.business.data.MediaStoreReportSaver
import com.wherever.mobile.feature.business.data.ReportSaver
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
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

    @Binds
    abstract fun bindHome(impl: BusinessHomeRepositoryImpl): BusinessHomeRepository

    @Binds
    abstract fun bindReportSaver(impl: MediaStoreReportSaver): ReportSaver

    companion object {
        @Provides
        fun provideOnboardingApi(retrofit: Retrofit): BusinessOnboardingApi =
            retrofit.create(BusinessOnboardingApi::class.java)

        @Provides
        fun provideHomeApi(retrofit: Retrofit): BusinessHomeApi = retrofit.create(BusinessHomeApi::class.java)
    }
}
