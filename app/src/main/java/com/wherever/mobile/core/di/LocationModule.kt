package com.wherever.mobile.core.di

import com.wherever.mobile.core.location.AndroidLocationProvider
import com.wherever.mobile.core.location.LocationPermissionPreferences
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.location.SharedLocationPermissionPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds
    abstract fun bindLocation(impl: AndroidLocationProvider): LocationProvider

    @Binds
    abstract fun bindPermissionPreferences(impl: SharedLocationPermissionPreferences): LocationPermissionPreferences
}
