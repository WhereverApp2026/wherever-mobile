package com.wherever.mobile.core.di

import com.wherever.mobile.core.notifications.DeviceRegistration
import com.wherever.mobile.core.notifications.DeviceRegistrationImpl
import com.wherever.mobile.core.notifications.FirebasePushTokenSource
import com.wherever.mobile.core.notifications.PushTokenSource
import com.wherever.mobile.core.notifications.data.DevicesApi
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
    abstract fun bindRegistration(impl: DeviceRegistrationImpl): DeviceRegistration

    @Binds
    abstract fun bindTokenSource(impl: FirebasePushTokenSource): PushTokenSource

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): DevicesApi = retrofit.create(DevicesApi::class.java)
    }
}
