package com.payroll.app.desktop.ui.components.shared.loading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Reusable loading indicator component
 * Displays a centered loading spinner with optional message
 *
 * Usage:
 * ```kotlin
 * LoadingIndicator()
 * LoadingIndicator(message = "Loading data...")
 * ```
 */
@Composable
fun LoadingIndicator(
    message: String = Strings.Common.loading,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = PayrollColors.Primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = PayrollColors.TextSecondary
            )
        }
    }
}
