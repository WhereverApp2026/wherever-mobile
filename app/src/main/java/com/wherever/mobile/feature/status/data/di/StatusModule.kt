package com.wherever.mobile.feature.status.data.di

import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.di.AppVersion
import com.wherever.mobile.core.network.AndroidConnectivityMonitor
import com.wherever.mobile.core.network.ConnectivityMonitor
import com.wherever.mobile.feature.status.data.AppStatusRepositoryImpl
import com.wherever.mobile.feature.status.data.StatusApi
import com.wherever.mobile.feature.status.domain.AppStatusRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class StatusModule {
    @Binds
    abstract fun bindRepository(impl: AppStatusRepositoryImpl): AppStatusRepository

    @Binds
    abstract fun bindConnectivity(impl: AndroidConnectivityMonitor): ConnectivityMonitor

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): StatusApi = retrofit.create(StatusApi::class.java)

        @Provides
        @AppVersion
        fun provideVersion(): String = BuildConfig.VERSION_NAME
    }
}
