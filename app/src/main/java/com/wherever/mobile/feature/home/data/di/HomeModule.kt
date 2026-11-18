package com.wherever.mobile.feature.home.data.di

import com.wherever.mobile.feature.home.data.HomeApi
import com.wherever.mobile.feature.home.data.HomeRepositoryImpl
import com.wherever.mobile.feature.home.domain.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun bindRepository(impl: HomeRepositoryImpl): HomeRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)
    }
}
