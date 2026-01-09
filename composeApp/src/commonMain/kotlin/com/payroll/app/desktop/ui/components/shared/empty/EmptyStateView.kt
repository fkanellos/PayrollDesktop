package com.payroll.app.desktop.ui.components.shared.empty

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Generic empty state component
 * Displays icon, title, subtitle, and optional action button
 *
 * Replaces:
 * - EmptyClientsView
 * - EmptyEmployeesView
 * - EmptyClientBreakdownCard
 * - Any other empty state displays
 *
 * Usage:
 * ```kotlin
 * EmptyStateView(
 *     icon = Icons.Default.PersonOff,
 *     title = "No clients found",
 *     subtitle = "Add your first client",
 *     actionLabel = "Add Client",
 *     onActionClick = { viewModel.handleAction(AddClient) }
 * )
 * ```
 */
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PayrollColors.TextSecondary.copy(alpha = 0.5f)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PayrollColors.OnSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = PayrollColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}
