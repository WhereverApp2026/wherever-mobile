package com.wherever.mobile.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Push que llegó con la app en primer plano: se muestra como aviso dentro de la app (88_PushBanner). */
data class InAppPush(val title: String?, val body: String?, val data: Map<String, String?>)

/**
 * Puente entre `WhereverMessagingService` y la raíz de la app. Con la app visible el push no se pinta en
 * la barra del sistema: llega aquí y la raíz muestra el aviso con Ver y Más tarde (specs/nv-18).
 */
@Singleton
class InAppPushes @Inject constructor() {
    @Volatile
    var foreground: Boolean = false

    private val flow = MutableSharedFlow<InAppPush>(extraBufferCapacity = BUFFER)
    val pushes: SharedFlow<InAppPush> = flow.asSharedFlow()

    /** `false` si la app no está visible: el servicio pinta la notificación del sistema. */
    fun offer(push: InAppPush): Boolean = foreground && flow.tryEmit(push)

    private companion object {
        const val BUFFER = 4
    }
}
