package com.wherever.mobile.feature.photos.data.di

import com.wherever.mobile.core.media.AndroidImageCompressor
import com.wherever.mobile.core.media.ImageCompressor
import com.wherever.mobile.feature.photos.data.ImagesApi
import com.wherever.mobile.feature.photos.data.PhotoRepositoryImpl
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotosModule {
    @Binds
    abstract fun bindRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    abstract fun bindCompressor(impl: AndroidImageCompressor): ImageCompressor

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): ImagesApi = retrofit.create(ImagesApi::class.java)
    }
}
