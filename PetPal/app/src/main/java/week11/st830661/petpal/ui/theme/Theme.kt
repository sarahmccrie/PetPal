package week11.st830661.petpal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PetGreen,
    onPrimary = PetCard,
    primaryContainer = PetFieldBackground,
    onPrimaryContainer = PetTextDark,

    secondary = PetGreenDark,
    onSecondary = PetCard,

    background = PetBackground,
    onBackground = PetTextDark,

    surface = PetCard,
    onSurface = PetTextDark,

    error = PetError,
    onError = PetCard
)

private val DarkColorScheme = darkColorScheme(
    primary = PetGreen,
    onPrimary = PetCard,
    primaryContainer = PetGreenDark,
    onPrimaryContainer = PetCard,

    background = PetTextDark,
    onBackground = PetCard,

    surface = PetTextDark,
    onSurface = PetCard,

    error = PetError,
    onError = PetCard
)

@Composable
fun PetPalTheme(
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
