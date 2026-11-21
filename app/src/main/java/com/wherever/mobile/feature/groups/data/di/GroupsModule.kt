package com.wherever.mobile.feature.groups.data.di

import com.wherever.mobile.core.common.SessionCleaner
import com.wherever.mobile.core.database.WhereverDatabase
import com.wherever.mobile.feature.groups.data.GroupMessageDao
import com.wherever.mobile.feature.groups.data.GroupMessagesCleaner
import com.wherever.mobile.feature.groups.data.GroupsApi
import com.wherever.mobile.feature.groups.data.GroupsRepositoryImpl
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupsModule {
    @Binds
    abstract fun bindRepository(impl: GroupsRepositoryImpl): GroupsRepository

    @Binds
    @IntoSet
    abstract fun bindCleaner(impl: GroupMessagesCleaner): SessionCleaner

    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): GroupsApi = retrofit.create(GroupsApi::class.java)

        @Provides
        fun provideMessageDao(database: WhereverDatabase): GroupMessageDao = database.groupMessageDao()
    }
}
