// TooManyFunctions: un Composable por paso y por fila de la maqueta (43 a 47), mejor juntos que repartidos.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.business.ui

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InlineProgress
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StepHeader
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.BusinessDraft
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.Catalog
import com.wherever.mobile.feature.business.domain.DESCRIPTION_MAX
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.feature.business.domain.MAX_PHOTOS
import com.wherever.mobile.feature.business.domain.MIN_PHOTOS
import com.wherever.mobile.feature.business.domain.Option
import com.wherever.mobile.feature.business.domain.PRICE_LEVELS
import com.wherever.mobile.feature.business.domain.isValidRange

/** Pasos del alta como los numera la maqueta (42_NegCuenta es el 1 y se salta con sesión). */
private const val TOTAL_STEPS = 5
private const val FIRST_VISIBLE_STEP = 2

/** Tipos de documento que se pueden elegir. */
private val DOCUMENT_MIME = arrayOf("image/jpeg", "image/png", "application/pdf")

/** A dónde va el alta al terminar o al salir. */
data class OnboardingNavigation(val onExit: () -> Unit, val onPanel: () -> Unit)

/** 43_NegRegistro → 44_NegUbic → 45_NegOper → 46_NegVerif → 47_NegListo (specs/nv-34). Entra desde Ajustes. */
@Composable
fun BusinessOnboardingRoute(
    navigation: OnboardingNavigation,
    viewModel: BusinessOnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.exit.collect { navigation.onExit() } }
    BackHandler(enabled = state.step != OnboardingStep.DONE) { viewModel.back() }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.locate() }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoPicked(it.toString()) }
    }
    var pendingDocument by remember { mutableStateOf<DocumentType?>(null) }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val type = pendingDocument
        if (uri != null && type != null) viewModel.onDocumentPicked(type, uri.toString())
    }
    val actions = OnboardingActions(
        onBack = viewModel::back,
        onNext = viewModel::next,
        onSaveExit = viewModel::saveAndExit,
        onName = viewModel::onName,
        onCategory = viewModel::onCategory,
        onDescription = viewModel::onDescription,
        onAiWords = viewModel::onAiWords,
        onSuggest = viewModel::suggestDescription,
        onAddress = viewModel::onAddress,
        onLocate = { permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onSchedule = viewModel::onSchedule,
        onPriceLevel = viewModel::onPriceLevel,
        onAveragePrice = viewModel::onAveragePrice,
        onToggleService = viewModel::onToggleService,
        onAddPhoto = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onDocument = { type ->
            pendingDocument = type
            documentPicker.launch(DOCUMENT_MIME)
        },
        onClabe = viewModel::onClabe,
        onHolder = viewModel::onHolder,
        onPanel = navigation.onPanel,
        onEdit = viewModel::edit,
        onRetry = viewModel::load
    )
    OnboardingContent(state, actions)
}

@Composable
internal fun OnboardingContent(state: OnboardingUiState, actions: OnboardingActions) {
    val loadError = state.loadError
    when {
        state.loading -> LoadingState()
        loadError != null -> ErrorState(loadError.asString(), actions.onRetry)
        state.step == OnboardingStep.DONE -> DoneStep(state, actions)
        else -> StepForm(state, actions)
    }
}

@Composable
private fun StepForm(state: OnboardingUiState, actions: OnboardingActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(
                    if (state.step ==
                        OnboardingStep.VERIFY
                    ) {
                        R.string.business_submit
                    } else {
                        R.string.business_continue
                    }
                ),
                onClick = actions.onNext,
                enabled = state.canContinue,
                loading = state.saving,
                tag = "business_next"
            )
            WhereverTextLink(
                stringResource(R.string.business_save_exit),
                actions.onSaveExit,
                Modifier.testTag("business_save_exit")
            )
        }
    ) {
        StepHeader(
            stringResource(
                R.string.business_step_label,
                state.step.ordinal + FIRST_VISIBLE_STEP,
                TOTAL_STEPS,
                stringResource(state.step.label())
            ),
            state.step.ordinal + FIRST_VISIBLE_STEP,
            TOTAL_STEPS,
            actions.onBack
        )
        when (state.step) {
            OnboardingStep.REGISTER -> RegisterStep(state, actions)
            OnboardingStep.LOCATION -> LocationStep(state, actions)
            OnboardingStep.OPERATION -> OperationStep(state, actions)
            OnboardingStep.VERIFY -> VerifyStep(state, actions)
            OnboardingStep.DONE -> Unit
        }
        if (state.missing.isNotEmpty()) {
            InfoBanner(
                (
                    listOf(stringResource(R.string.business_missing_title)) + state.missing.map {
                        "• $it"
                    }
                    ).joinToString("\n"),
                container = SunsetTint,
                contentColor = Ink,
                tag = "business_missing"
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@StringRes
private fun OnboardingStep.label(): Int = when (this) {
    OnboardingStep.REGISTER -> R.string.business_step_business
    OnboardingStep.LOCATION -> R.string.business_step_location
    OnboardingStep.OPERATION -> R.string.business_step_operation
    OnboardingStep.VERIFY, OnboardingStep.DONE -> R.string.business_step_verify
}

/** 43_NegRegistro. «Tipo de experiencia» y «año de apertura» no existen en la API: no se dibujan (spec §2). */
@Composable
private fun RegisterStep(state: OnboardingUiState, actions: OnboardingActions) {
    val locked = state.draft != null
    TitleBlock(
        stringResource(R.string.business_register_title),
        subtitle = stringResource(R.string.business_register_subtitle)
    )
    LabeledTextField(
        label = stringResource(R.string.business_name_label),
        value = state.name,
        onValueChange = actions.onName,
        placeholder = stringResource(R.string.business_name_placeholder),
        readOnly = locked,
        tag = "business_name"
    )
    Label(R.string.business_category_label)
    ChipGroup {
        state.catalog?.categories.orEmpty().forEach { option ->
            SelectChip(
                option.name,
                state.category == option.id,
                { actions.onCategory(option.id) },
                tag = "business_category_${option.id}",
                enabled = !locked
            )
        }
    }
    LabeledTextField(
        label = stringResource(R.string.business_description_label),
        value = state.description,
        onValueChange = actions.onDescription,
        placeholder = stringResource(R.string.business_description_placeholder),
        singleLine = false,
        readOnly = locked,
        supporting = stringResource(R.string.common_char_count, state.description.length, DESCRIPTION_MAX),
        tag = "business_description"
    )
    if (locked) {
        LabeledTextField(
            label = stringResource(R.string.business_ai_label),
            value = state.aiWords,
            onValueChange = actions.onAiWords,
            placeholder = stringResource(R.string.business_ai_placeholder),
            tag = "business_ai_words"
        )
        WhereverTextLink(stringResource(R.string.business_ai_action), actions.onSuggest)
    }
}

/** 44_NegUbic. Sin mapa (D-34a): ubicación con «Estoy en mi negocio». WhatsApp e Instagram no están en el paso 2. */
@Composable
private fun LocationStep(state: OnboardingUiState, actions: OnboardingActions) {
    val address = state.address
    TitleBlock(
        stringResource(R.string.business_location_title),
        subtitle = stringResource(R.string.business_location_subtitle)
    )
    LabeledTextField(
        label = stringResource(R.string.business_street_label),
        value = address.street,
        onValueChange = { actions.onAddress(address.copy(street = it)) },
        leadingIcon = Icons.Outlined.LocationOn,
        tag = "business_street"
    )
    LabeledTextField(
        label = stringResource(R.string.business_neighborhood_label),
        value = address.neighborhood,
        onValueChange = { actions.onAddress(address.copy(neighborhood = it)) },
        tag = "business_neighborhood"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
        LabeledTextField(
            label = stringResource(R.string.business_city_label),
            value = address.city,
            onValueChange = { actions.onAddress(address.copy(city = it)) },
            modifier = Modifier.weight(2f),
            tag = "business_city"
        )
        LabeledTextField(
            label = stringResource(R.string.business_zip_label),
            value = address.zip,
            onValueChange = { actions.onAddress(address.copy(zip = it.filter(Char::isDigit).take(ZIP_DIGITS))) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            tag = "business_zip"
        )
    }
    LabeledTextField(
        label = stringResource(R.string.business_state_label),
        value = address.state,
        onValueChange = { actions.onAddress(address.copy(state = it)) },
        tag = "business_state"
    )
    SurfaceCard {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            val point = state.point
            Text(
                if (point == null) {
                    stringResource(R.string.business_locate_hint)
                } else {
                    stringResource(R.string.business_located, "%.5f".format(point.lat), "%.5f".format(point.lng))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("business_location_status")
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.business_locate),
                onClick = actions.onLocate,
                enabled = !state.locating,
                icon = Icons.Outlined.LocationOn,
                modifier = Modifier.fillMaxWidth(),
                tag = "business_locate"
            )
        }
    }
}

/** 45_NegOper. Mesas, máximo de personas y anticipo no están en el alta (son de reservas, nv-37): no se dibujan. */
@Composable
private fun OperationStep(state: OnboardingUiState, actions: OnboardingActions) {
    TitleBlock(
        stringResource(R.string.business_operation_title),
        subtitle = stringResource(R.string.business_operation_subtitle)
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Label(R.string.business_schedule_label)
    }
    SurfaceCard { state.schedules.forEach { ScheduleRow(it, actions.onSchedule) } }
    if (state.schedules.any { !it.closed && !it.isValidRange() }) {
        Text(
            stringResource(R.string.business_schedule_invalid),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    Label(R.string.business_price_label)
    SegmentedControl(
        options = (1..PRICE_LEVELS).map { "$".repeat(it) },
        selectedIndex = state.priceLevel - 1,
        onSelect = { actions.onPriceLevel(it + 1) }
    )
    LabeledTextField(
        label = stringResource(R.string.business_average_price_label),
        value = state.averagePrice,
        onValueChange = actions.onAveragePrice,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        tag = "business_average_price"
    )
    Label(R.string.business_services_label)
    ChipGroup {
        state.catalog?.services.orEmpty().forEach { option ->
            SelectChip(option.name, option.id in state.services, { actions.onToggleService(option.id) })
        }
    }
}

@Composable
private fun ScheduleRow(schedule: DaySchedule, onChange: (DaySchedule) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier.fillMaxWidth().heightIn(
            min = Sizes.RowMinHeight
        ).padding(horizontal = Spacing.L, vertical = Spacing.XS)
    ) {
        Text(
            stringResource(DAY_NAMES[schedule.day]),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (!schedule.closed) {
            TimeField(R.string.business_opens, schedule.opens) { onChange(schedule.copy(opens = it)) }
            TimeField(R.string.business_closes, schedule.closes) { onChange(schedule.copy(closes = it)) }
        }
        Switch(
            checked = !schedule.closed,
            onCheckedChange = { open ->
                onChange(
                    if (open) {
                        schedule.copy(
                            closed = false,
                            opens = "09:00",
                            closes = "20:00"
                        )
                    } else {
                        schedule.copy(closed = true)
                    }
                )
            },
            modifier = Modifier.testTag("business_day_${schedule.day}")
        )
    }
}

@Composable
private fun TimeField(@StringRes label: Int, value: String, onChange: (String) -> Unit) {
    LabeledTextField(
        label = stringResource(label),
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == ':' }.take(TIME_LENGTH)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.size(width = Sizes.Thumbnail + Spacing.XXL, height = Sizes.Thumbnail + Spacing.XXXL)
    )
}

/** 46_NegVerif. Logo y constancia fiscal no tienen ruta en el alta: no se dibujan (fiscales: 90_NegFiscal). */
@Composable
private fun VerifyStep(state: OnboardingUiState, actions: OnboardingActions) {
    TitleBlock(
        stringResource(R.string.business_verify_title),
        subtitle = stringResource(R.string.business_verify_subtitle)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
        state.draft?.photos.orEmpty().take(MAX_THUMBS).forEach { photo ->
            AsyncImage(
                model = photo.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(Sizes.Thumbnail).clip(RoundedCornerShape(Radius.M))
            )
        }
        if (state.photoCount < MAX_PHOTOS) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.Thumbnail)
                    .clip(RoundedCornerShape(Radius.M))
                    .border(Strokes.Hairline, MaterialTheme.colorScheme.primary, RoundedCornerShape(Radius.M))
                    .clickable(enabled = !state.uploadingPhoto, role = Role.Button, onClick = actions.onAddPhoto)
                    .testTag("business_add_photo")
            ) {
                if (state.uploadingPhoto) {
                    InlineProgress()
                } else {
                    Icon(
                        Icons.Outlined.Add,
                        stringResource(R.string.business_add_photo),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    Text(
        stringResource(R.string.business_photos_count, state.photoCount, MIN_PHOTOS),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SurfaceCard {
        Text(
            stringResource(R.string.business_documents_label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = Spacing.L, top = Spacing.M)
        )
        DOCUMENTS.forEach { (type, title, hint) ->
            DocumentRow(
                title = stringResource(title),
                hint = stringResource(hint),
                done = type in state.uploadedDocuments || state.draft.hasDocument(type),
                uploading = state.uploadingDocument == type,
                onUpload = { actions.onDocument(type) },
                tag = "business_doc_${type.apiKey}"
            )
        }
    }
    SurfaceCard {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Label(R.string.business_deposit_label)
            LabeledTextField(
                label = stringResource(R.string.business_clabe_label),
                value = state.clabe,
                onValueChange = actions.onClabe,
                placeholder = stringResource(R.string.business_clabe_placeholder),
                error = if (state.clabe.length == CLABE_LENGTH &&
                    !state.clabeValid
                ) {
                    stringResource(R.string.business_clabe_invalid)
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                tag = "business_clabe"
            )
            if (state.clabe.isNotEmpty()) {
                LabeledTextField(
                    label = stringResource(R.string.business_holder_label),
                    value = state.holder,
                    onValueChange = actions.onHolder,
                    tag = "business_holder"
                )
            }
        }
    }
}

private fun BusinessDraft?.hasDocument(type: DocumentType): Boolean = when (type) {
    DocumentType.INE_FRONT, DocumentType.INE_BACK -> this?.hasIne == true
    DocumentType.PROOF_OF_ADDRESS -> this?.hasProofOfAddress == true
}

@Suppress("LongParameterList") // Cada fila de la maqueta cambia título, ayuda, estado y etiqueta.
@Composable
private fun DocumentRow(
    title: String,
    hint: String,
    done: Boolean,
    uploading: Boolean,
    onUpload: () -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(
            min = Sizes.RowMinHeight
        ).padding(horizontal = Spacing.L, vertical = Spacing.S).testTag(tag)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            uploading -> InlineProgress()
            done -> Icon(Icons.Outlined.CheckCircle, stringResource(R.string.business_doc_uploaded), tint = Mint)
            else -> SelectChip(
                stringResource(R.string.business_doc_upload),
                selected = false,
                onClick = onUpload,
                tag = "${tag}_upload"
            )
        }
    }
}

/** 47_NegListo */
@Composable
private fun DoneStep(state: OnboardingUiState, actions: OnboardingActions) {
    val rejected = state.draft?.state == BusinessState.REJECTED
    FormScreen(
        footer = {
            WhereverPrimaryButton(stringResource(R.string.business_go_panel), actions.onPanel, tag = "business_panel")
            if (rejected) {
                WhereverOutlinedButton(
                    text = stringResource(R.string.business_edit_request),
                    onClick = actions.onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "business_edit"
                )
            }
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            ).size(Sizes.AvatarL).background(MintTint, CircleShape)
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Mint,
                modifier = Modifier.size(Sizes.IconXXL)
            )
        }
        Text(
            stringResource(R.string.business_done_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(if (rejected) R.string.business_done_rejected else R.string.business_done_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        SurfaceCard {
            ProgressRow(R.string.business_done_account, R.string.business_done_ready, done = true)
            ProgressRow(R.string.business_done_data, R.string.business_done_ready, done = true)
            ProgressRow(R.string.business_done_documents, R.string.business_done_ready, done = true)
            ProgressRow(R.string.business_done_review, R.string.business_done_in_progress, done = false)
            ProgressRow(R.string.business_done_publish, R.string.business_done_pending, done = false)
        }
        InfoBanner(stringResource(R.string.business_done_meanwhile), container = SunsetTint, contentColor = Ink)
    }
}

@Composable
private fun ProgressRow(@StringRes title: Int, @StringRes status: Int, done: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.RowMinHeight).padding(horizontal = Spacing.L)
    ) {
        Icon(
            if (done) Icons.Outlined.CheckCircle else Icons.Outlined.DateRange,
            contentDescription = null,
            tint = if (done) Mint else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(status),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Label(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private val DAY_NAMES = listOf(
    R.string.business_day_0,
    R.string.business_day_1,
    R.string.business_day_2,
    R.string.business_day_3,
    R.string.business_day_4,
    R.string.business_day_5,
    R.string.business_day_6
)

private val DOCUMENTS = listOf(
    Triple(DocumentType.INE_FRONT, R.string.business_doc_ine_front, R.string.business_doc_hint_ine),
    Triple(DocumentType.INE_BACK, R.string.business_doc_ine_back, R.string.business_doc_hint_ine),
    Triple(DocumentType.PROOF_OF_ADDRESS, R.string.business_doc_proof, R.string.business_doc_hint_proof)
)

private const val ZIP_DIGITS = 5
private const val TIME_LENGTH = 5
private const val MAX_THUMBS = 3
private const val CLABE_LENGTH = 18

// --- Vistas previas ---

private val PREVIEW_CATALOG = Catalog(
    categories = listOf(Option("cafeteria", "Cafetería"), Option("restaurante", "Restaurante"), Option("bar", "Bar")),
    services = listOf(
        Option("terraza", "Terraza"),
        Option("wifi", "Wi-Fi"),
        Option("estacionamiento", "Estacionamiento")
    )
)

private val PREVIEW_DRAFT = BusinessDraft(
    id = "n1",
    name = "Café Raíz",
    category = "cafeteria",
    description = "Cafetería de especialidad con café de olla y desayunos caseros.",
    state = BusinessState.DRAFT,
    step = 5,
    version = 3,
    photos = listOf(BusinessPhoto("f1", "https://ejemplo.invalid/1.jpg"))
)

internal val PREVIEW_ONBOARDING_ACTIONS = OnboardingActions()

private fun previewState(step: OnboardingStep) = OnboardingUiState(
    loading = false,
    step = step,
    catalog = PREVIEW_CATALOG,
    draft = if (step == OnboardingStep.REGISTER) null else PREVIEW_DRAFT,
    name = "Café Raíz",
    category = "cafeteria",
    description = "Cafetería de especialidad con café de olla y desayunos caseros.",
    address = Address("Av. Hidalgo 23", "", "Centro", "Tula de Allende", "Hidalgo", "42800"),
    point = GeoPoint(PREVIEW_LAT, PREVIEW_LNG),
    priceLevel = 2,
    averagePrice = "180",
    services = setOf("terraza"),
    uploadedDocuments = setOf(DocumentType.INE_FRONT)
)

private const val PREVIEW_LAT = 20.0596
private const val PREVIEW_LNG = -99.3431

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessRegisterPreview() {
    WhereverTheme { OnboardingContent(previewState(OnboardingStep.REGISTER), PREVIEW_ONBOARDING_ACTIONS) }
}

@Preview
@Composable
internal fun BusinessLocationPreview() {
    WhereverTheme { OnboardingContent(previewState(OnboardingStep.LOCATION), PREVIEW_ONBOARDING_ACTIONS) }
}

@Preview
@Composable
internal fun BusinessOperationPreview() {
    WhereverTheme { OnboardingContent(previewState(OnboardingStep.OPERATION), PREVIEW_ONBOARDING_ACTIONS) }
}

@Preview
@Composable
internal fun BusinessVerifyPreview() {
    WhereverTheme { OnboardingContent(previewState(OnboardingStep.VERIFY), PREVIEW_ONBOARDING_ACTIONS) }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessDonePreview() {
    WhereverTheme {
        OnboardingContent(
            previewState(OnboardingStep.DONE).copy(draft = PREVIEW_DRAFT.copy(state = BusinessState.IN_REVIEW)),
            PREVIEW_ONBOARDING_ACTIONS
        )
    }
}

@Preview
@Composable
internal fun BusinessMissingPreview() {
    WhereverTheme {
        OnboardingContent(
            previewState(
                OnboardingStep.VERIFY
            ).copy(missing = listOf("Faltan 2 fotos", "Falta el comprobante de domicilio")),
            PREVIEW_ONBOARDING_ACTIONS
        )
    }
}
