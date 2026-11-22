package com.wherever.mobile.feature.reviews.data.di

import com.wherever.mobile.feature.reviews.data.ReviewRepositoryImpl
import com.wherever.mobile.feature.reviews.data.ReviewsApi
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewsModule {
    @Binds
    abstract fun bindRepository(impl: ReviewRepositoryImpl): ReviewRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): ReviewsApi = retrofit.create(ReviewsApi::class.java)
    }
}
