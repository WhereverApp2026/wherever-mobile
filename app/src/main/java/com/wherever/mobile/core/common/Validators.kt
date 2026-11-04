package com.wherever.mobile.core.common

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private const val PASSWORD_MIN = 8
private const val PASSWORD_MAX = 128

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

/** Regla de la API: 8 a 128 caracteres, con al menos una letra y un número. */
fun isValidPassword(password: String): Boolean = password.length in PASSWORD_MIN..PASSWORD_MAX &&
    password.any(Char::isLetter) &&
    password.any(Char::isDigit)
