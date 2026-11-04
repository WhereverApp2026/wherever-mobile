package com.wherever.mobile.feature.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    private val clave = "Clave123"
    private val claveCorta = "clave"

    @Test
    fun loginRequiresValidEmailAndPassword() {
        val errors = AuthValidator.login("ana", " ")

        assertEquals(FieldError.INVALID_EMAIL, errors[AuthField.EMAIL])
        assertEquals(FieldError.REQUIRED, errors[AuthField.PASSWORD])
    }

    @Test
    fun loginAcceptsValidCredentials() {
        assertTrue(AuthValidator.login("ana@example.com", "Clave123").isEmpty())
    }

    @Test
    fun registerRequiresTermsAndStrongPassword() {
        val errors = AuthValidator.register(
            RegistrationForm(
                name = " ",
                birthDate = "1994-13-40",
                email = "ana@example.com",
                password = claveCorta,
                passwordConfirmation = "otra",
                acceptedTerms = false
            )
        )

        assertEquals(FieldError.REQUIRED, errors[AuthField.NAME])
        assertEquals(FieldError.INVALID_BIRTH_DATE, errors[AuthField.BIRTH_DATE])
        assertEquals(FieldError.WEAK_PASSWORD, errors[AuthField.PASSWORD])
        assertEquals(FieldError.PASSWORD_MISMATCH, errors[AuthField.PASSWORD_CONFIRMATION])
        assertEquals(FieldError.TERMS_REQUIRED, errors[AuthField.TERMS])
    }

    @Test
    fun registerAcceptsValidForm() {
        val errors = AuthValidator.register(
            RegistrationForm(
                name = "Ana",
                birthDate = "1994-05-06",
                email = "ana@example.com",
                password = clave,
                passwordConfirmation = clave,
                acceptedTerms = true
            )
        )

        assertTrue(errors.isEmpty())
    }

    @Test
    fun resetRequiresTokenAndMatchingPassword() {
        val errors = AuthValidator.resetPassword("", clave, "Clave124")

        assertEquals(FieldError.REQUIRED, errors[AuthField.TOKEN])
        assertEquals(FieldError.PASSWORD_MISMATCH, errors[AuthField.PASSWORD_CONFIRMATION])
    }

    @Test
    fun verifyEmailRequiresToken() {
        assertEquals(FieldError.REQUIRED, AuthValidator.verifyEmail(" ")[AuthField.TOKEN])
    }

    @Test
    fun recoveryEmailUsesEmailValidation() {
        assertEquals(FieldError.INVALID_EMAIL, AuthValidator.recoveryEmail("ana")[AuthField.EMAIL])
        assertFalse(AuthValidator.recoveryEmail("ana@example.com").isNotEmpty())
    }
}
