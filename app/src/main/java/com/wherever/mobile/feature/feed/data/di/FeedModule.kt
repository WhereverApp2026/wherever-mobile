package com.wherever.mobile.feature.feed.data.di

import com.wherever.mobile.feature.feed.data.FeedApi
import com.wherever.mobile.feature.feed.data.FeedRepositoryImpl
import com.wherever.mobile.feature.feed.domain.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {
    @Binds
    abstract fun bindRepository(impl: FeedRepositoryImpl): FeedRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): FeedApi = retrofit.create(FeedApi::class.java)
    }
}
