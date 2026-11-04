package com.wherever.mobile.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onboarding_state")
data class OnboardingStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val userId: String = "",
    val step: String,
    val interests: String = "",
    val vibe: String = "",
    val budget: String = "",
    val company: String = "",
    val updatedAt: Long = 0
)
