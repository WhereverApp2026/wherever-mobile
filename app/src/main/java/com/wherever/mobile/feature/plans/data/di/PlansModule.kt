package com.wherever.mobile.feature.plans.data.di

import com.wherever.mobile.feature.plans.data.PlanAssistantRepositoryImpl
import com.wherever.mobile.feature.plans.data.PlansApi
import com.wherever.mobile.feature.plans.data.PlansRepositoryImpl
import com.wherever.mobile.feature.plans.domain.PlanAssistantRepository
import com.wherever.mobile.feature.plans.domain.PlansRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PlansModule {
    @Binds
    abstract fun bindAssistant(impl: PlanAssistantRepositoryImpl): PlanAssistantRepository

    /** Singleton: comparte el caché de nombres de lugares entre pantallas. */
    @Binds
    @Singleton
    abstract fun bindPlans(impl: PlansRepositoryImpl): PlansRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): PlansApi = retrofit.create(PlansApi::class.java)
    }
}
