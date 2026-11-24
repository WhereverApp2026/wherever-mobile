package com.wherever.mobile.feature.groups.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wherever.mobile.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint as OsmPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Mapa de osmdroid (D25) con los lugares sugeridos y el punto; tocar un pin lo elige. */
@Composable
fun MeetingMap(markers: List<MeetingMarker>, onMarker: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val map = remember { newMeetingMap(context) }
    val pinColor = MaterialTheme.colorScheme.primary.toArgb()
    val chosenColor = MaterialTheme.colorScheme.tertiary.toArgb()
    DisposableEffect(lifecycle, map) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            map.onDetach()
        }
    }
    AndroidView(
        factory = { map },
        update = { view ->
            view.overlays.removeAll { it is Marker }
            markers.forEach { marker ->
                view.overlays.add(
                    Marker(view).apply {
                        position = OsmPoint(marker.lat, marker.lng)
                        title = marker.name
                        icon = meetingPin(view.context, if (marker.chosen) chosenColor else pinColor)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            onMarker(marker.id)
                            true
                        }
                    }
                )
            }
            fit(view, markers)
            view.invalidate()
        },
        modifier = modifier.testTag("meeting_map")
    )
}

private fun fit(view: MapView, markers: List<MeetingMarker>) {
    if (markers.isEmpty()) return
    if (markers.size == 1) {
        view.controller.setZoom(SINGLE_ZOOM)
        view.controller.setCenter(OsmPoint(markers.first().lat, markers.first().lng))
        return
    }
    val box = BoundingBox.fromGeoPoints(markers.map { OsmPoint(it.lat, it.lng) })
    // Se ajusta cuando el mapa ya tiene tamaño.
    view.post { view.zoomToBoundingBox(box.increaseByScale(BOX_MARGIN), false) }
}

private fun meetingPin(context: Context, color: Int): Drawable? =
    ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.mutate()?.apply { setTint(color) }

private fun newMeetingMap(context: Context): MapView {
    Configuration.getInstance().apply {
        // Política de teselas de OSM: agente de usuario propio y caché en el almacenamiento privado.
        userAgentValue = context.packageName
        osmdroidBasePath = context.cacheDir.resolve("osmdroid")
        osmdroidTileCache = context.cacheDir.resolve("osmdroid/tiles")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(SINGLE_ZOOM)
    }
}

private const val SINGLE_ZOOM = 16.0
private const val BOX_MARGIN = 1.6f
