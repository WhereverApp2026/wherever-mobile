package com.wherever.mobile.demo

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.data.BaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class DemoEntity(val id: Int, val name: String)

data class DemoItem(val id: Int, val name: String)

class DemoRepository(dispatcherProvider: DispatcherProvider) : BaseRepository<DemoEntity, DemoItem>(dispatcherProvider) {

    private val local = MutableStateFlow<List<DemoEntity>>(emptyList())

    var remote: List<DemoEntity> = emptyList()

    override fun observeEntities(): Flow<List<DemoEntity>> = local

    override suspend fun fetchEntities(): List<DemoEntity> = remote

    override suspend fun saveEntities(entities: List<DemoEntity>) {
        local.value = entities
    }

    override fun toDomain(entity: DemoEntity): DemoItem = DemoItem(entity.id, entity.name.uppercase())
}
