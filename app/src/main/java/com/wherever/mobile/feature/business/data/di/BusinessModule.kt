package com.wherever.mobile.feature.business.data.di

import com.wherever.mobile.feature.business.data.BusinessCouponsApi
import com.wherever.mobile.feature.business.data.BusinessCouponsRepositoryImpl
import com.wherever.mobile.feature.business.data.BusinessHomeApi
import com.wherever.mobile.feature.business.data.BusinessHomeRepositoryImpl
import com.wherever.mobile.feature.business.data.BusinessOnboardingApi
import com.wherever.mobile.feature.business.data.BusinessOnboardingRepositoryImpl
import com.wherever.mobile.feature.business.data.BusinessProfileApi
import com.wherever.mobile.feature.business.data.BusinessProfileRepositoryImpl
import com.wherever.mobile.feature.business.data.BusinessReviewsApi
import com.wherever.mobile.feature.business.data.BusinessReviewsRepositoryImpl
import com.wherever.mobile.feature.business.data.MediaStoreReportSaver
import com.wherever.mobile.feature.business.data.ReportSaver
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
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
    abstract fun bindCoupons(impl: BusinessCouponsRepositoryImpl): BusinessCouponsRepository

    @Binds
    abstract fun bindReviews(impl: BusinessReviewsRepositoryImpl): BusinessReviewsRepository

    @Binds
    abstract fun bindProfile(impl: BusinessProfileRepositoryImpl): BusinessProfileRepository

    @Binds
    abstract fun bindReportSaver(impl: MediaStoreReportSaver): ReportSaver

    companion object {
        @Provides
        fun provideOnboardingApi(retrofit: Retrofit): BusinessOnboardingApi =
            retrofit.create(BusinessOnboardingApi::class.java)

        @Provides
        fun provideCouponsApi(retrofit: Retrofit): BusinessCouponsApi = retrofit.create(BusinessCouponsApi::class.java)

        @Provides
        fun provideReviewsApi(retrofit: Retrofit): BusinessReviewsApi = retrofit.create(BusinessReviewsApi::class.java)

        @Provides
        fun provideProfileApi(retrofit: Retrofit): BusinessProfileApi = retrofit.create(BusinessProfileApi::class.java)

        @Provides
        fun provideHomeApi(retrofit: Retrofit): BusinessHomeApi = retrofit.create(BusinessHomeApi::class.java)
    }
}
