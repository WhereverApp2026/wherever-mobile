package com.wherever.mobile.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushDestinationTest {
    @Test
    fun `sin tipo no es un push`() {
        assertNull(PushDestination.from(emptyMap()))
        assertNull(PushDestination.from(mapOf("tipo" to " ", "grupo_id" to "g1")))
    }

    @Test
    fun `cada tipo de la API lleva a su destino con sus ids`() {
        assertEquals(
            PushDestination.FriendRequest("u1"),
            PushDestination.from(mapOf("tipo" to "solicitud_amistad", "usuario_id" to "u1"))
        )
        assertEquals(
            PushDestination.FriendAccepted("u2"),
            PushDestination.from(mapOf("tipo" to "amistad_aceptada", "usuario_id" to "u2"))
        )
        assertEquals(
            PushDestination.PlanInvitation("g1", "p1"),
            PushDestination.from(mapOf("tipo" to "invitacion_plan", "grupo_id" to "g1", "plan_id" to "p1"))
        )
        assertEquals(
            PushDestination.VoteOpened("g1", "v1"),
            PushDestination.from(mapOf("tipo" to "votacion_abierta", "grupo_id" to "g1", "votacion_id" to "v1"))
        )
        assertEquals(
            PushDestination.VoteClosed(null, "v1"),
            PushDestination.from(mapOf("tipo" to "votacion_cerrada", "votacion_id" to "v1"))
        )
    }

    @Test
    fun `tipo desconocido o sin el id que necesita abre Notificaciones`() {
        assertEquals(PushDestination.Notifications, PushDestination.from(mapOf("tipo" to "cupon_nuevo")))
        assertEquals(PushDestination.Notifications, PushDestination.from(mapOf("tipo" to "solicitud_amistad")))
        assertEquals(PushDestination.Notifications, PushDestination.from(mapOf("tipo" to "votacion_abierta")))
        assertEquals(PushDestination.Notifications, PushDestination.from(mapOf("tipo" to "invitacion_plan")))
    }

    @Test
    fun `Security - ids con caracteres raros o demasiado largos se descartan`() {
        listOf("../yo", "u1?x=1", "u 1", "a".repeat(65), "").forEach { bad ->
            assertEquals(
                bad,
                PushDestination.Notifications,
                PushDestination.from(mapOf("tipo" to "solicitud_amistad", "usuario_id" to bad))
            )
        }
        assertEquals(
            PushDestination.FriendRequest("a".repeat(64)),
            PushDestination.from(mapOf("tipo" to "solicitud_amistad", "usuario_id" to "a".repeat(64)))
        )
    }
}
