package com.payroll.app.desktop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Centralized dimensions for the Payroll App
 * All spacing, sizing, and padding values should be referenced from here
 */
object AppDimensions {

    // ============================================================
    // SPACING - Used for gaps between elements
    // ============================================================
    object Spacing {
        val none: Dp = 0.dp
        val xxs: Dp = 2.dp      // Minimal spacing
        val xs: Dp = 4.dp       // Extra small
        val sm: Dp = 8.dp       // Small
        val md: Dp = 12.dp      // Medium
        val lg: Dp = 16.dp      // Large
        val xl: Dp = 20.dp      // Extra large
        val xxl: Dp = 24.dp     // 2x Extra large
        val xxxl: Dp = 32.dp    // 3x Extra large
        val huge: Dp = 48.dp    // Huge spacing
        val massive: Dp = 64.dp // Massive spacing
    }

    // ============================================================
    // PADDING - Used for internal component padding
    // ============================================================
    object Padding {
        val none: Dp = 0.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 20.dp
        val xxl: Dp = 24.dp
        val card: Dp = 24.dp        // Standard card padding
        val screen: Dp = 24.dp      // Screen edge padding
        val dialog: Dp = 24.dp      // Dialog padding
        val button: Dp = 16.dp      // Button internal padding
        val input: Dp = 12.dp       // Input field padding
        val listItem: Dp = 16.dp    // List item padding
    }

    // ============================================================
    // SIZES - Component sizes
    // ============================================================
    object Size {
        // Icons
        val iconXs: Dp = 12.dp
        val iconSm: Dp = 16.dp
        val iconMd: Dp = 20.dp
        val iconLg: Dp = 24.dp
        val iconXl: Dp = 32.dp
        val iconXxl: Dp = 48.dp
        val iconHuge: Dp = 64.dp

        // Buttons
        val buttonHeight: Dp = 48.dp
        val buttonHeightSm: Dp = 36.dp
        val buttonHeightLg: Dp = 56.dp
        val buttonMinWidth: Dp = 100.dp
        val iconButton: Dp = 36.dp

        // Progress Indicators
        val progressSm: Dp = 16.dp
        val progressMd: Dp = 24.dp
        val progressLg: Dp = 32.dp
        val progressXl: Dp = 48.dp

        // Dividers
        val dividerThickness: Dp = 1.dp
        val dividerThicknessBold: Dp = 2.dp

        // Avatars
        val avatarSm: Dp = 32.dp
        val avatarMd: Dp = 40.dp
        val avatarLg: Dp = 56.dp
        val avatarXl: Dp = 80.dp

        // Cards
        val cardElevation: Dp = 2.dp
        val cardElevationHover: Dp = 4.dp
        val cardElevationPressed: Dp = 1.dp

        // Panels
        val sidebarWidth: Dp = 320.dp
        val sidebarWidthCollapsed: Dp = 72.dp
        val dialogWidth: Dp = 400.dp
        val dialogWidthLg: Dp = 600.dp

        // Touch targets
        val minTouchTarget: Dp = 48.dp

        // Stroke widths
        val strokeThin: Dp = 1.dp
        val strokeMedium: Dp = 2.dp
        val strokeThick: Dp = 3.dp
    }

    // ============================================================
    // CORNER RADIUS
    // ============================================================
    object Corner {
        val none: Dp = 0.dp
        val xs: Dp = 4.dp
        val sm: Dp = 6.dp
        val md: Dp = 8.dp
        val lg: Dp = 10.dp
        val xl: Dp = 12.dp
        val xxl: Dp = 16.dp
        val round: Dp = 24.dp
        val full: Dp = 999.dp   // Fully rounded (pill shape)

        // Pre-built shapes
        val cardShape = RoundedCornerShape(xl)
        val buttonShape = RoundedCornerShape(md)
        val inputShape = RoundedCornerShape(md)
        val chipShape = RoundedCornerShape(round)
        val dialogShape = RoundedCornerShape(xl)
        val bottomSheetShape = RoundedCornerShape(topStart = xxl, topEnd = xxl)
    }

    // ============================================================
    // TEXT SIZES
    // ============================================================
    object TextSize {
        // Display
        val displayLarge: TextUnit = 32.sp
        val displayMedium: TextUnit = 28.sp
        val displaySmall: TextUnit = 24.sp

        // Headlines
        val headlineLarge: TextUnit = 24.sp
        val headlineMedium: TextUnit = 20.sp
        val headlineSmall: TextUnit = 18.sp

        // Titles
        val titleLarge: TextUnit = 18.sp
        val titleMedium: TextUnit = 16.sp
        val titleSmall: TextUnit = 14.sp

        // Body
        val bodyLarge: TextUnit = 16.sp
        val bodyMedium: TextUnit = 14.sp
        val bodySmall: TextUnit = 12.sp

        // Labels
        val labelLarge: TextUnit = 14.sp
        val labelMedium: TextUnit = 12.sp
        val labelSmall: TextUnit = 11.sp

        // Captions
        val caption: TextUnit = 12.sp
        val captionSmall: TextUnit = 10.sp

        // Specific use cases
        val button: TextUnit = 14.sp
        val input: TextUnit = 14.sp
        val tableHeader: TextUnit = 14.sp
        val tableCell: TextUnit = 14.sp
        val chipLabel: TextUnit = 12.sp
        val badge: TextUnit = 10.sp
        val tooltip: TextUnit = 12.sp
    }

    // ============================================================
    // LINE HEIGHTS
    // ============================================================
    object LineHeight {
        val tight: TextUnit = 16.sp
        val normal: TextUnit = 20.sp
        val relaxed: TextUnit = 24.sp
        val loose: TextUnit = 28.sp
    }

    // ============================================================
    // ELEVATION LEVELS
    // ============================================================
    object Elevation {
        val none: Dp = 0.dp
        val xs: Dp = 1.dp
        val sm: Dp = 2.dp
        val md: Dp = 4.dp
        val lg: Dp = 8.dp
        val xl: Dp = 16.dp

        // Semantic elevations
        val card: Dp = sm
        val dropdown: Dp = md
        val dialog: Dp = lg
        val tooltip: Dp = md
        val fab: Dp = lg
    }

    // ============================================================
    // Z-INDEX (for layering)
    // ============================================================
    object ZIndex {
        const val base: Float = 0f
        const val dropdown: Float = 100f
        const val sticky: Float = 200f
        const val fixed: Float = 300f
        const val modal: Float = 400f
        const val popover: Float = 500f
        const val tooltip: Float = 600f
        const val toast: Float = 700f
    }

    // ============================================================
    // ANIMATION DURATIONS (in milliseconds)
    // ============================================================
    object Animation {
        const val instant: Int = 0
        const val fastest: Int = 100
        const val fast: Int = 150
        const val normal: Int = 250
        const val slow: Int = 350
        const val slower: Int = 500
        const val slowest: Int = 700
    }

    // ============================================================
    // BREAKPOINTS (for responsive design)
    // ============================================================
    object Breakpoint {
        val compact: Dp = 600.dp      // Phone portrait
        val medium: Dp = 840.dp       // Tablet portrait / Phone landscape
        val expanded: Dp = 1200.dp    // Desktop / Tablet landscape
        val large: Dp = 1600.dp       // Large desktop
    }
}

// ============================================================
// CONVENIENCE TYPE ALIASES
// ============================================================
typealias Spacing = AppDimensions.Spacing
typealias Padding = AppDimensions.Padding
typealias Size = AppDimensions.Size
typealias Corner = AppDimensions.Corner
typealias TextSize = AppDimensions.TextSize
