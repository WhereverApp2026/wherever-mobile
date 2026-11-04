package com.wherever.mobile.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WhereverDatabase = Room.databaseBuilder(
        context,
        WhereverDatabase::class.java,
        "wherever.db"
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideAppMetadataDao(database: WhereverDatabase): AppMetadataDao = database.appMetadataDao()

    @Provides
    fun provideOnboardingDao(database: WhereverDatabase): OnboardingDao = database.onboardingDao()
}
