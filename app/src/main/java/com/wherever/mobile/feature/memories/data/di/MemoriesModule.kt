package com.wherever.mobile.feature.memories.data.di

import com.wherever.mobile.feature.memories.data.AndroidPostcardGallery
import com.wherever.mobile.feature.memories.data.AndroidPostcardRenderer
import com.wherever.mobile.feature.memories.data.MemoriesApi
import com.wherever.mobile.feature.memories.data.MemoriesRepositoryImpl
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.PostcardGallery
import com.wherever.mobile.feature.memories.domain.PostcardRenderer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoriesModule {
    @Binds
    abstract fun bindRepository(impl: MemoriesRepositoryImpl): MemoriesRepository

    @Binds
    abstract fun bindRenderer(impl: AndroidPostcardRenderer): PostcardRenderer

    @Binds
    abstract fun bindGallery(impl: AndroidPostcardGallery): PostcardGallery

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): MemoriesApi = retrofit.create(MemoriesApi::class.java)
    }
}
