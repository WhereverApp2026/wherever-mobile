package com.wherever.mobile.feature.help.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.help.domain.Faq
import com.wherever.mobile.feature.help.domain.REPORT_MAX_LENGTH
import com.wherever.mobile.feature.help.domain.SupportContacts
import kotlinx.coroutines.launch

/** Eventos de 86_Ayuda; la pantalla solo los envía (sin lógica en el Composable). */
internal data class HelpActions(
    val onBack: () -> Unit,
    val onQuery: (String) -> Unit,
    val onToggleFaq: (Int) -> Unit,
    val onWhatsApp: (String) -> Unit,
    val onEmail: (String) -> Unit,
    val onReport: () -> Unit
)

/** Eventos de la hoja «Reportar un problema». */
internal data class ReportActions(val onChange: (String) -> Unit, val onSend: () -> Unit, val onDismiss: () -> Unit)

/** 86_Ayuda (specs/nv-33). Se abre desde Ajustes. */
@Composable
fun HelpRoute(onBack: () -> Unit, viewModel: HelpViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sent = stringResource(R.string.help_report_sent)
    val noApp = stringResource(R.string.help_no_app)
    LaunchedEffect(viewModel) { viewModel.reportSent.collect { snackbar.showSnackbar(sent) } }
    val open: (Intent) -> Unit = { intent ->
        if (!context.startSafely(intent)) scope.launch { snackbar.showSnackbar(noApp) }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        HelpContent(
            state,
            HelpActions(
                onBack = onBack,
                onQuery = viewModel::onQueryChange,
                onToggleFaq = viewModel::onToggleFaq,
                onWhatsApp = { open(whatsAppIntent(it)) },
                onEmail = { open(emailIntent(it)) },
                onReport = viewModel::onOpenReport
            )
        )
        state.report?.let { report ->
            ReportSheet(
                report,
                ReportActions(viewModel::onReportChange, viewModel::sendReport, viewModel::onDismissReport)
            )
        }
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
}

/** Correo sin asunto ni cuerpo: no lleva datos del usuario (specs/nv-33 §5). */
private fun emailIntent(email: String) = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())

/** Chat de WhatsApp por su enlace público; solo el número de soporte. */
private fun whatsAppIntent(number: String) =
    Intent(Intent.ACTION_VIEW, "https://wa.me/${number.filter(Char::isDigit)}".toUri())

private fun Context.startSafely(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}

@Composable
internal fun HelpContent(state: HelpUiState, actions: HelpActions) {
    FormScreen {
        TopTitleRow(stringResource(R.string.help_title), actions.onBack)
        LabeledTextField(
            label = stringResource(R.string.help_search_label),
            value = state.query,
            onValueChange = actions.onQuery,
            placeholder = stringResource(R.string.help_search_placeholder),
            leadingIcon = Icons.Outlined.Search,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            trailingContent = if (state.query.isNotEmpty()) {
                {
                    WhereverIconButton(onClick = { actions.onQuery("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.help_search_clear))
                    }
                }
            } else {
                null
            },
            tag = "help_search"
        )
        SectionTitle(stringResource(R.string.help_faq_section))
        if (state.noResults) {
            InfoBanner(stringResource(R.string.help_no_results), tag = "help_no_results")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                state.faqs.forEach { faq ->
                    FaqCard(faq, expanded = faq.id == state.expandedId, onToggle = { actions.onToggleFaq(faq.id) })
                }
            }
        }
        SectionTitle(stringResource(R.string.help_contact_section))
        ContactCard(state.contacts, actions)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
}

/** Pregunta en acordeón: el encabezado abre y cierra la respuesta. */
@Composable
private fun FaqCard(faq: Faq, expanded: Boolean, onToggle: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("help_faq_${faq.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.TouchTarget)
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(horizontal = Spacing.L, vertical = Spacing.M)
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight
                },
                contentDescription = stringResource(if (expanded) R.string.help_collapse else R.string.help_expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = Spacing.L, end = Spacing.L, bottom = Spacing.M)
                    .testTag("help_faq_answer_${faq.id}")
            )
        }
    }
}

/** «¿Sigues con dudas?»: WhatsApp y correo solo si `BuildConfig` los trae; reportar siempre. */
@Composable
private fun ContactCard(contacts: SupportContacts, actions: HelpActions) {
    SurfaceCard(modifier = Modifier.testTag("help_contacts")) {
        contacts.whatsapp?.let { number ->
            ContactRow(
                icon = painterResource(R.drawable.ic_chat),
                iconTint = Mint,
                circle = MintTint,
                title = stringResource(R.string.help_whatsapp_title),
                subtitle = stringResource(R.string.help_whatsapp_subtitle),
                onClick = { actions.onWhatsApp(number) },
                tag = "help_whatsapp"
            )
        }
        contacts.email?.let { email ->
            ContactRow(
                icon = rememberVectorPainter(Icons.Outlined.Email),
                iconTint = RouteBlue,
                circle = RouteBlueTint,
                title = stringResource(R.string.help_email_title),
                subtitle = email,
                onClick = { actions.onEmail(email) },
                tag = "help_email"
            )
        }
        ContactRow(
            icon = painterResource(R.drawable.ic_flag),
            iconTint = Sunset,
            circle = SunsetTint,
            title = stringResource(R.string.help_report_title),
            subtitle = stringResource(R.string.help_report_subtitle),
            onClick = actions.onReport,
            tag = "help_report"
        )
    }
}

@Suppress("LongParameterList") // Cada fila de la maqueta cambia ícono, colores, textos y etiqueta.
@Composable
private fun ContactRow(
    icon: Painter,
    iconTint: Color,
    circle: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.RowMinHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.L, vertical = Spacing.S)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.AvatarS).background(circle, CircleShape)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(Sizes.IconM))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Hoja «Reportar un problema»: texto de 1 a 500 → `POST /errores`. */
@Composable
private fun ReportSheet(report: ReportUiState, actions: ReportActions) {
    WhereverBottomSheet(onDismissRequest = actions.onDismiss, tag = "help_report_sheet") {
        ReportForm(report, actions)
    }
}

@Composable
internal fun ReportForm(report: ReportUiState, actions: ReportActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.L)) {
        Text(
            stringResource(R.string.help_report_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        LabeledTextField(
            label = stringResource(R.string.help_report_label),
            value = report.text,
            onValueChange = actions.onChange,
            placeholder = stringResource(R.string.help_report_placeholder),
            singleLine = false,
            error = report.fieldError?.asString(),
            supporting = stringResource(R.string.common_char_count, report.text.length, REPORT_MAX_LENGTH),
            tag = "help_report_text"
        )
        InfoBanner(stringResource(R.string.help_report_privacy), icon = Icons.Outlined.Lock)
        report.error?.let { ErrorBanner(it.asString()) }
        WhereverPrimaryButton(
            text = stringResource(R.string.help_report_send),
            onClick = actions.onSend,
            enabled = report.canSend,
            loading = report.sending,
            tag = "help_report_send"
        )
    }
}

/** Preguntas de la vista previa con los textos reales de `strings.xml`. */
@Composable
private fun previewFaqs(): List<Faq> = listOf(
    R.string.help_faq_cancel_q to R.string.help_faq_cancel_a,
    R.string.help_faq_deposit_q to R.string.help_faq_deposit_a,
    R.string.help_faq_seal_q to R.string.help_faq_seal_a,
    R.string.help_faq_kitty_q to R.string.help_faq_kitty_a
).mapIndexed { index, (question, answer) -> Faq(index, stringResource(question), stringResource(answer)) }

private val PREVIEW_ACTIONS = HelpActions({}, {}, {}, {}, {}, {})

/** Contactos de la vista previa, como los dibuja la maqueta. */
private val PREVIEW_CONTACTS = SupportContacts(email = "ayuda@ejemplo.com", whatsapp = "5215500000000")

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HelpPreview() {
    WhereverTheme {
        HelpContent(HelpUiState(faqs = previewFaqs(), expandedId = 0, contacts = PREVIEW_CONTACTS), PREVIEW_ACTIONS)
    }
}

@Preview
@Composable
internal fun HelpNoResultsPreview() {
    WhereverTheme {
        HelpContent(HelpUiState(query = "mascotas", faqs = emptyList(), contacts = PREVIEW_CONTACTS), PREVIEW_ACTIONS)
    }
}

@Preview
@Composable
internal fun HelpReportPreview() {
    WhereverTheme {
        // La hoja se dibuja sobre `surface`; la vista previa pinta ese fondo para la captura.
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(ScreenPadding)) {
            ReportForm(
                ReportUiState(text = stringResource(R.string.help_report_placeholder)),
                ReportActions({}, {}, {})
            )
        }
    }
}
