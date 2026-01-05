package com.payroll.app.desktop.ui.components.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.ui.theme.*

/**
 * Reusable composables for common UI patterns
 */

// ============================================================
// LOADING STATES
// ============================================================

/**
 * Full screen loading indicator
 */
@Composable
fun LoadingScreen(
    message: String = Strings.Common.LOADING,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Size.progressXl),
                color = PayrollColors.Primary
            )
            Text(
                text = message,
                style = PayrollTypography.bodyMedium,
                color = PayrollColors.TextSecondary
            )
        }
    }
}

/**
 * Inline loading indicator
 */
@Composable
fun LoadingIndicator(
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Size.progressMd),
            color = PayrollColors.Primary,
            strokeWidth = 2.dp
        )
        message?.let {
            Text(
                text = it,
                style = PayrollTypography.bodyMedium,
                color = PayrollColors.TextSecondary
            )
        }
    }
}

// ============================================================
// EMPTY STATES
// ============================================================

/**
 * Empty state with icon and message
 */
@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Inbox,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Padding.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Size.iconHuge),
            tint = PayrollColors.TextSecondary
        )
        Text(
            text = title,
            style = PayrollTypography.headlineSmall,
            color = PayrollColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Text(
                text = it,
                style = PayrollTypography.bodyMedium,
                color = PayrollColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        action?.let {
            Spacer(modifier = Modifier.height(Spacing.md))
            it()
        }
    }
}

// ============================================================
// ERROR STATES
// ============================================================

/**
 * Error message card
 */
@Composable
fun ErrorCard(
    message: String,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Error.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Padding.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = PayrollColors.Error,
                modifier = Modifier.size(Size.iconLg)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.Common.ERROR,
                    style = PayrollTypography.titleSmall,
                    color = PayrollColors.Error
                )
                Text(
                    text = message,
                    style = PayrollTypography.bodySmall,
                    color = PayrollColors.Error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                onRetry?.let {
                    TextButton(onClick = it) {
                        Text(Strings.Common.RETRY)
                    }
                }
                onDismiss?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = Strings.Common.CLOSE
                        )
                    }
                }
            }
        }
    }
}

/**
 * Inline error message
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = PayrollColors.Error,
            modifier = Modifier.size(Size.iconSm)
        )
        Text(
            text = message,
            style = PayrollTypography.bodySmall,
            color = PayrollColors.Error
        )
    }
}

// ============================================================
// SUCCESS STATES
// ============================================================

/**
 * Success message card
 */
@Composable
fun SuccessCard(
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Success.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Padding.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PayrollColors.Success,
                modifier = Modifier.size(Size.iconLg)
            )
            Text(
                text = message,
                style = PayrollTypography.bodyMedium,
                color = PayrollColors.Success,
                modifier = Modifier.weight(1f)
            )
            onDismiss?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Strings.Common.CLOSE
                    )
                }
            }
        }
    }
}

// ============================================================
// SECTION HEADERS
// ============================================================

/**
 * Section header with title and optional subtitle
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = PayrollTypography.headlineSmall,
                color = PayrollColors.OnSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = PayrollTypography.bodySmall,
                    color = PayrollColors.TextSecondary
                )
            }
        }
        action?.invoke()
    }
}

// ============================================================
// STATUS BADGES
// ============================================================

/**
 * Status badge with icon and text
 */
@Composable
fun StatusBadge(
    text: String,
    type: StatusType = StatusType.INFO,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = remember(type) {
        when (type) {
            StatusType.SUCCESS -> Triple(
                PayrollColors.Success.copy(alpha = 0.1f),
                PayrollColors.Success,
                Icons.Default.CheckCircle
            )
            StatusType.ERROR -> Triple(
                PayrollColors.Error.copy(alpha = 0.1f),
                PayrollColors.Error,
                Icons.Default.Error
            )
            StatusType.WARNING -> Triple(
                PayrollColors.Warning.copy(alpha = 0.1f),
                PayrollColors.Warning,
                Icons.Default.Warning
            )
            StatusType.INFO -> Triple(
                PayrollColors.Info.copy(alpha = 0.1f),
                PayrollColors.Info,
                Icons.Default.Info
            )
        }
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = Corner.chipShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Padding.md, vertical = Padding.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(Size.iconSm)
            )
            Text(
                text = text,
                style = PayrollTypography.chip,
                color = textColor
            )
        }
    }
}

enum class StatusType {
    SUCCESS, ERROR, WARNING, INFO
}

// ============================================================
// CONFIRMATION DIALOGS
// ============================================================

/**
 * Standard confirmation dialog
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = Strings.Common.YES,
    dismissText: String = Strings.Common.CANCEL,
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = if (isDestructive) {
            {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = PayrollColors.Error
                )
            }
        } else null,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) PayrollColors.Error else PayrollColors.Primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Size.progressSm),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(dismissText)
            }
        }
    )
}

// ============================================================
// LABELED VALUES
// ============================================================

/**
 * Label with value below
 */
@Composable
fun LabeledValue(
    label: String,
    value: String,
    valueColor: Color = PayrollColors.OnSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        Text(
            text = label,
            style = PayrollTypography.labelSmall,
            color = PayrollColors.TextSecondary
        )
        Text(
            text = value,
            style = PayrollTypography.bodyMedium,
            color = valueColor
        )
    }
}

/**
 * Label with value inline
 */
@Composable
fun LabeledValueInline(
    label: String,
    value: String,
    valueColor: Color = PayrollColors.OnSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = PayrollTypography.bodyMedium,
            color = PayrollColors.TextSecondary
        )
        Text(
            text = value,
            style = PayrollTypography.bodyMedium,
            color = valueColor
        )
    }
}
