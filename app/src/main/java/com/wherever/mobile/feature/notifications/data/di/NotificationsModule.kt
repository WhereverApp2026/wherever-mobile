package com.wherever.mobile.feature.notifications.data.di

import com.wherever.mobile.feature.notifications.data.NotificationsApi
import com.wherever.mobile.feature.notifications.data.NotificationsRepositoryImpl
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    abstract fun bindRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): NotificationsApi = retrofit.create(NotificationsApi::class.java)
    }
}
