package com.payroll.app.desktop.ui.components.shared.badges

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Generic status badge component
 * Displays a small colored badge with text
 *
 * Usage:
 * ```kotlin
 * StatusBadge(
 *     text = "Completed",
 *     backgroundColor = PayrollColors.Success,
 *     textColor = Color.White
 * )
 * ```
 */
@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Pre-configured status badge variants
 */
object StatusBadgeDefaults {
    @Composable
    fun Success(text: String, modifier: Modifier = Modifier) =
        StatusBadge(
            text = text,
            backgroundColor = PayrollColors.Success,
            textColor = Color.White,
            modifier = modifier
        )

    @Composable
    fun Warning(text: String, modifier: Modifier = Modifier) =
        StatusBadge(
            text = text,
            backgroundColor = PayrollColors.Warning,
            textColor = Color.White,
            modifier = modifier
        )

    @Composable
    fun Error(text: String, modifier: Modifier = Modifier) =
        StatusBadge(
            text = text,
            backgroundColor = PayrollColors.Error,
            textColor = Color.White,
            modifier = modifier
        )

    @Composable
    fun Info(text: String, modifier: Modifier = Modifier) =
        StatusBadge(
            text = text,
            backgroundColor = PayrollColors.Info,
            textColor = Color.White,
            modifier = modifier
        )

    @Composable
    fun Pending(text: String, modifier: Modifier = Modifier) =
        StatusBadge(
            text = text,
            backgroundColor = PayrollColors.Warning.copy(alpha = 0.2f),
            textColor = PayrollColors.Warning,
            modifier = modifier
        )
}
