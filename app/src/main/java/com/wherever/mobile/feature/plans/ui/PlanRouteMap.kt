package com.wherever.mobile.feature.plans.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.feature.plans.domain.RoutePoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint as OsmPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private const val SINGLE_ZOOM = 16.0
private const val BOX_MARGIN = 1.5f
private const val LABEL_SCALE = 0.5f
private const val ROUTE_WIDTH_SCALE = 2f

/** Color de cada parada como en la maqueta: 1 naranja, 2 verde, 3 azul, y vuelve a empezar. */
internal fun stopColor(number: Int): Color = when ((number - 1) % 3) {
    0 -> Sunset
    1 -> Mint
    else -> RouteBlue
}

/** Texto sobre el color de la parada (oscuro sobre el naranja, claro sobre los demás). */
internal fun onStopColor(number: Int): Color = if ((number - 1) % 3 == 0) Ink else Color.White

/**
 * Mapa de osmdroid (D25, E-04) con un pin numerado por parada y una línea recta entre ellas en orden.
 * La línea no sigue calles: no hay proveedor de rutas (specs/nv-30, D-30f).
 */
@Composable
fun PlanRouteMap(points: List<RoutePoint>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val map = remember { newRouteMap(context) }
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
            view.overlays.clear()
            val geo = points.map { OsmPoint(it.lat, it.lng) }
            if (geo.size > 1) {
                view.overlays.add(
                    Polyline(view).apply {
                        setPoints(geo)
                        outlinePaint.color = RouteBlue.toArgb()
                        outlinePaint.strokeWidth = with(density) { Strokes.Thin.toPx() } * ROUTE_WIDTH_SCALE
                    }
                )
            }
            points.forEach { point ->
                view.overlays.add(
                    Marker(view).apply {
                        position = OsmPoint(point.lat, point.lng)
                        title = point.stop.name
                        icon = numberPin(view.context, density, point.number)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                )
            }
            fit(view, geo)
            view.invalidate()
        },
        modifier = modifier.testTag("plan_route_map")
    )
}

private fun fit(view: MapView, points: List<OsmPoint>) {
    when {
        points.isEmpty() -> Unit
        points.size == 1 -> {
            view.controller.setZoom(SINGLE_ZOOM)
            view.controller.setCenter(points.first())
        }
        else -> {
            val box = BoundingBox.fromGeoPoints(points)
            // Se ajusta cuando el mapa ya tiene tamaño.
            view.post { view.zoomToBoundingBox(box.increaseByScale(BOX_MARGIN), false) }
        }
    }
}

/** Círculo del color de la parada con su número, del tamaño de `Sizes.StepBadge`. */
private fun numberPin(context: Context, density: Density, number: Int): BitmapDrawable {
    val size = with(density) { Sizes.StepBadge.roundToPx() }
    val ring = with(density) { Strokes.Thin.toPx() } * ROUTE_WIDTH_SCALE
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = size / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.White.toArgb()
    canvas.drawCircle(radius, radius, radius, paint)
    paint.color = stopColor(number).toArgb()
    canvas.drawCircle(radius, radius, radius - ring, paint)
    paint.color = onStopColor(number).toArgb()
    paint.textSize = size * LABEL_SCALE
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER
    val baseline = radius - (paint.descent() + paint.ascent()) / 2
    canvas.drawText(number.toString(), radius, baseline, paint)
    return BitmapDrawable(context.resources, bitmap)
}

private fun newRouteMap(context: Context): MapView {
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
