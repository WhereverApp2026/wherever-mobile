package com.wherever.mobile.feature.auth.domain

import java.time.Clock
import java.time.LocalDate
import java.time.Period

private const val NAME_MIN = 2
private const val NAME_MAX = 40
private const val ADULT_AGE = 18

fun isValidName(name: String): Boolean = name.trim().length in NAME_MIN..NAME_MAX

fun isAdult(birthDate: LocalDate, clock: Clock): Boolean =
    Period.between(birthDate, LocalDate.now(clock)).years >= ADULT_AGE
