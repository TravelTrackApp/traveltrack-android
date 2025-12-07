/**
 * File: Theme.kt
 * 
 * Material 3 theme configuration for the app. Defines light and dark color schemes
 * and applies theme settings based on system dark mode preference.
 */
package week11.st765512.finalproject.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Ink,
    primaryContainer = TealDark,
    onPrimaryContainer = TealSubtle,
    secondary = Amber,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF3D3222),
    onSecondaryContainer = AmberLight,
    background = Ink,
    onBackground = CloudWhite,
    surface = InkLight,
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF2A2D32),
    onSurfaceVariant = SlateLight,
    outline = Slate,
    outlineVariant = Color(0xFF3A3D42),
    error = Coral,
    onError = CloudWhite,
    errorContainer = Color(0xFF3D2222),
    onErrorContainer = CoralLight
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = CloudWhite,
    primaryContainer = TealSubtle,
    onPrimaryContainer = TealDark,
    secondary = TealDark,
    onSecondary = CloudWhite,
    secondaryContainer = MistBlue,
    onSecondaryContainer = Ink,
    tertiary = Amber,
    onTertiary = Ink,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = Color(0xFF5D4200),
    background = SoftGray,
    onBackground = Ink,
    surface = CloudWhite,
    onSurface = Ink,
    surfaceVariant = MistBlue,
    onSurfaceVariant = Slate,
    outline = SlateLight,
    outlineVariant = MistBlue,
    error = Coral,
    onError = CloudWhite,
    errorContainer = CoralLight,
    onErrorContainer = Color(0xFF8B0000)
)

@Composable
fun TravelTrackAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
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
        content = content
    )
}