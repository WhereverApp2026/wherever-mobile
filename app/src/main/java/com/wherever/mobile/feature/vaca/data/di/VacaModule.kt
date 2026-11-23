package com.wherever.mobile.feature.vaca.data.di

import com.wherever.mobile.feature.vaca.data.VacaApi
import com.wherever.mobile.feature.vaca.data.VacaRepositoryImpl
import com.wherever.mobile.feature.vaca.domain.VacaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class VacaModule {
    @Binds
    abstract fun bindRepository(impl: VacaRepositoryImpl): VacaRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): VacaApi = retrofit.create(VacaApi::class.java)
    }
}
