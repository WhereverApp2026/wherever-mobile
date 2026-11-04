package com.wherever.mobile.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingDao {

    @Query("SELECT * FROM onboarding_state WHERE id = 1")
    fun observe(): Flow<OnboardingStateEntity?>

    @Query("SELECT * FROM onboarding_state WHERE id = 1")
    suspend fun get(): OnboardingStateEntity?

    @Upsert
    suspend fun upsert(entity: OnboardingStateEntity)

    @Query("DELETE FROM onboarding_state")
    suspend fun clear()
}
