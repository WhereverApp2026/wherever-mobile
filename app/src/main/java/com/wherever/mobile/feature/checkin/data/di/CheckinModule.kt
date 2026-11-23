package com.wherever.mobile.feature.checkin.data.di

import com.wherever.mobile.feature.checkin.data.CheckinApi
import com.wherever.mobile.feature.checkin.data.CheckinRepositoryImpl
import com.wherever.mobile.feature.checkin.domain.CheckinRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class CheckinModule {
    @Binds
    abstract fun bindRepository(impl: CheckinRepositoryImpl): CheckinRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): CheckinApi = retrofit.create(CheckinApi::class.java)
    }
}
