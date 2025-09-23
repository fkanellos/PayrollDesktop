package com.payroll.app.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

// Color Palette
object PayrollColors {
    val Primary = Color(0xFF667EEA)
    val PrimaryVariant = Color(0xFF764BA2)
    val Secondary = Color(0xFF03DAC5)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFB00020)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF2D3748)
    val OnSurface = Color(0xFF2D3748)
    val OnError = Color(0xFFFFFFFF)

    // Custom colors for payroll app
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
    val CardBackground = Color(0xFFFFFFFF)
    val DividerColor = Color(0xFFE1E5E9)
    val TextSecondary = Color(0xFF718096)
}

// Typography
object PayrollTypography {
    val h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )

    val h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )

    val h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    )

    val body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    val body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )

    val button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )

    val caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
}
private val LightColorScheme = lightColorScheme(
    primary = PayrollColors.Primary,
    onPrimary = PayrollColors.OnPrimary,
    secondary = PayrollColors.Secondary,
    onSecondary = PayrollColors.OnSecondary,
    background = PayrollColors.Background,
    onBackground = PayrollColors.OnBackground,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceContainer = Color.White,
    error = PayrollColors.Error,
    onError = PayrollColors.OnError,
)

private val DarkColorScheme = darkColorScheme(
    primary = PayrollColors.Primary,
    onPrimary = PayrollColors.OnPrimary,
    secondary = PayrollColors.Secondary,
    onSecondary = PayrollColors.OnSecondary,
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    error = PayrollColors.Error,
    onError = PayrollColors.OnError,
)

@Composable
fun PayrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            displayLarge = PayrollTypography.h1,
            displayMedium = PayrollTypography.h2,
            displaySmall = PayrollTypography.h3,
            bodyLarge = PayrollTypography.body1,
            bodyMedium = PayrollTypography.body2,
            labelLarge = PayrollTypography.button,
            bodySmall = PayrollTypography.caption
        ),
        content = content
    )
}

@Preview
@Composable
fun PayrollThemePreview() {
    PayrollTheme {
        Surface {
            Text(
                text = "Σύστημα Μισθοδοσίας",
                style = PayrollTypography.h1,
                color = PayrollColors.Primary
            )
        }
    }
}