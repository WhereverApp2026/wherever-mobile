package com.wherever.mobile.feature.auth.domain

enum class AuthField {
    NAME,
    BIRTH_DATE,
    EMAIL,
    PASSWORD,
    PASSWORD_CONFIRMATION,
    TOKEN,
    TERMS
}

enum class FieldError {
    REQUIRED,
    INVALID_EMAIL,
    INVALID_BIRTH_DATE,
    WEAK_PASSWORD,
    PASSWORD_MISMATCH,
    TERMS_REQUIRED
}

data class RegistrationForm(
    val name: String,
    val birthDate: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
    val acceptedTerms: Boolean
)

object AuthValidator {

    fun login(email: String, password: String): Map<AuthField, FieldError> = buildMap {
        validateEmail(email)?.let { put(AuthField.EMAIL, it) }
        if (password.isBlank()) put(AuthField.PASSWORD, FieldError.REQUIRED)
    }

    fun register(form: RegistrationForm): Map<AuthField, FieldError> = buildMap {
        if (form.name.isBlank()) put(AuthField.NAME, FieldError.REQUIRED)
        validateBirthDate(form.birthDate)?.let { put(AuthField.BIRTH_DATE, it) }
        validateEmail(form.email)?.let { put(AuthField.EMAIL, it) }
        validatePassword(form.password)?.let { put(AuthField.PASSWORD, it) }
        if (form.password != form.passwordConfirmation) {
            put(AuthField.PASSWORD_CONFIRMATION, FieldError.PASSWORD_MISMATCH)
        }
        if (!form.acceptedTerms) put(AuthField.TERMS, FieldError.TERMS_REQUIRED)
    }

    fun resetPassword(token: String, password: String, confirmation: String): Map<AuthField, FieldError> = buildMap {
        if (token.isBlank()) put(AuthField.TOKEN, FieldError.REQUIRED)
        validatePassword(password)?.let { put(AuthField.PASSWORD, it) }
        if (password != confirmation) {
            put(AuthField.PASSWORD_CONFIRMATION, FieldError.PASSWORD_MISMATCH)
        }
    }

    fun verifyEmail(token: String): Map<AuthField, FieldError> = buildMap {
        if (token.isBlank()) put(AuthField.TOKEN, FieldError.REQUIRED)
    }

    fun recoveryEmail(email: String): Map<AuthField, FieldError> = buildMap {
        validateEmail(email)?.let { put(AuthField.EMAIL, it) }
    }

    private fun validateEmail(email: String): FieldError? = when {
        email.isBlank() -> FieldError.REQUIRED
        !EMAIL_PATTERN.matches(email.trim()) -> FieldError.INVALID_EMAIL
        else -> null
    }

    private fun validatePassword(password: String): FieldError? = when {
        password.isBlank() -> FieldError.REQUIRED
        password.length < MIN_PASSWORD_LENGTH -> FieldError.WEAK_PASSWORD
        password.none { it.isUpperCase() } -> FieldError.WEAK_PASSWORD
        password.none { it.isDigit() } -> FieldError.WEAK_PASSWORD
        else -> null
    }

    private fun validateBirthDate(value: String): FieldError? {
        if (value.isBlank()) return FieldError.REQUIRED
        return try {
            java.time.LocalDate.parse(value.trim())
            null
        } catch (_: java.time.format.DateTimeParseException) {
            FieldError.INVALID_BIRTH_DATE
        }
    }

    private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val MIN_PASSWORD_LENGTH = 8
}
