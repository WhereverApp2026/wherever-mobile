package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledCheckbox
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.StepHeader
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatCountdown
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val OLDEST_YEAR_RANGE = 100
private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** 05_Registro */
@Composable
fun RegisterRoute(onBack: () -> Unit, onRegistered: () -> Unit, viewModel: RegisterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.registered.collect { onRegistered() } }
    RegisterContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onEmailChange = viewModel::onEmailChange,
        onEmailBlur = viewModel::onEmailBlur,
        onPasswordChange = viewModel::onPasswordChange,
        onTermsChange = viewModel::onTermsChange,
        onSubmit = viewModel::submit,
        onBack = onBack
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    FormScreen(
        footer = {
            val waiting = state.retryAfterSeconds > 0
            WhereverPrimaryButton(
                text = if (waiting) {
                    stringResource(R.string.common_wait, formatCountdown(state.retryAfterSeconds))
                } else {
                    stringResource(R.string.register_submit)
                },
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.submitting,
                tag = "register_submit"
            )
        }
    ) {
        StepHeader(stringResource(R.string.register_step), step = 1, total = ONBOARDING_STEPS, onBack = onBack)
        TitleBlock(stringResource(R.string.register_title), subtitle = stringResource(R.string.register_subtitle))
        LabeledTextField(
            label = stringResource(R.string.field_name),
            value = state.name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.register_name_placeholder),
            leadingIcon = Icons.Filled.Person,
            error = state.nameError?.asString(),
            tag = "register_name"
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                label = stringResource(R.string.field_birth_date),
                value = state.birthDate?.format(DISPLAY_DATE).orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = stringResource(R.string.register_birth_date_placeholder),
                leadingIcon = Icons.Filled.DateRange,
                error = state.birthDateError?.asString(),
                tag = "register_birth_date"
            )
            Box(Modifier.matchParentSize().clickable { showDatePicker = true })
        }
        LabeledTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.field_email_placeholder),
            leadingIcon = Icons.Filled.Email,
            error = state.emailError?.asString(),
            supporting = when (state.emailCheck) {
                EmailCheck.CHECKING -> stringResource(R.string.register_email_checking)
                EmailCheck.AVAILABLE -> stringResource(R.string.register_email_available)
                else -> null
            },
            onBlur = onEmailBlur,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            tag = "register_email"
        )
        LabeledTextField(
            label = stringResource(R.string.field_password),
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.register_password_placeholder),
            leadingIcon = Icons.Filled.Lock,
            error = state.passwordError?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            tag = "register_password"
        )
        LabeledCheckbox(state.acceptsTerms, onTermsChange, tag = "register_terms") {
            Text(
                text = stringResource(R.string.register_terms),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
    if (showDatePicker) {
        BirthDatePicker(
            maxYear = state.maxBirthYear,
            onDismiss = { showDatePicker = false },
            onPicked = {
                onBirthDateChange(it)
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePicker(maxYear: Int, onDismiss: () -> Unit, onPicked: (LocalDate) -> Unit) {
    val pickerState = rememberDatePickerState(yearRange = (maxYear - OLDEST_YEAR_RANGE)..maxYear)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}

@Preview
@Composable
private fun RegisterPreview() {
    WhereverTheme(darkTheme = false) {
        RegisterContent(RegisterUiState(maxBirthYear = 2026), {}, {}, {}, {}, {}, {}, {}, {})
    }
}
