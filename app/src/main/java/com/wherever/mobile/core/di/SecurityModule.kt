package com.wherever.mobile.core.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.SessionCleaner
import com.wherever.mobile.core.security.AndroidDeviceSignalsReader
import com.wherever.mobile.core.security.AppLockPreferences
import com.wherever.mobile.core.security.DataStoreTokenStore
import com.wherever.mobile.core.security.DeviceSignalsReader
import com.wherever.mobile.core.security.EncryptedTokensSerializer
import com.wherever.mobile.core.security.KeystoreTokenCipher
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.core.security.SharedAppLockPreferences
import com.wherever.mobile.core.security.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context, dispatchers: DispatcherProvider): TokenStore =
        DataStoreTokenStore(
            DataStoreFactory.create(
                serializer = EncryptedTokensSerializer(KeystoreTokenCipher()),
                // Clave perdida o archivo dañado equivale a sesión vencida: se va a Login.
                corruptionHandler = ReplaceFileCorruptionHandler<SessionTokens?> { null },
                scope = CoroutineScope(dispatchers.io + SupervisorJob()),
                produceFile = { context.dataStoreFile("session_tokens.enc") }
            )
        )

    @Provides
    fun provideDeviceSignals(reader: AndroidDeviceSignalsReader): DeviceSignalsReader = reader

    @Provides
    fun provideAppLock(impl: SharedAppLockPreferences): AppLockPreferences = impl

    /** Al cerrar sesión se apaga el bloqueo con huella: la siguiente cuenta decide si lo quiere. */
    @Provides
    @IntoSet
    fun provideAppLockCleaner(impl: SharedAppLockPreferences): SessionCleaner = impl
}
