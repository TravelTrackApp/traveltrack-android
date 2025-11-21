package week11.st765512.finalproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Ink,
    secondary = TealPrimary,
    onSecondary = Ink,
    background = Ink,
    onBackground = CloudWhite,
    surface = Color(0xFF13202E),
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF1E2C3A),
    onSurfaceVariant = CloudWhite,
    error = Coral,
    onError = CloudWhite
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = CloudWhite,
    primaryContainer = TealLight,
    onPrimaryContainer = Ink,
    secondary = TealDark,
    onSecondary = CloudWhite,
    secondaryContainer = MistBlue,
    onSecondaryContainer = Ink,
    background = SoftGray,
    onBackground = Ink,
    surface = CloudWhite,
    onSurface = Ink,
    surfaceVariant = MistBlue,
    onSurfaceVariant = Slate,
    outline = Slate,
    error = Coral,
    onError = CloudWhite
)

@Composable
fun TravelTrackAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}