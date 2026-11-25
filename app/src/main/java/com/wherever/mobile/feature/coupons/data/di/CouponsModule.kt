package com.wherever.mobile.feature.coupons.data.di

import com.wherever.mobile.feature.coupons.data.CouponRepositoryImpl
import com.wherever.mobile.feature.coupons.data.CouponsApi
import com.wherever.mobile.feature.coupons.domain.CouponRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class CouponsModule {
    @Binds
    abstract fun bindRepository(impl: CouponRepositoryImpl): CouponRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): CouponsApi = retrofit.create(CouponsApi::class.java)
    }
}
