package com.wherever.mobile.feature.reservations.data.di

import com.wherever.mobile.feature.reservations.data.ReservationsApi
import com.wherever.mobile.feature.reservations.data.ReservationsRepositoryImpl
import com.wherever.mobile.feature.reservations.domain.ReservationsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ReservationsModule {
    @Binds
    abstract fun bindRepository(impl: ReservationsRepositoryImpl): ReservationsRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): ReservationsApi = retrofit.create(ReservationsApi::class.java)
    }
}
