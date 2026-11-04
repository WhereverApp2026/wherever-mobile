package com.wherever.mobile.core.network

/** Marca un método de Retrofit que no lleva Bearer. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicEndpoint
