package com.wherever.mobile.feature.places.ui

import android.content.Context
import android.content.res.Configuration as UiConfiguration
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.places.domain.MapArea
import com.wherever.mobile.feature.places.domain.MapCell
import com.wherever.mobile.feature.places.domain.MapPin
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Acercamiento inicial: barrio (unos 2 km a la redonda). */
private const val START_ZOOM = 15.0
private const val MAP_DEBOUNCE_MILLIS = 600L

data class ExploreMapNavigation(
    val onBack: () -> Unit = {},
    val onPlace: (String) -> Unit = {},
    /** «A mi plan»: arma un plan con este lugar (CreatePlanRoute con su nombre). */
    val onPlan: (String) -> Unit = {}
)

/** 32_ExpMapa (Figma `292:5280`). Mapa de OpenStreetMap con osmdroid (constitución E-04). */
@Composable
fun ExploreMapRoute(navigation: ExploreMapNavigation, viewModel: ExploreMapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        state.center?.let { center ->
            OsmMap(
                center = OsmPoint(center.lat, center.lng),
                pins = state.pins,
                cells = state.cells,
                onArea = viewModel::onArea,
                onPin = viewModel::select
            )
        }
        MapOverlay(state, navigation, viewModel)
    }
}

@Composable
private fun MapOverlay(state: ExploreMapUiState, navigation: ExploreMapNavigation, viewModel: ExploreMapViewModel) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.padding(ScreenPadding)
        ) {
            BackCircleButton(onClick = navigation.onBack)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                items(MAP_CATEGORIES) { key ->
                    SelectChip(
                        text = stringResource(categoryLabel(key)),
                        selected = state.category == key,
                        onClick = { viewModel.selectCategory(key) },
                        tag = "map_category_$key"
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding)
        ) {
            if (state.moved) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.map_search_here),
                    onClick = viewModel::searchHere,
                    loading = state.loading,
                    tag = "map_search_here"
                )
            }
            state.error?.let { ErrorBanner(it.asString()) }
            if (state.empty) InfoBanner(stringResource(R.string.map_empty), tag = "map_empty")
        }
        Box(modifier = Modifier.weight(1f))
        state.selected?.let { PlaceMapCard(it, navigation) }
        // Atribución obligatoria de los datos de OpenStreetMap (ODbL).
        Text(
            stringResource(R.string.map_attribution),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.End)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = ATTRIBUTION_ALPHA))
                .padding(horizontal = Spacing.S, vertical = Spacing.XS)
                .testTag("map_attribution")
        )
    }
}

private const val ATTRIBUTION_ALPHA = 0.85f

/** Tarjeta del lugar tocado: nombre, giro, calificación, Ver detalle y A mi plan. */
@Composable
internal fun PlaceMapCard(pin: MapPin, navigation: ExploreMapNavigation) {
    SurfaceCard(modifier = Modifier.fillMaxWidth().padding(ScreenPadding).testTag("map_card")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(pin.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.map_card_line, stringResource(categoryLabel(pin.category)), pin.rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.map_see_detail),
                    onClick = { navigation.onPlace(pin.id) },
                    modifier = Modifier.weight(1f),
                    tag = "map_see_detail"
                )
                WhereverOutlinedButton(
                    text = stringResource(R.string.map_to_plan),
                    onClick = { navigation.onPlan(pin.name) },
                    modifier = Modifier.weight(1f),
                    tag = "map_to_plan"
                )
            }
        }
    }
}

/** El `MapView` de osmdroid dentro de Compose, con su ciclo de vida y caché en `cacheDir`. */
@Composable
private fun OsmMap(
    center: OsmPoint,
    pins: List<MapPin>,
    cells: List<MapCell>,
    onArea: (MapArea) -> Unit,
    onPin: (MapPin?) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val map = remember { newMapView(context, center) }
    val pinColor = MaterialTheme.colorScheme.primary.toArgb()
    val cellColor = MaterialTheme.colorScheme.secondary.toArgb()
    DisposableEffect(lifecycle, map) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        map.addMapListener(
            DelayedMapListener(
                object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean = report(map, onArea)

                    override fun onZoom(event: ZoomEvent?): Boolean = report(map, onArea)
                },
                MAP_DEBOUNCE_MILLIS
            )
        )
        // La primera zona sale en cuanto el mapa tiene tamaño.
        map.post { report(map, onArea) }
        onDispose {
            lifecycle.removeObserver(observer)
            map.onDetach()
        }
    }
    AndroidView(
        factory = { map },
        update = { view ->
            view.overlays.removeAll { it is Marker }
            pins.forEach { pin ->
                view.overlays.add(
                    Marker(view).apply {
                        position = OsmPoint(pin.lat, pin.lng)
                        title = pin.name
                        icon = pinIcon(view.context, pinColor)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            onPin(pin)
                            true
                        }
                    }
                )
            }
            cells.forEach { cell ->
                view.overlays.add(
                    Marker(view).apply {
                        position = OsmPoint(cell.lat, cell.lng)
                        title = cell.count.toString()
                        icon = pinIcon(view.context, cellColor)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        // Una celda agrupa lugares: tocarla acerca el mapa a ella.
                        setOnMarkerClickListener { marker, mapView ->
                            mapView.controller.animateTo(marker.position, mapView.zoomLevelDouble + 2, null)
                            true
                        }
                    }
                )
            }
            view.invalidate()
        },
        modifier = Modifier.fillMaxSize().testTag("map_view")
    )
}

/** Pin propio en lugar del de osmdroid, con el color del tema. */
private fun pinIcon(context: Context, color: Int): Drawable? =
    ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.mutate()?.apply { setTint(color) }

private fun report(map: MapView, onArea: (MapArea) -> Unit): Boolean {
    val box = map.boundingBox
    onArea(MapArea.of(box.latSouth, box.lonWest, box.latNorth, box.lonEast, map.zoomLevelDouble))
    return true
}

private fun newMapView(context: Context, center: OsmPoint): MapView {
    Configuration.getInstance().apply {
        // Política de teselas de OSM: agente de usuario propio y caché en el almacenamiento privado.
        userAgentValue = context.packageName
        osmdroidBasePath = context.cacheDir.resolve("osmdroid")
        osmdroidTileCache = context.cacheDir.resolve("osmdroid/tiles")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(START_ZOOM)
        controller.setCenter(center)
        minZoomLevel = MapArea.MIN_ZOOM.toDouble()
        maxZoomLevel = MapArea.MAX_ZOOM.toDouble()
    }
}

private const val PREVIEW_RATING = 4.6

@Preview
@Preview(uiMode = UiConfiguration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlaceMapCardPreview() {
    WhereverTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            PlaceMapCard(MapPin("n1", "Café Tacuba", "cafeteria", PREVIEW_RATING, 0.0, 0.0), ExploreMapNavigation())
        }
    }
}
