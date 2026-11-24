package com.wherever.mobile.core.di

import android.content.Context
import androidx.room.Room
import com.wherever.mobile.core.common.DefaultDispatcherProvider
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.database.DATABASE_NAME
import com.wherever.mobile.core.database.DatabaseKeyProvider
import com.wherever.mobile.core.database.MIGRATION_1_2
import com.wherever.mobile.core.database.MIGRATION_2_3
import com.wherever.mobile.core.database.MIGRATION_3_4
import com.wherever.mobile.core.database.MIGRATION_4_5
import com.wherever.mobile.core.database.WhereverDatabase
import com.wherever.mobile.core.database.migratePlaintextDatabase
import com.wherever.mobile.core.database.rawKey
import com.wherever.mobile.core.security.KeystoreTokenCipher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

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
    fun provideDatabase(@ApplicationContext context: Context): WhereverDatabase {
        // Base cifrada con SQLCipher (constitución E-06); la llave la envuelve Android Keystore.
        System.loadLibrary("sqlcipher")
        val passphrase = DatabaseKeyProvider(context, KeystoreTokenCipher()).passphrase()
        migratePlaintextDatabase(context, passphrase)
        return Room.databaseBuilder(context, WhereverDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(SupportOpenHelperFactory(rawKey(passphrase).toByteArray()))
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .build()
    }
}
