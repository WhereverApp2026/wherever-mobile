package com.wherever.mobile.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledCheckbox
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StepHeader
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.designsystem.rememberFormSubmit
import com.wherever.mobile.core.ui.SecureScreen
import com.wherever.mobile.core.ui.formatCountdown
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val PREVIEW_TODAY: LocalDate = LocalDate.parse("2026-11-12")

/** 05_Registro */
@Composable
fun RegisterRoute(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    onHelp: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    SecureScreen()
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
        onBack = onBack,
        onHelp = onHelp
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
    onBack: () -> Unit,
    onHelp: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val submit = rememberFormSubmit(onSubmit)
    FormScreen(
        footer = {
            val waiting = state.retryAfterSeconds > 0
            WhereverPrimaryButton(
                text = if (waiting) {
                    stringResource(R.string.common_wait, formatCountdown(state.retryAfterSeconds))
                } else {
                    stringResource(R.string.register_submit)
                },
                onClick = submit,
                enabled = state.canSubmit,
                loading = state.submitting,
                tag = "register_submit"
            )
            GoogleSignInBlock(tag = "register_google")
        }
    ) {
        StepHeader(stringResource(R.string.register_step), step = 1, total = ONBOARDING_STEPS, onBack = onBack)
        TitleBlock(stringResource(R.string.register_title), subtitle = stringResource(R.string.register_subtitle))
        LabeledTextField(
            label = stringResource(R.string.field_name),
            value = state.name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.register_name_placeholder),
            leadingIcon = Icons.Outlined.Person,
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
                leadingIcon = Icons.Outlined.DateRange,
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
            leadingIcon = Icons.Outlined.Email,
            error = state.emailError?.asString(),
            supporting = when (state.emailCheck) {
                EmailCheck.CHECKING -> stringResource(R.string.register_email_checking)
                EmailCheck.AVAILABLE -> stringResource(R.string.register_email_available)
                else -> null
            },
            onBlur = onEmailBlur,
            // Siguiente lleva a la contraseña (antes el teclado mostraba ✓ y no avanzaba).
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            tag = "register_email"
        )
        LabeledTextField(
            label = stringResource(R.string.field_password),
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.register_password_placeholder),
            leadingIcon = Icons.Outlined.Lock,
            error = state.passwordError?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation(),
            tag = "register_password"
        )
        LabeledCheckbox(state.acceptsTerms, onTermsChange, tag = "register_terms") {
            TermsText(onHelp, Modifier.padding(start = Spacing.XS))
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
    if (showDatePicker) {
        BirthDatePicker(
            today = state.today,
            onDismiss = { showDatePicker = false },
            onPicked = {
                onBirthDateChange(it)
                showDatePicker = false
            }
        )
    }
}

/** Solo deja tocar días entre hace 100 años y hoy; los futuros salen deshabilitados (nv-42). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthDatePicker(
    today: LocalDate,
    onDismiss: () -> Unit,
    onPicked: (LocalDate) -> Unit,
    initialDisplayedMonthMillis: Long = BirthDateBounds(today).initialDisplayedMonthMillis
) {
    val bounds = remember(today) { BirthDateBounds(today) }
    val pickerState = rememberDatePickerState(
        initialDisplayedMonthMillis = initialDisplayedMonthMillis,
        yearRange = bounds.yearRange,
        selectableDates = remember(bounds) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = bounds.isSelectable(utcTimeMillis)

                override fun isSelectableYear(year: Int) = bounds.isSelectableYear(year)
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            WhereverTextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { pickerState.selectedDateMillis?.let { onPicked(it.toPickerDate()) } },
                modifier = Modifier.testTag("birth_date_accept")
            ) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = { WhereverTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    ) {
        DatePicker(state = pickerState, modifier = Modifier.testTag("birth_date_picker"))
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun RegisterPreview() {
    WhereverTheme {
        RegisterContent(RegisterUiState(today = PREVIEW_TODAY), {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

/** "Acepto los términos y el aviso de privacidad.": los dos enlaces abren 86_Ayuda (Figma 05_Registro). */
@Composable
private fun TermsText(onHelp: () -> Unit, modifier: Modifier = Modifier) {
    val full = stringResource(R.string.register_terms)
    val links = listOf(stringResource(R.string.register_terms_link), stringResource(R.string.register_privacy_link))
    val linkStyle = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    )
    val text = buildAnnotatedString {
        append(full)
        links.forEachIndexed { index, link ->
            val start = full.indexOf(link)
            if (start >= 0) {
                addLink(
                    LinkAnnotation.Clickable("register_terms_link_$index", linkStyle) { onHelp() },
                    start,
                    start + link.length
                )
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.testTag("register_terms_text")
    )
}
