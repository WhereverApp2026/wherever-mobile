package com.wherever.mobile.feature.places.data.di

import com.wherever.mobile.feature.places.data.FavoritesApi
import com.wherever.mobile.feature.places.data.FavoritesRepositoryImpl
import com.wherever.mobile.feature.places.data.PlacesApi
import com.wherever.mobile.feature.places.data.PlacesRepositoryImpl
import com.wherever.mobile.feature.places.data.ReviewsRepositoryImpl
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.ReviewsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesModule {
    @Binds
    abstract fun bindRepository(impl: PlacesRepositoryImpl): PlacesRepository

    @Binds
    abstract fun bindReviews(impl: ReviewsRepositoryImpl): ReviewsRepository

    @Binds
    abstract fun bindFavorites(impl: FavoritesRepositoryImpl): FavoritesRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): PlacesApi = retrofit.create(PlacesApi::class.java)

        @Provides
        fun provideFavoritesApi(retrofit: Retrofit): FavoritesApi = retrofit.create(FavoritesApi::class.java)
    }
}
