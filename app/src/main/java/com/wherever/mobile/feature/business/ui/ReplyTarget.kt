package com.wherever.mobile.feature.business.ui

/** Lo que 52_NegResp necesita de la reseña; llega como argumentos de la ruta. */
data class ReplyTarget(
    val reviewId: String,
    val rating: Int,
    val comment: String,
    val author: String,
    val createdAt: String,
    val currentReply: String?,
    /** Avance del criterio «responder reseñas» del Sello; nulo si el panel no lo mandó. */
    val progress: Int?
)
