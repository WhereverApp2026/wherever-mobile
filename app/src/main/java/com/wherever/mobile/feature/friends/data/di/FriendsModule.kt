package com.wherever.mobile.feature.friends.data.di

import com.wherever.mobile.feature.friends.data.AndroidContactsReader
import com.wherever.mobile.feature.friends.data.ContactMatchRepositoryImpl
import com.wherever.mobile.feature.friends.data.ContactsApi
import com.wherever.mobile.feature.friends.data.FriendsApi
import com.wherever.mobile.feature.friends.data.FriendsRepositoryImpl
import com.wherever.mobile.feature.friends.data.QrImageRepositoryImpl
import com.wherever.mobile.feature.friends.domain.ContactMatchRepository
import com.wherever.mobile.feature.friends.domain.ContactsReader
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.QrImageRepository
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

    @Binds
    abstract fun bindContactMatches(impl: ContactMatchRepositoryImpl): ContactMatchRepository

    @Binds
    abstract fun bindContactsReader(impl: AndroidContactsReader): ContactsReader

    @Binds
    abstract fun bindQrImages(impl: QrImageRepositoryImpl): QrImageRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): FriendsApi = retrofit.create(FriendsApi::class.java)

        @Provides
        fun provideContactsApi(retrofit: Retrofit): ContactsApi = retrofit.create(ContactsApi::class.java)
    }
}
