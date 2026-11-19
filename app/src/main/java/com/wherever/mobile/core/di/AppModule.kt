package com.wherever.mobile.core.di

import android.content.Context
import androidx.room.Room
import com.wherever.mobile.core.common.DefaultDispatcherProvider
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.database.MIGRATION_1_2
import com.wherever.mobile.core.database.WhereverDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDispatchers(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(dispatchers: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.io)

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WhereverDatabase =
        Room.databaseBuilder(context, WhereverDatabase::class.java, "wherever.db")
            .addMigrations(MIGRATION_1_2)
            .build()
}
