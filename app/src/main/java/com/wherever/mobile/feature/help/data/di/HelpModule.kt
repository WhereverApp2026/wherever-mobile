package com.wherever.mobile.feature.help.data.di

import com.wherever.mobile.feature.help.data.HelpApi
import com.wherever.mobile.feature.help.data.HelpRepositoryImpl
import com.wherever.mobile.feature.help.domain.HelpRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HelpModule {
    @Binds
    abstract fun bindRepository(impl: HelpRepositoryImpl): HelpRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): HelpApi = retrofit.create(HelpApi::class.java)
    }
}
