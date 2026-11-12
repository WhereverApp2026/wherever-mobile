package com.wherever.mobile.feature.auth.domain

import java.time.Clock
import java.time.LocalDate
import java.time.Period

private const val NAME_MIN = 2
private const val NAME_MAX = 40
private const val ADULT_AGE = 18

/** Edad máxima aceptada al registrarse (nv-42). */
const val OLDEST_BIRTH_YEARS = 100L

/** Años hacia atrás en los que abre el calendario de Registro (nv-42). */
const val DEFAULT_BIRTH_YEARS_AGO = 18L

/** Por qué una fecha de nacimiento no sirve para registrarse (nv-42). */
enum class BirthDateIssue { FUTURE, TOO_OLD, UNDERAGE }

fun isValidName(name: String): Boolean = name.trim().length in NAME_MIN..NAME_MAX

private fun isAdult(birthDate: LocalDate, today: LocalDate): Boolean =
    Period.between(birthDate, today).years >= ADULT_AGE

/** Fecha más antigua aceptada: hoy hace [OLDEST_BIRTH_YEARS] años, ese día incluido. */
fun oldestBirthDate(today: LocalDate): LocalDate = today.minusYears(OLDEST_BIRTH_YEARS)

/**
 * Valida la fecha contra "hoy" en la zona del [clock] inyectado, no en UTC: de noche en México
 * ya es mañana en UTC, y eso no debe volver futura la fecha de hoy.
 */
fun validateBirthDate(birthDate: LocalDate, clock: Clock): BirthDateIssue? {
    val today = LocalDate.now(clock)
    return when {
        birthDate.isAfter(today) -> BirthDateIssue.FUTURE
        birthDate.isBefore(oldestBirthDate(today)) -> BirthDateIssue.TOO_OLD
        !isAdult(birthDate, today) -> BirthDateIssue.UNDERAGE
        else -> null
    }
}
