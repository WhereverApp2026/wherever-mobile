package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.feature.profile.domain.CityRef
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.NotificationSettings
import com.wherever.mobile.feature.profile.domain.Preferences
import com.wherever.mobile.feature.profile.domain.PreferencesPatch
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.PrivacyPatch
import com.wherever.mobile.feature.profile.domain.Tastes
import com.wherever.mobile.feature.profile.domain.UserProfile

fun TastesDto.toEntity() = TastesEntity(categories, budgetMin, budgetMax, restrictions, allergies)

fun PreferencesDto.toEntity() = PreferencesEntity(
    mobility = mobility,
    theme = theme,
    units = units,
    quietFrom = quietHours?.from,
    quietTo = quietHours?.to,
    notifReservations = notifications.reservations,
    notifCoupons = notifications.coupons,
    notifGroups = notifications.groups,
    notifReviews = notifications.reviews,
    notifPromotions = notifications.promotions
)

fun PrivacyDto.toEntity() =
    PrivacyEntity(location, personalizedLearning, mood, usageData, searchableByPhone, sensitiveData)

fun UserProfileDto.toEntity() = UserProfileEntity(
    id = id,
    name = name,
    email = email,
    emailVerified = emailVerified,
    mode = mode,
    city = city?.let { CityEntity(it.id, it.name) },
    tastes = tastes?.toEntity(),
    preferences = preferences.toEntity(),
    privacy = privacy.toEntity(),
    points = points,
    level = level,
    weekStreak = weekStreak,
    deleteAt = deleteAt,
    version = version
)

fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    name = name,
    email = email,
    emailVerified = emailVerified,
    mode = mode,
    city = city?.let { CityRef(it.id, it.name) },
    tastes = tastes?.let { Tastes(it.categories, it.budgetMin, it.budgetMax, it.restrictions, it.allergies) },
    preferences = Preferences(
        mobility = preferences.mobility,
        theme = preferences.theme,
        units = preferences.units,
        quietFrom = preferences.quietFrom,
        quietTo = preferences.quietTo,
        notifications = NotificationSettings(
            reservations = preferences.notifReservations,
            coupons = preferences.notifCoupons,
            groups = preferences.notifGroups,
            reviews = preferences.notifReviews,
            promotions = preferences.notifPromotions
        )
    ),
    privacy = Privacy(
        privacy.location,
        privacy.personalizedLearning,
        privacy.mood,
        privacy.usageData,
        privacy.searchableByPhone,
        privacy.sensitiveData
    ),
    points = points,
    level = level,
    weekStreak = weekStreak,
    deleteAt = deleteAt,
    version = version
)

fun Tastes.toDto() = TastesDto(categories, budgetMin, budgetMax, restrictions, allergies)

fun CitySuggestionDto.toDomain() = CitySuggestion(id, name, state)

fun PreferencesPatch.toDto(): PreferencesPatchDto {
    val hasNotifications = listOf(coupons, groups, reviews, promotions).any { it != null }
    return PreferencesPatchDto(
        theme = theme,
        mobility = mobility,
        notifications = if (hasNotifications) NotificationsPatchDto(coupons, groups, reviews, promotions) else null
    )
}

fun PrivacyPatch.toDto() =
    PrivacyPatchDto(location, personalizedLearning, mood, usageData, searchableByPhone, sensitiveData)
