// TooManyFunctions: una función por bloque de IAPalabra, 17_AjustaPlan y Generando, más sus vistas previas.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.plans.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InkRaised
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverDialog
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.auth.ui.toPickerDate
import com.wherever.mobile.feature.auth.ui.toPickerMillis
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.tastes.ui.tasteLabelRes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Velo de las tarjetas «de vidrio» sobre el azul de Generando. */
private const val GLASS_ALPHA = 0.16f

/** Línea corta del esqueleto (60 % del ancho). */
private const val SHORT_LINE = 0.6f

/** Pasos aún no hechos en Generando. */
private const val PENDING_ALPHA = 0.6f

/** Gustos que nombra el aviso de IAPalabra («café, tacos y plazas»). */
private const val TASTES_SHOWN = 3

/** Palabras de ejemplo de IAPalabra. */
private val QUICK_WORDS = listOf(
    R.string.quick_tacos,
    R.string.quick_relax,
    R.string.quick_adventure,
    R.string.quick_coffee,
    R.string.quick_friends,
    R.string.quick_cheap,
    R.string.quick_outdoors,
    R.string.quick_romantic
)

private val STEPS = listOf(R.string.generating_1, R.string.generating_2, R.string.generating_3, R.string.generating_4)

internal val DayFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-MX"))
internal val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** IAPalabra → 17_AjustaPlan → Generando 1/4…4/4 → plan propuesto (specs/nv-23). */
@Composable
fun CreatePlanRoute(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onManual: () -> Unit = {},
    viewModel: CreatePlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect(onSaved) }
    BackHandler(enabled = state.phase != CreatePhase.WORD && state.phase != CreatePhase.RESULT) {
        when (state.phase) {
            CreatePhase.ADJUST -> viewModel.backToWord()
            else -> viewModel.cancel()
        }
    }
    CreatePlanContent(
        state = state,
        today = viewModel.today(),
        actions = CreatePlanActions(
            onBack = onBack,
            onPhrase = viewModel::onPhrase,
            onQuickWord = viewModel::onQuickWord,
            onAdjust = viewModel::adjust,
            onBackToWord = viewModel::backToWord,
            onManual = onManual,
            onCompany = viewModel::onCompany,
            onDate = viewModel::onDate,
            onTime = viewModel::onTime,
            onPeople = viewModel::onPeople,
            onStops = viewModel::onStops,
            onPrice = viewModel::onPrice,
            onMobility = viewModel::onMobility,
            onMood = viewModel::onMood,
            onGenerate = viewModel::generate,
            onCancel = viewModel::cancel,
            onShowPlan = viewModel::showPlan,
            onSave = viewModel::save,
            onOtherIdea = viewModel::otherIdea,
            onEdit = viewModel::edit
        )
    )
}

@Composable
internal fun CreatePlanContent(state: CreatePlanUiState, today: LocalDate, actions: CreatePlanActions) {
    when (state.phase) {
        CreatePhase.WORD -> WordStep(state, actions)
        CreatePhase.ADJUST -> AdjustStep(state, today, actions)
        CreatePhase.GENERATING, CreatePhase.READY -> Generating(state, actions)
        CreatePhase.RESULT -> DraftResult(state, actions)
    }
}

@Composable
private fun sparkleIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_wh_sparkle)

/** IAPalabra: una palabra basta. */
@Composable
private fun WordStep(state: CreatePlanUiState, actions: CreatePlanActions) {
    FormScreen(
        footer = {
            WhereverTextLink(
                stringResource(R.string.plan_adjust_details),
                actions.onAdjust,
                Modifier.testTag("plan_adjust_details")
            )
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_generate),
                onClick = actions.onGenerate,
                enabled = state.canGenerate,
                icon = sparkleIcon(),
                modifier = Modifier.fillMaxWidth(),
                tag = "plan_generate"
            )
        }
    ) {
        BackCircleButton(onClick = actions.onBack)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.Thumbnail).background(SunsetTint, CircleShape)
        ) {
            Icon(sparkleIcon(), contentDescription = null, tint = Sunset, modifier = Modifier.size(Sizes.IconXL))
        }
        Text(
            text = stringResource(R.string.plan_word_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.plan_word_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LabeledTextField(
            label = stringResource(R.string.plan_word_label),
            value = state.phrase,
            onValueChange = actions.onPhrase,
            placeholder = stringResource(R.string.plan_word_placeholder),
            leadingIcon = sparkleIcon(),
            supporting = stringResource(R.string.common_char_count, state.phrase.length, PHRASE_MAX),
            tag = "plan_phrase"
        )
        Text(
            text = stringResource(R.string.plan_word_examples),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        ChipGroup {
            QUICK_WORDS.forEach { word ->
                val text = stringResource(word)
                SelectChip(text, state.phrase.equals(text, ignoreCase = true), { actions.onQuickWord(text) })
            }
        }
        TastesBanner(state.tastes)
        Notices(state)
    }
}

/** «Ya conozco tus gustos: café, tacos y plazas»: solo con gustos y `aprendizaje_personalizado`. */
@Composable
private fun TastesBanner(tastes: List<String>) {
    val names = tastes.mapNotNull { key -> tasteLabelRes(key) }.take(TASTES_SHOWN).map { label ->
        // Las etiquetas de gustos llevan un emoji al inicio («☕ Café»): aquí va solo el nombre, en minúsculas.
        stringResource(label).substringAfter(' ').lowercase(Locale.forLanguageTag("es-MX"))
    }
    if (names.isEmpty()) return
    val list = if (names.size == 1) {
        names.first()
    } else {
        stringResource(R.string.plan_tastes_and, names.dropLast(1).joinToString(", "), names.last())
    }
    InfoBanner(stringResource(R.string.plan_tastes_known, list), tag = "plan_tastes")
}

@Composable
private fun Notices(state: CreatePlanUiState) {
    if (state.noLocation) InfoBanner(stringResource(R.string.plan_needs_location), tag = "plan_no_location")
    state.error?.let { ErrorBanner(it.asString()) }
}

/** 17_AjustaPlan. */
@Composable
private fun AdjustStep(state: CreatePlanUiState, today: LocalDate, actions: CreatePlanActions) {
    var pickDate by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_generate),
                onClick = actions.onGenerate,
                enabled = state.canGenerate,
                icon = sparkleIcon(),
                modifier = Modifier.fillMaxWidth(),
                tag = "plan_generate"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBackToWord)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(
                    text = stringResource(R.string.plan_adjust_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.plan_adjust_for, state.phrase.trim()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        SectionLabel(R.string.plan_who)
        ChipGroup {
            Company.entries.forEach { company ->
                SelectChip(
                    stringResource(companyLabel(company)),
                    state.company == company,
                    { actions.onCompany(company) },
                    tag = "plan_company_${company.name.lowercase()}"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            PickerField(
                label = stringResource(R.string.plan_day),
                value = state.date?.format(DayFormat)?.replace(".", "")?.replaceFirstChar { it.uppercase() }
                    ?: stringResource(R.string.plan_today),
                icon = Icons.Outlined.DateRange,
                onClick = { pickDate = true },
                modifier = Modifier.weight(1f).testTag("plan_day")
            )
            PickerField(
                label = stringResource(R.string.plan_start_at),
                value = state.time?.format(TimeFormat) ?: stringResource(R.string.plan_now),
                icon = null,
                onClick = { pickTime = true },
                modifier = Modifier.weight(1f).testTag("plan_time")
            )
        }
        if (state.invalidDateTime) ErrorBanner(stringResource(R.string.plan_date_invalid))
        SurfaceCard {
            StepperRow(
                title = stringResource(R.string.plan_people_title),
                subtitle = stringResource(R.string.plan_people_hint),
                value = state.people,
                range = 1..MAX_PEOPLE,
                onChange = actions.onPeople,
                tag = "plan_people"
            )
            HorizontalDivider(Modifier.padding(horizontal = Spacing.L), color = MaterialTheme.colorScheme.outline)
            StepperRow(
                title = stringResource(R.string.plan_stops_title),
                subtitle = stringResource(R.string.plan_stops_hint, AI_MIN_STOPS, AI_MAX_STOPS),
                value = state.stops,
                range = AI_MIN_STOPS..AI_MAX_STOPS,
                onChange = actions.onStops,
                tag = "plan_stops"
            )
        }
        SectionLabel(R.string.plan_budget)
        SegmentedControl(
            options = PriceLevel.entries.map { stringResource(priceLabel(it)) },
            selectedIndex = state.price?.ordinal ?: -1,
            onSelect = { actions.onPrice(PriceLevel.entries[it]) },
            modifier = Modifier.testTag("plan_price")
        )
        SectionLabel(R.string.plan_mobility)
        ChipGroup {
            Mobility.entries.forEach { mobility ->
                SelectChip(stringResource(mobilityLabel(mobility)), state.mobility == mobility, {
                    actions.onMobility(mobility)
                })
            }
        }
        if (state.moodAllowed) {
            SectionLabel(R.string.plan_mood)
            ChipGroup {
                Mood.entries.forEach { mood ->
                    SelectChip(stringResource(moodLabel(mood)), state.mood == mood, {
                        actions.onMood(mood)
                    }, tag = "mood_${mood.name}")
                }
            }
        }
        Notices(state)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            WhereverTextLink(
                stringResource(R.string.plan_manual_link),
                actions.onManual,
                Modifier.testTag("plan_manual")
            )
        }
    }
    if (pickDate) {
        PlanDatePicker(today, state.date ?: today, { pickDate = false }) {
            pickDate = false
            actions.onDate(it)
        }
    }
    if (pickTime) {
        PlanTimePicker(state.time, { pickTime = false }) {
            pickTime = false
            actions.onTime(it)
        }
    }
}

@Composable
internal fun SectionLabel(@StringRes text: Int) {
    Text(text = stringResource(text), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

/** Campo que abre un selector (día u hora), con el mismo aspecto que los campos de texto. */
@Composable
internal fun PickerField(
    label: String,
    value: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(Radius.Field),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.FieldHeight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Spacing.L, vertical = Sizes.FieldVerticalPadding)
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Sizes.IconM)
                    )
                    Spacer(Modifier.width(Spacing.M))
                }
                Text(text = value, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            }
        }
    }
}

/** Personas o paradas con − y +. */
@Composable
private fun StepperRow(
    title: String,
    subtitle: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.L, vertical = Spacing.S)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CircleIconButton(
            icon = ImageVector.vectorResource(R.drawable.ic_wh_minus),
            description = stringResource(R.string.plan_less, title),
            onClick = { if (value > range.first) onChange(value - 1) },
            modifier = Modifier.testTag("${tag}_less")
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.M).testTag("${tag}_value")
        )
        CircleIconButton(
            icon = Icons.Outlined.Add,
            description = stringResource(R.string.plan_more, title),
            onClick = { if (value < range.last) onChange(value + 1) },
            modifier = Modifier.testTag("${tag}_more")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlanDatePicker(
    today: LocalDate,
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPicked: (LocalDate) -> Unit
) {
    val last = today.plusDays(MAX_DAYS_AHEAD)
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toPickerMillis(),
        yearRange = today.year..last.year,
        selectableDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis.toPickerDate() in today..last

                override fun isSelectableYear(year: Int) = year in today.year..last.year
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            WhereverTextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { pickerState.selectedDateMillis?.let { onPicked(it.toPickerDate()) } }
            ) { Text(stringResource(R.string.common_accept)) }
        },
        dismissButton = { WhereverTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlanTimePicker(initial: LocalTime?, onDismiss: () -> Unit, onPicked: (LocalTime) -> Unit) {
    val start = initial ?: DEFAULT_START
    val pickerState = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = true)
    WhereverDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            WhereverTextButton(onClick = { onPicked(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = { WhereverTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        title = { Text(stringResource(R.string.plan_start_at)) },
        text = { TimePicker(state = pickerState) }
    )
}

/** Generando 1/4 a 4/4: tres pasos mientras responde la API y «¡Plan listo!» con Ver mi plan. */
@Composable
private fun Generating(state: CreatePlanUiState, actions: CreatePlanActions) {
    val ready = state.phase == CreatePhase.READY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RouteBlue, InkRaised)))
            .testTag(if (ready) "plan_ready" else "plan_generating")
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(ScreenPadding)) {
            GeneratingBody(state, ready, Modifier.weight(1f))
            WhereverOutlinedButton(
                text = stringResource(if (ready) R.string.plan_show else R.string.common_cancel),
                onClick = if (ready) actions.onShowPlan else actions.onCancel,
                contentColor = Mist,
                borderColor = Mist,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.M),
                tag = if (ready) "plan_show" else "plan_cancel"
            )
        }
    }
}

@Composable
private fun GeneratingBody(state: CreatePlanUiState, ready: Boolean, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassChip(text = state.phrase.trim(), icon = Icons.Outlined.Search)
            Spacer(Modifier.weight(1f))
            state.company?.let { GlassChip(text = stringResource(companyLabel(it)), icon = null) }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.CreateTile).background(Gold, CircleShape)
        ) {
            Icon(sparkleIcon(), contentDescription = null, tint = Ink, modifier = Modifier.size(Sizes.IconL))
        }
        Text(
            text = stringResource(if (ready) R.string.plan_ready_title else R.string.generating_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Mist,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        Text(stringResource(R.string.generating_subtitle), style = MaterialTheme.typography.bodyLarge, color = Mist)
        STEPS.forEachIndexed { index, label ->
            val status = when {
                ready || index < state.step -> StepStatus.DONE
                index == state.step -> StepStatus.ACTIVE
                else -> StepStatus.PENDING
            }
            StepRow(stringResource(label), status, index)
        }
        WhereverProgressBar(
            progress = { if (ready) 1f else (state.step + 1f) / (GENERATING_STEPS + 1f) },
            color = Gold,
            modifier = Modifier.fillMaxWidth()
        )
        repeat(2) { GlassSkeleton() }
    }
}

private enum class StepStatus { DONE, ACTIVE, PENDING }

@Composable
private fun StepRow(text: String, status: StepStatus, index: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (status == StepStatus.PENDING) PENDING_ALPHA else 1f)
            .testTag("plan_step_${index}_${status.name.lowercase()}")
    ) {
        when (status) {
            StepStatus.DONE -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.IconL).background(Mint, CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Mist,
                    modifier = Modifier.size(Sizes.IconS)
                )
            }
            StepStatus.ACTIVE -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.IconL).border(Strokes.Thin, Gold, CircleShape)
            ) {
                Box(Modifier.size(Sizes.Dot).background(Gold, CircleShape))
            }
            StepStatus.PENDING -> Box(Modifier.size(Sizes.IconL).border(Strokes.Thin, Mist, CircleShape))
        }
        Spacer(Modifier.width(Spacing.M))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (status == StepStatus.ACTIVE) FontWeight.Bold else FontWeight.Normal,
            color = Mist
        )
    }
}

@Composable
private fun GlassChip(text: String, icon: ImageVector?) {
    Surface(shape = CircleShape, color = Mist.copy(alpha = GLASS_ALPHA), contentColor = Mist) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = Sizes.AvatarS).padding(horizontal = Spacing.L)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.IconS))
                Spacer(Modifier.width(Spacing.S))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (icon != null) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Tarjetas vacías que anticipan las paradas mientras se arma el plan. */
@Composable
private fun GlassSkeleton() {
    Surface(shape = RoundedCornerShape(Radius.L), color = Mist.copy(alpha = GLASS_ALPHA)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(Spacing.L)) {
            Box(
                Modifier.size(Sizes.CreateTile).background(Mist.copy(alpha = GLASS_ALPHA), RoundedCornerShape(Radius.M))
            )
            Spacer(Modifier.width(Spacing.L))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S), modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.fillMaxWidth().height(Sizes.SkeletonLineL)
                        .background(Mist.copy(alpha = GLASS_ALPHA), CircleShape)
                )
                Box(
                    Modifier.fillMaxWidth(fraction = SHORT_LINE).height(Sizes.SkeletonLineS)
                        .background(Mist.copy(alpha = GLASS_ALPHA), CircleShape)
                )
            }
        }
    }
}

/** El plan que propuso la API: Guardar, Otra idea o Ajustar (nv-48). */
@Composable
private fun DraftResult(state: CreatePlanUiState, actions: CreatePlanActions) {
    val draft = state.draft ?: return
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_save),
                onClick = actions.onSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "plan_save"
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.plan_other_idea),
                onClick = actions.onOtherIdea,
                icon = Icons.Outlined.Refresh,
                modifier = Modifier.fillMaxWidth(),
                tag = "plan_other_idea"
            )
        }
    ) {
        TopTitleRow(draft.name.ifBlank { stringResource(R.string.plan_word_title) }, actions.onBack)
        if (draft.source == SOURCE_RULES) {
            InfoBanner(
                draft.notice ?: stringResource(R.string.plan_rules_notice),
                icon = Icons.Outlined.Info,
                tag = "plan_rules"
            )
        }
        draft.parameters?.let { params -> UnderstoodChips(params, draft.people) }
        state.error?.let { ErrorBanner(it.asString()) }
        CostSummary(draft.totalCost, draft.costPerPerson, draft.people)
        Timeline(stops = draft.stops, transfers = draft.transfers, mobility = draft.mobility)
        WhereverTextLink(stringResource(R.string.plan_adjust), actions.onEdit)
    }
}

internal const val SOURCE_RULES = "reglas"

/** Hoy en las vistas previas y capturas (sábado 17 de octubre de 2026, como la maqueta). */
@Suppress("MagicNumber") // Fecha de la maqueta.
private val PREVIEW_TODAY: LocalDate = LocalDate.of(2026, 10, 17)

@Preview(name = "IAPalabra claro")
@Preview(name = "IAPalabra oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlanWordPreview() {
    WhereverTheme {
        CreatePlanContent(
            CreatePlanUiState(phrase = "tacos", tastes = listOf("cafes", "tacos", "parques")),
            PREVIEW_TODAY,
            CreatePlanActions()
        )
    }
}

@Preview(name = "17_AjustaPlan claro")
@Preview(name = "17_AjustaPlan oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlanAdjustPreview() {
    WhereverTheme {
        CreatePlanContent(
            CreatePlanUiState(
                phrase = "Viernes de tacos",
                phase = CreatePhase.ADJUST,
                company = Company.COUPLE,
                date = PREVIEW_TODAY.plusDays(1),
                time = DEFAULT_START,
                price = PriceLevel.MEDIUM
            ),
            PREVIEW_TODAY,
            CreatePlanActions()
        )
    }
}

@Preview(name = "Generando 1/4")
@Composable
internal fun PlanGeneratingPreview() {
    WhereverTheme {
        CreatePlanContent(
            CreatePlanUiState(phrase = "Tacos", company = Company.COUPLE, phase = CreatePhase.GENERATING),
            PREVIEW_TODAY,
            CreatePlanActions()
        )
    }
}

@Preview(name = "Generando 3/4")
@Composable
internal fun PlanGeneratingStep3Preview() {
    WhereverTheme {
        CreatePlanContent(
            CreatePlanUiState(phrase = "Tacos", company = Company.COUPLE, phase = CreatePhase.GENERATING, step = 2),
            PREVIEW_TODAY,
            CreatePlanActions()
        )
    }
}

@Preview(name = "Generando 4/4")
@Composable
internal fun PlanReadyPreview() {
    WhereverTheme {
        CreatePlanContent(
            CreatePlanUiState(phrase = "Tacos", company = Company.COUPLE, phase = CreatePhase.READY, step = 3),
            PREVIEW_TODAY,
            CreatePlanActions()
        )
    }
}
