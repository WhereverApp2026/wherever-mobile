package com.wherever.mobile.feature.account.data.di

import com.wherever.mobile.feature.account.data.AccountApi
import com.wherever.mobile.feature.account.data.AccountRepositoryImpl
import com.wherever.mobile.feature.account.domain.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {
    @Binds
    abstract fun bindRepository(impl: AccountRepositoryImpl): AccountRepository

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): AccountApi = retrofit.create(AccountApi::class.java)
    }
}
