package dev.qtremors.filion.theme

import android.app.Activity
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.qtremors.filion.settings.ThemeMode

@Composable
fun FilionTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val effectivelyDark = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK,
        ThemeMode.OLED -> true
    }

    val dynamicColorAvailable = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        themeMode == ThemeMode.OLED && dynamicColorAvailable -> {
            dynamicDarkColorScheme(context).copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF121212),
                surfaceContainerLowest = Color.Black,
                surfaceContainerLow = Color(0xFF0A0A0A),
                surfaceContainer = Color(0xFF111111),
                surfaceContainerHigh = Color(0xFF1A1A1A),
                surfaceContainerHighest = Color(0xFF222222),
                onBackground = Color.White,
                onSurface = Color.White
            )
        }
        themeMode == ThemeMode.OLED -> OledColorScheme
        dynamicColorAvailable -> {
            if (effectivelyDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectivelyDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !effectivelyDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !effectivelyDark
        }
    }

    val reducedMotionEnabled = remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }

    val semanticColors = if (effectivelyDark) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSemanticColors provides semanticColors,
        LocalReducedMotionEnabled provides reducedMotionEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}
