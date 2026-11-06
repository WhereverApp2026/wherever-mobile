package com.wherever.mobile.core.di

import android.content.Context
import androidx.room.Room
import com.wherever.mobile.core.common.DefaultDispatcherProvider
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.database.WhereverDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDispatchers(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WhereverDatabase =
        Room.databaseBuilder(context, WhereverDatabase::class.java, "wherever.db").build()
}
