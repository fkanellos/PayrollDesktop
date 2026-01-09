package com.payroll.app.desktop.core.utils

import androidx.compose.ui.graphics.Color
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Parse hex color string to Color
 * Supports formats: #RRGGBB, #AARRGGBB, RRGGBB, AARRGGBB
 */
fun parseHexColor(hexColor: String): Color {
    return try {
        val cleanHex = hexColor.removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(
                red = cleanHex.substring(0, 2).toInt(16) / 255f,
                green = cleanHex.substring(2, 4).toInt(16) / 255f,
                blue = cleanHex.substring(4, 6).toInt(16) / 255f
            )
            8 -> Color(
                alpha = cleanHex.substring(0, 2).toInt(16) / 255f,
                red = cleanHex.substring(2, 4).toInt(16) / 255f,
                green = cleanHex.substring(4, 6).toInt(16) / 255f,
                blue = cleanHex.substring(6, 8).toInt(16) / 255f
            )
            else -> PayrollColors.Primary
        }
    } catch (e: Exception) {
        PayrollColors.Primary
    }
}
