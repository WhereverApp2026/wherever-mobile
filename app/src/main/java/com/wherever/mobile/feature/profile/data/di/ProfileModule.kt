package com.wherever.mobile.feature.profile.data.di

import com.wherever.mobile.core.database.WhereverDatabase
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UserProfileDao
import com.wherever.mobile.feature.profile.data.UsersApi
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @Binds
    abstract fun bindRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)

        @Provides
        fun provideDao(database: WhereverDatabase): UserProfileDao = database.userProfileDao()
    }
}
