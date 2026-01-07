package com.payroll.app.desktop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    ERROR,
    WARNING,
    INFO
}

/**
 * Reusable error dialog component
 *
 * Usage:
 * ```
 * ErrorDialog(
 *     title = "Σφάλμα Αποθήκευσης",
 *     message = "Δεν ήταν δυνατή η αποθήκευση του πελάτη. Ελέγξτε τη σύνδεσή σας.",
 *     severity = ErrorSeverity.ERROR,
 *     onDismiss = { showError = false },
 *     onRetry = { retryOperation() }
 * )
 * ```
 */
@Composable
fun ErrorDialog(
    title: String,
    message: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    dismissButtonText: String = "OK",
    retryButtonText: String = "Επανάληψη"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (severity) {
                    ErrorSeverity.ERROR -> Icons.Default.Error
                    ErrorSeverity.WARNING -> Icons.Default.Warning
                    ErrorSeverity.INFO -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (severity) {
                    ErrorSeverity.ERROR -> Color(0xFFD32F2F)
                    ErrorSeverity.WARNING -> Color(0xFFF57C00)
                    ErrorSeverity.INFO -> Color(0xFF1976D2)
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            if (onRetry != null) {
                Button(
                    onClick = {
                        onDismiss()
                        onRetry()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(retryButtonText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    )
}

/**
 * Compact error banner for inline errors
 */
@Composable
fun ErrorBanner(
    message: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = when (severity) {
            ErrorSeverity.ERROR -> Color(0xFFFFEBEE)
            ErrorSeverity.WARNING -> Color(0xFFFFF3E0)
            ErrorSeverity.INFO -> Color(0xFFE3F2FD)
        },
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (severity) {
                    ErrorSeverity.ERROR -> Icons.Default.Error
                    ErrorSeverity.WARNING -> Icons.Default.Warning
                    ErrorSeverity.INFO -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (severity) {
                    ErrorSeverity.ERROR -> Color(0xFFD32F2F)
                    ErrorSeverity.WARNING -> Color(0xFFF57C00)
                    ErrorSeverity.INFO -> Color(0xFF1976D2)
                },
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                color = when (severity) {
                    ErrorSeverity.ERROR -> Color(0xFFC62828)
                    ErrorSeverity.WARNING -> Color(0xFFE65100)
                    ErrorSeverity.INFO -> Color(0xFF1565C0)
                }
            )

            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Helper to create user-friendly error messages from exceptions
 */
object ErrorMessageHelper {

    fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Πρόβλημα σύνδεσης. Ελέγξτε τη σύνδεσή σας στο διαδίκτυο."

            exception.message?.contains("database", ignoreCase = true) == true ->
                "Σφάλμα βάσης δεδομένων. Δοκιμάστε να κλείσετε και να ανοίξετε ξανά την εφαρμογή."

            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Η αίτηση έληξε. Δοκιμάστε ξανά."

            exception.message?.contains("authentication", ignoreCase = true) == true ||
            exception.message?.contains("401", ignoreCase = true) == true ->
                "Πρόβλημα ταυτοποίησης. Ίσως χρειάζεται να συνδεθείτε ξανά."

            exception.message?.contains("403", ignoreCase = true) == true ->
                "Δεν έχετε δικαίωμα πρόσβασης σε αυτή τη λειτουργία."

            exception.message?.contains("404", ignoreCase = true) == true ->
                "Δεν βρέθηκαν τα ζητούμενα δεδομένα."

            exception.message?.contains("duplicate", ignoreCase = true) == true ||
            exception.message?.contains("unique", ignoreCase = true) == true ->
                "Υπάρχει ήδη εγγραφή με αυτά τα στοιχεία."

            else -> exception.message ?: "Προέκυψε ένα απροσδόκητο σφάλμα."
        }
    }

    fun getDetailedMessage(exception: Throwable): String {
        val basicMessage = getErrorMessage(exception)
        val technicalDetails = exception.message?.let { "\n\nΤεχνικές λεπτομέρειες: $it" } ?: ""
        return basicMessage + technicalDetails
    }
}
