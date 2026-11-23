package com.wherever.mobile.feature.groups.data.di

import com.wherever.mobile.core.common.SessionCleaner
import com.wherever.mobile.core.database.WhereverDatabase
import com.wherever.mobile.feature.groups.data.GroupMembersRepositoryImpl
import com.wherever.mobile.feature.groups.data.GroupMessageDao
import com.wherever.mobile.feature.groups.data.GroupMessagesCleaner
import com.wherever.mobile.feature.groups.data.GroupsApi
import com.wherever.mobile.feature.groups.data.GroupsRepositoryImpl
import com.wherever.mobile.feature.groups.data.VotesRepositoryImpl
import com.wherever.mobile.feature.groups.domain.GroupMembersRepository
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.VotesRepository
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
    abstract fun bindMembers(impl: GroupMembersRepositoryImpl): GroupMembersRepository

    @Binds
    abstract fun bindVotes(impl: VotesRepositoryImpl): VotesRepository

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
