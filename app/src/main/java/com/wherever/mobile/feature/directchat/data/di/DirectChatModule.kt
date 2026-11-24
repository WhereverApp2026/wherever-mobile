package com.wherever.mobile.feature.directchat.data.di

import com.wherever.mobile.feature.directchat.data.DirectChatApi
import com.wherever.mobile.feature.directchat.data.DirectChatRepositoryImpl
import com.wherever.mobile.feature.directchat.domain.DirectChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class DirectChatModule {
    @Binds
    abstract fun bindRepository(impl: DirectChatRepositoryImpl): DirectChatRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): DirectChatApi = retrofit.create(DirectChatApi::class.java)
    }
}
