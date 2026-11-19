package com.wherever.mobile.feature.friends.data.di

import com.wherever.mobile.feature.friends.data.FriendsApi
import com.wherever.mobile.feature.friends.data.FriendsRepositoryImpl
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class FriendsModule {
    @Binds
    abstract fun bindRepository(impl: FriendsRepositoryImpl): FriendsRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): FriendsApi = retrofit.create(FriendsApi::class.java)
    }
}
