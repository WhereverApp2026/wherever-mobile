package com.wherever.mobile.core.data

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class BaseRepository<Entity, Domain>(private val dispatcherProvider: DispatcherProvider) {
    protected abstract fun observeEntities(): Flow<List<Entity>>

    protected abstract suspend fun fetchEntities(): List<Entity>

    protected abstract suspend fun saveEntities(entities: List<Entity>)

    protected abstract fun toDomain(entity: Entity): Domain

    fun observe(): Flow<Result<List<Domain>>> = observeEntities()
        .map<List<Entity>, Result<List<Domain>>> { entities ->
            Result.Success(entities.map { entity -> toDomain(entity) })
        }
        .catch { error -> emit(Result.Failure(AppError.Unknown(error.message))) }

    suspend fun refresh(): Result<Unit> = withContext(dispatcherProvider.io) {
        runCatching {
            saveEntities(fetchEntities())
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { error -> Result.Failure(AppError.Unknown(error.message)) }
        )
    }
}
