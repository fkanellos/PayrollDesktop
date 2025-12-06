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
    // ============================================================
    // DISPLAY STYLES - Large promotional text
    // ============================================================
    val displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    val displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )

    val displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    // ============================================================
    // HEADLINE STYLES - Section headings
    // ============================================================
    val headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    val headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )

    val headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

    // ============================================================
    // TITLE STYLES - Card titles, list headers
    // ============================================================
    val titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

    val titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    val titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    // ============================================================
    // BODY STYLES - Main content text
    // ============================================================
    val bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    // ============================================================
    // LABEL STYLES - Form labels, button text
    // ============================================================
    val labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )

    // ============================================================
    // SPECIAL STYLES
    // ============================================================
    val button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val captionBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val overline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )

    val tableHeader = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val tableCell = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val input = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val chip = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val badge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp
    )

    val tooltip = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val money = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

    val moneyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    // ============================================================
    // LEGACY ALIASES (for backward compatibility)
    // ============================================================
    val h1 = displayLarge
    val h2 = headlineLarge
    val h3 = headlineMedium
    val body1 = bodyLarge
    val body2 = bodyMedium
}
private val LightColorScheme = lightColorScheme(
    primary = PayrollColors.Primary,
    onPrimary = PayrollColors.OnPrimary,
    primaryContainer = PayrollColors.Primary.copy(alpha = 0.1f),
    onPrimaryContainer = PayrollColors.Primary,

    secondary = PayrollColors.Secondary,
    onSecondary = PayrollColors.OnSecondary,
    secondaryContainer = PayrollColors.Secondary.copy(alpha = 0.1f),
    onSecondaryContainer = PayrollColors.Secondary,

    background = PayrollColors.Background,
    onBackground = PayrollColors.OnBackground,

    // 🔧 FIX: Όλα τα surface colors σε λευκό!
    surface = Color.White,  // ✅ Main surface
    onSurface = Color(0xFF2D3748),  // Text on surface
    surfaceVariant = Color(0xFFF5F5F5),  // Alternative surface
    onSurfaceVariant = Color(0xFF424242),  // Text on variant
    surfaceContainer = Color.White,  // ✅ Dropdown menus!
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF8F9FA),
    surfaceContainerHighest = Color(0xFFF5F5F5),

    // Outline
    outline = PayrollColors.DividerColor,
    outlineVariant = PayrollColors.DividerColor.copy(alpha = 0.5f),

    // Error
    error = PayrollColors.Error,
    onError = PayrollColors.OnError,
    errorContainer = PayrollColors.Error.copy(alpha = 0.1f),
    onErrorContainer = PayrollColors.Error,

    // Surface tint
    surfaceTint = Color.Transparent  // No tint!
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
    val colorScheme = LightColorScheme

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