package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Norma de medidas (docs/diseno.md). Fuera de core/designsystem no se escribe ningún `dp` ni `sp`:
// la regla de detekt `HardcodedDesignValue` lo bloquea. Objetos: Spacing, Radius, WhereverShapes,
// Strokes, Elevations y Sizes.

/** Espaciado: escala de 4/8 dp. */
object Spacing {
    val None: Dp = 0.dp
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp
    val L: Dp = 16.dp

    /** Margen lateral de la pantalla (maqueta: 20 dp en un lienzo de 390). */
    val XL: Dp = 20.dp
    val XXL: Dp = 24.dp
    val XXXL: Dp = 32.dp
    val Huge: Dp = 48.dp
    val Giant: Dp = 64.dp
}

/** Radios de esquina. Boceto 2: campos 14, tarjetas 18, píldora en botones (docs/diseno_boceto2.md §4.1). */
object Radius {
    val XS: Dp = 4.dp
    val S: Dp = 8.dp
    val M: Dp = 12.dp

    /** Campo de texto. */
    val Field: Dp = 14.dp
    val L: Dp = 16.dp

    /** Tarjeta. */
    val Card: Dp = 18.dp
    val XL: Dp = 20.dp
    val XXL: Dp = 24.dp
    val XXXL: Dp = 32.dp
}

/** Formas listas para usar. */
object WhereverShapes {
    val Small = RoundedCornerShape(Radius.S)
    val Medium = RoundedCornerShape(Radius.M)
    val Field = RoundedCornerShape(Radius.Field)
    val Card = RoundedCornerShape(Radius.Card)
    val Sheet = RoundedCornerShape(topStart = Radius.XXL, topEnd = Radius.XXL)
    val Pill = CircleShape
}

/** Grosor de bordes, divisores e indicadores. */
object Strokes {
    val Hairline: Dp = 1.dp
    val Thin: Dp = 2.dp
    val Thick: Dp = 4.dp
}

/** Elevaciones: la maqueta usa sombras suaves. */
object Elevations {
    val None: Dp = 0.dp
    val Card: Dp = 2.dp
    val Raised: Dp = 6.dp
    val Overlay: Dp = 8.dp
}

/** Tamaños de controles, íconos, avatares e imágenes. */
object Sizes {
    /** Área táctil mínima (constitución, accesibilidad). */
    val TouchTarget: Dp = 48.dp
    val ButtonHeight: Dp = 52.dp
    val RowMinHeight: Dp = 52.dp

    /** Campo de texto del Boceto 2: 52 de alto = 14 + línea de 24 + 14. */
    val FieldHeight: Dp = 52.dp
    val FieldVerticalPadding: Dp = 14.dp

    val IconXS: Dp = 12.dp
    val IconS: Dp = 16.dp
    val IconM: Dp = 20.dp
    val IconL: Dp = 24.dp
    val IconXL: Dp = 32.dp
    val IconXXL: Dp = 40.dp

    /** Círculo numerado de una parada o paso. */
    val StepBadge: Dp = 28.dp
    val EditBadge: Dp = 32.dp
    val ProgressLarge: Dp = 56.dp
    val SkeletonLineL: Dp = 16.dp
    val SkeletonLineS: Dp = 12.dp

    val ProgressSmall: Dp = 24.dp
    val BarHeight: Dp = 8.dp
    val Dot: Dp = 8.dp

    val AvatarS: Dp = 40.dp
    val AvatarList: Dp = 44.dp
    val AvatarM: Dp = 56.dp
    val AvatarL: Dp = 88.dp
    val AvatarXL: Dp = 96.dp

    val Thumbnail: Dp = 64.dp

    /** Cuadro del ícono en las filas de la hoja 15_Crear. */
    val CreateTile: Dp = 52.dp

    /** 10_Inicio: portada (390 × 244), tarjeta-foto de "Cerca de ti" (160 × 184) y de "Planes de tus amigos". */
    val HomeCover: Dp = 244.dp
    val NearbyCardWidth: Dp = 160.dp
    val NearbyCardHeight: Dp = 184.dp
    val FriendPlanCardWidth: Dp = 294.dp
    val FriendPlanPhoto: Dp = 118.dp

    /** Botón redondo dentro del buscador (chispa de IA de 10_Inicio). */
    val SearchAction: Dp = 44.dp

    /** Ancho máximo del QR de Mi código (25_MiCodigo). */
    val QrCode: Dp = 280.dp
    val ThumbnailL: Dp = 72.dp
    val CardImage: Dp = 132.dp
    val HeroImage: Dp = 240.dp

    /** Mapa dentro de una pantalla con más contenido (47_PuntoMedio). */
    val MapPreview: Dp = 220.dp
    val BadgeL: Dp = 64.dp
    val GalleryPhotoWidth: Dp = 300.dp
    val EventCardWidth: Dp = 240.dp
    val WelcomeIllustration: Dp = 360.dp

    /** Alto mínimo del texto y botones de Bienvenida para dibujar la ilustración encima. */
    val WelcomeContent: Dp = 420.dp
    val LabelColumn: Dp = 120.dp
    val ChartHeight: Dp = 72.dp

    /** Ancho máximo del contenido en pantallas grandes. */
    val ContentMaxWidth: Dp = 520.dp
    val DialogMaxWidth: Dp = 360.dp
}
