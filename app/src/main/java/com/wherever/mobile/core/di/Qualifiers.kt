package com.wherever.mobile.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient

/** Scope de toda la app: trabajo que debe terminar aunque la pantalla se cierre (registro del push). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
