package com.wherever.mobile.core.security

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first

class DataStoreTokenStore(private val dataStore: DataStore<SessionTokens?>) : TokenStore {
    override suspend fun current(): SessionTokens? = dataStore.data.first()

    override suspend fun save(tokens: SessionTokens) {
        dataStore.updateData { tokens }
    }

    override suspend fun clear() {
        dataStore.updateData { null }
    }
}
