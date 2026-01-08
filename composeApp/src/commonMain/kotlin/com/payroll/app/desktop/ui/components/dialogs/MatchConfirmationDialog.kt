package com.payroll.app.desktop.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.payroll.app.desktop.domain.models.ClientMatchResult
import com.payroll.app.desktop.domain.models.MatchConfidence
import com.payroll.app.desktop.domain.models.UncertainMatch

/**
 * Paginated dialog για επιβεβαίωση multiple uncertain client matches
 * Δείχνει ένα match κάθε φορά με navigation "1/5", "2/5" κλπ
 */
@Composable
fun MatchConfirmationPaginatedDialog(
    uncertainMatches: List<UncertainMatch>,
    onConfirm: (UncertainMatch) -> Unit,
    onReject: (UncertainMatch) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }

    if (uncertainMatches.isEmpty()) return

    val currentMatch = uncertainMatches[currentIndex]
    val suggestedMatch = currentMatch.suggestedMatch ?: return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                if (uncertainMatches.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button
                        IconButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                contentDescription = "Προηγούμενο"
                            )
                        }

                        // Page indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${currentIndex + 1}/${uncertainMatches.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Next button
                        IconButton(
                            onClick = { if (currentIndex < uncertainMatches.size - 1) currentIndex++ },
                            enabled = currentIndex < uncertainMatches.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "Επόμενο"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Header Icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Επιβεβαίωση",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Επιβεβαίωση Ταιριάσματος",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confidence badge
                ConfidenceBadge(suggestedMatch.confidence)

                Spacer(modifier = Modifier.height(24.dp))

                // Question
                Text(
                    text = "Είναι το ίδιο άτομο;",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event title from calendar
                MatchComparisonCard(
                    label = "Από Calendar",
                    text = currentMatch.eventTitle,
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggested client from database
                MatchComparisonCard(
                    label = "Από Βάση",
                    text = suggestedMatch.clientName,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Match reason
                Text(
                    text = "Λόγος: ${suggestedMatch.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject button
                    OutlinedButton(
                        onClick = {
                            onReject(currentMatch)
                            // Auto-advance or close
                            if (currentIndex < uncertainMatches.size - 1) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Όχι")
                    }

                    // Confirm button
                    Button(
                        onClick = {
                            onConfirm(currentMatch)
                            // Auto-advance or close
                            if (currentIndex < uncertainMatches.size - 1) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ναι")
                    }
                }
            }
        }
    }
}

/**
 * Simple single-match dialog (legacy, για backwards compatibility)
 */
@Composable
fun MatchConfirmationDialog(
    eventTitle: String,
    suggestedMatch: ClientMatchResult,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Επιβεβαίωση",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Επιβεβαίωση Ταιριάσματος",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confidence badge
                ConfidenceBadge(suggestedMatch.confidence)

                Spacer(modifier = Modifier.height(24.dp))

                // Question
                Text(
                    text = "Είναι το ίδιο άτομο;",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event title from calendar
                MatchComparisonCard(
                    label = "Από Calendar",
                    text = eventTitle,
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggested client from database
                MatchComparisonCard(
                    label = "Από Βάση",
                    text = suggestedMatch.clientName,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Match reason
                Text(
                    text = "Λόγος: ${suggestedMatch.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject button
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Όχι")
                    }

                    // Confirm button
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ναι")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: MatchConfidence) {
    val (text, color) = when (confidence) {
        MatchConfidence.EXACT -> "Ακριβές" to MaterialTheme.colorScheme.primary
        MatchConfidence.HIGH -> "Υψηλή Βεβαιότητα" to MaterialTheme.colorScheme.tertiary
        MatchConfidence.MEDIUM -> "Μέτρια Βεβαιότητα" to MaterialTheme.colorScheme.secondary
        MatchConfidence.LOW -> "Χαμηλή Βεβαιότητα" to MaterialTheme.colorScheme.error
        MatchConfidence.NONE -> "Καμία" to MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MatchComparisonCard(
    label: String,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
