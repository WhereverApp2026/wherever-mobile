package com.wherever.mobile.feature.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.feature.auth.domain.FieldError

@Composable
fun AuthHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Text(text = stringResource(R.string.auth_action_back))
            }
        }
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    error: FieldError? = null,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = { Text(text = label) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(text = stringResource(error.messageResource())) }
        } else {
            null
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                TextButton(onClick = onTogglePassword) {
                    Text(
                        text = stringResource(
                            if (passwordVisible) {
                                R.string.auth_action_hide_password
                            } else {
                                R.string.auth_action_show_password
                            }
                        )
                    )
                }
            }
        } else {
            null
        }
    )
}

@Composable
fun AuthErrorBanner(error: AppError?, modifier: Modifier = Modifier) {
    if (error == null) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(error.messageResource()), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AuthFooterAction(text: String, actionText: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onClick) {
            Text(text = actionText)
        }
    }
}

@Composable
fun FieldError.messageResource(): Int = when (this) {
    FieldError.REQUIRED -> R.string.auth_error_required
    FieldError.INVALID_EMAIL -> R.string.auth_error_email
    FieldError.INVALID_BIRTH_DATE -> R.string.auth_error_birth_date
    FieldError.WEAK_PASSWORD -> R.string.auth_error_password
    FieldError.PASSWORD_MISMATCH -> R.string.auth_error_password_mismatch
    FieldError.TERMS_REQUIRED -> R.string.auth_error_terms
}

@Composable
fun AppError.messageResource(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.auth_error_credentials
    is AppError.Validation -> R.string.error_validation
    is AppError.RateLimited -> R.string.auth_error_rate_limited
    AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
