package com.payroll.app.desktop.ui.components.shared.headers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Generic screen header component
 * Displays title, subtitle, and optional action buttons
 *
 * Usage:
 * ```kotlin
 * ScreenHeader(
 *     title = "Employees",
 *     subtitle = "Manage your employees",
 *     primaryAction = HeaderAction(
 *         icon = Icons.Default.Add,
 *         label = "Add Employee",
 *         onClick = { viewModel.handleAction(AddEmployee) }
 *     ),
 *     secondaryAction = HeaderAction(
 *         icon = Icons.Default.Refresh,
 *         label = "Refresh",
 *         onClick = { viewModel.handleAction(Refresh) }
 *     )
 * )
 * ```
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    primaryAction: HeaderAction? = null,
    secondaryAction: HeaderAction? = null,
    badge: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title section
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayrollColors.OnSurface
                )

                if (badge != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = PayrollColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = PayrollColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = PayrollColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            secondaryAction?.let { action ->
                OutlinedButton(onClick = action.onClick) {
                    Icon(action.icon, contentDescription = action.label)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(action.label)
                }
            }

            primaryAction?.let { action ->
                Button(onClick = action.onClick) {
                    Icon(action.icon, contentDescription = action.label)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(action.label)
                }
            }
        }
    }
}

/**
 * Data class for header actions
 */
data class HeaderAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
