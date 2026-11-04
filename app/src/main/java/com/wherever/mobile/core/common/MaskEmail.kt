package com.wherever.mobile.core.common

private const val VISIBLE_CHARS = 2

/** os•••@correo.com */
fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    if (at <= 0) return email
    return email.take(minOf(VISIBLE_CHARS, at)) + "•••" + email.substring(at)
}
