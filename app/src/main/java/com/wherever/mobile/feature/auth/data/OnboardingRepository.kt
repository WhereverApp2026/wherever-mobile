package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.database.OnboardingDao
import com.wherever.mobile.core.database.OnboardingStateEntity
import com.wherever.mobile.feature.auth.domain.FriendSuggestion
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.auth.domain.OnboardingSnapshot
import com.wherever.mobile.feature.auth.domain.OnboardingStep
import javax.inject.Inject
import kotlinx.coroutines.withContext

class OnboardingRepository @Inject constructor(
    private val remote: OnboardingRemote,
    private val dao: OnboardingDao,
    private val dispatchers: DispatcherProvider
) {

    suspend fun snapshot(): OnboardingSnapshot = withContext(dispatchers.io) {
        dao.get()?.toSnapshot() ?: OnboardingSnapshot()
    }

    suspend fun connect(): OnboardingStep = withContext(dispatchers.io) {
        val current = snapshot()
        if (current.step != OnboardingStep.REGISTER) {
            return@withContext current.step
        }
        val remoteProfile = remote.me().getOrNull()
        if (remoteProfile != null && OnboardingParser.onboardingComplete(remoteProfile) == true) {
            persist(current.copy(step = OnboardingStep.COMPLETE))
            OnboardingStep.COMPLETE
        } else {
            OnboardingStep.PREFERENCES
        }
    }

    suspend fun savePreferences(preferences: OnboardingPreferences): Result<Unit> = withContext(dispatchers.io) {
        val result = remote.savePreferences(
            PreferencesRequestDto(
                interests = preferences.interests,
                vibe = preferences.vibe,
                budget = preferences.budget,
                company = preferences.company
            )
        )
        if (result is Result.Success) {
            val current = snapshot()
            persist(current.copy(step = OnboardingStep.LOCATION, preferences = preferences))
        }
        result
    }

    suspend fun skipPreferences() = withContext(dispatchers.io) {
        persist(
            snapshot().copy(
                step = OnboardingStep.LOCATION,
                preferences = OnboardingPreferences()
            )
        )
    }

    suspend fun locationHandled() = withContext(dispatchers.io) {
        persist(snapshot().copy(step = OnboardingStep.FRIENDS))
    }

    suspend fun friendsHandled() = withContext(dispatchers.io) {
        persist(snapshot().copy(step = OnboardingStep.PERMISSIONS))
    }

    suspend fun suggestions(): Result<List<FriendSuggestion>> = withContext(dispatchers.io) {
        remote.suggestions()
    }

    suspend fun sendFriendRequest(userId: String): Result<Unit> = withContext(dispatchers.io) {
        remote.sendFriendRequest(FriendRequestDto(userId))
    }

    suspend fun complete(): Result<Unit> = withContext(dispatchers.io) {
        val result = remote.complete()
        if (result is Result.Success) {
            persist(snapshot().copy(step = OnboardingStep.COMPLETE))
        }
        result
    }

    private suspend fun persist(snapshot: OnboardingSnapshot) {
        dao.upsert(
            OnboardingStateEntity(
                step = snapshot.step.name,
                interests = snapshot.preferences.interests.joinToString(separator = ","),
                vibe = snapshot.preferences.vibe,
                budget = snapshot.preferences.budget,
                company = snapshot.preferences.company,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun OnboardingStateEntity.toSnapshot(): OnboardingSnapshot = OnboardingSnapshot(
        step = OnboardingStep.from(step),
        preferences = OnboardingPreferences(
            interests = interests.split(',').map(String::trim).filter(String::isNotEmpty),
            vibe = vibe,
            budget = budget,
            company = company
        )
    )
}
