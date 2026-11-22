@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.LogoMark
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteIllustration
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme

/** 01_Bienvenida */
@Composable
fun WelcomeScreen(onCreateAccount: () -> Unit, onHaveAccount: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RouteBlue, RouteBlue, Ink)))
    ) {
        // En horizontal o con letra grande la ilustración tapaba el texto: solo se dibuja si cabe.
        if (maxHeight >= Sizes.WelcomeIllustration + Sizes.WelcomeContent) {
            RouteIllustration(Modifier.fillMaxWidth().height(Sizes.WelcomeIllustration).testTag("welcome_art"))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .safeDrawingPadding()
                .padding(ScreenPadding),
            verticalArrangement = Arrangement.Bottom
        ) {
            LogoMark(contentDescription = stringResource(R.string.app_name))
            Spacer(Modifier.height(Spacing.M))
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.M))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(Spacing.XXL))
            WhereverPrimaryButton(
                text = stringResource(R.string.welcome_create_account),
                onClick = onCreateAccount,
                tag = "welcome_create_account"
            )
            Spacer(Modifier.height(Spacing.M))
            WhereverOutlinedButton(
                text = stringResource(R.string.welcome_have_account),
                onClick = onHaveAccount,
                contentColor = Color.White,
                borderColor = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                tag = "welcome_have_account"
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun WelcomePreview() {
    WhereverTheme { WelcomeScreen({}, {}) }
}
