package com.example.deltacleaners.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = OnSecondaryBlue,
    secondary = TertiaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    background = Neutral99,
    onBackground = Neutral10,
    surface = Color.White,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral40,
    outline = Neutral90,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Neutral10,
    primaryContainer = BackgroundDark,
    onPrimaryContainer = PrimaryBlueDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = NeutralDarkGray,
    onSurfaceVariant = OnSurfaceDark,
    outline = NeutralDarkGray,
    error = ErrorRed
)

// Add success to Material3 if needed (M3 doesn't have success by default in the scheme)
// but we can use it via our own Color.kt

@Composable
fun DeltaCleanersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
//    if (darkTheme) DarkColorScheme else
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Custom Success Color Mappings for M3-like usage
val ColorScheme.success: Color get() = SuccessGreen
val ColorScheme.onSuccess: Color get() = Color.White
val ColorScheme.warning: Color get() = WarningAmber
val ColorScheme.pending: Color get() = PendingOrange
