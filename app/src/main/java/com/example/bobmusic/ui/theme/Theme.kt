package com.example.bobmusic.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class AppTheme(val displayName: String) {
    FlamingRed("Flaming Red"),
    Forest("Forest"),
    Sunset("Sunset"),
    Mono("Mono"),

}

@Composable
fun BobMusicTheme(
    appTheme: AppTheme = AppTheme.FlamingRed,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.FlamingRed -> darkColorScheme(
            primary = PurplePrimary,
            background = Color.Black, // PURE BLACK
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF1E1E1E),
            onBackground = PurpleOnBackground,
            onSurface = PurpleOnBackground,
            onSurfaceVariant = PurpleOnBackground
        )
        AppTheme.Forest -> darkColorScheme(
            primary = ForestPrimary,
            background = Color.Black, // PURE BLACK
            surface = Color(0xFF0F1A14),
            surfaceVariant = Color(0xFF1A2D22),
            onBackground = ForestOnBackground,
            onSurface = ForestOnBackground,
            onSurfaceVariant = ForestOnBackground
        )
        AppTheme.Sunset -> darkColorScheme(
            primary = SunsetPrimary,
            background = Color.Black, // PURE BLACK
            surface = Color(0xFF1A120F),
            surfaceVariant = Color(0xFF2D1F19),
            onBackground = SunsetOnBackground,
            onSurface = SunsetOnBackground,
            onSurfaceVariant = SunsetOnBackground
        )
        AppTheme.Mono -> darkColorScheme(
            primary = MonoPrimary,
            background = Color.Black, // PURE BLACK
            surface = Color(0xFF1A1A1A),
            surfaceVariant = Color(0xFF2A2A2A),
            onBackground = MonoOnBackground,
            onSurface = MonoOnBackground,
            onSurfaceVariant = MonoOnBackground
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

/**
 * Frosted "liquid glass" treatment. Use on floating / prominent chrome
 * (nav bar, mini player, login card).
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color? = null,
    alpha: Float = 0.80f,
    shadowElevation: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.22f
): Modifier {
    val base = tint ?: MaterialTheme.colorScheme.surface
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            base.copy(alpha = (alpha + 0.06f).coerceAtMost(1f)),
            base.copy(alpha = alpha)
        )
    )
    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = borderAlpha),
            Color.White.copy(alpha = borderAlpha * 0.08f)
        )
    )
    return this
        .shadow(shadowElevation, shape, clip = false)
        .clip(shape)
        .background(bgBrush)
        .border(borderWidth, borderBrush, shape)
}

/**
 * Simple elevated rounded card for inline content.
 */
@Composable
fun Modifier.softSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color? = null,
    elevation: Dp = 3.dp
): Modifier = this
    .shadow(elevation, shape, clip = false)
    .clip(shape)
    .background(color ?: MaterialTheme.colorScheme.surface)