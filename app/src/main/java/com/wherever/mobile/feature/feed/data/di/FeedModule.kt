package com.wherever.mobile.feature.feed.data.di

import com.wherever.mobile.feature.feed.data.FeedApi
import com.wherever.mobile.feature.feed.data.FeedRepositoryImpl
import com.wherever.mobile.feature.feed.data.MomentSocialApi
import com.wherever.mobile.feature.feed.data.MomentSocialRepositoryImpl
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.feed.domain.MomentSocialRepository
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

    @Binds
    abstract fun bindSocial(impl: MomentSocialRepositoryImpl): MomentSocialRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): FeedApi = retrofit.create(FeedApi::class.java)

        @Provides
        fun provideSocialApi(retrofit: Retrofit): MomentSocialApi = retrofit.create(MomentSocialApi::class.java)
    }
}
