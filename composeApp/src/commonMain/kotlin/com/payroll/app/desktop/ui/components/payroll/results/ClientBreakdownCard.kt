package com.payroll.app.desktop.ui.components.payroll.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.format
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.utils.toEuroString

/**
 * Client Breakdown Card Component
 * Expandable card showing client payroll details with sessions and financial breakdown
 *
 * Usage:
 * ```kotlin
 * result.clientBreakdown.forEach { client ->
 *     ClientBreakdownCard(client)
 * }
 * ```
 */
@Composable
fun ClientBreakdownCard(
    client: com.payroll.app.desktop.domain.models.ClientPayrollDetail,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Calculate if there are any pending-related notes to show
    val hasPending = client.pendingSessions > 0
    val hasPaidPending = client.paidPendingCount > 0
    val hasUnresolved = client.unresolvedPendingCount > 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) PayrollColors.Primary.copy(alpha = 0.05f) else PayrollColors.CardBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                hasUnresolved -> PayrollColors.Warning.copy(alpha = 0.5f)
                hasPending -> Color(0xFF64748B).copy(alpha = 0.3f)
                expanded -> PayrollColors.Primary.copy(alpha = 0.3f)
                else -> PayrollColors.DividerColor
            }
        )
    ) {
        Column {
            // Main client info - clickable for expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = PayrollColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = client.clientName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Session breakdown with pending info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.Payroll.sessionsLabel.format(client.sessions),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PayrollColors.OnSurface
                        )
                        Text(text = "|", fontSize = 13.sp, color = PayrollColors.DividerColor)
                        Text(
                            text = Strings.Payroll.paidLabel.format(client.totalRevenue.toEuroString()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PayrollColors.Success
                        )
                    }

                    // Session type breakdown
                    if (client.completedSessions > 0 || client.pendingSessions > 0 || client.paidPendingCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (client.completedSessions > 0) {
                                SessionBadge(
                                    label = Strings.Payroll.completedSessionsLabel.format(client.completedSessions),
                                    color = PayrollColors.Success
                                )
                            }
                            if (client.pendingSessions > 0) {
                                SessionBadge(
                                    label = Strings.Payroll.pendingSessionsLabel.format(client.pendingSessions),
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (client.paidPendingCount > 0) {
                                SessionBadge(
                                    label = Strings.Payroll.paidPreviouslyLabel.format(client.paidPendingCount),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = client.employeeEarnings.toEuroString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PayrollColors.Success
                    )

                    Text(
                        text = "Μερίδιο εργαζομένου",
                        fontSize = 11.sp,
                        color = PayrollColors.TextSecondary
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Σύμπτυξη" else "Επέκταση",
                    tint = PayrollColors.Primary
                )
            }

            // Pending payment notes (always visible if applicable)
            if (hasPending || hasPaidPending || hasUnresolved) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasPaidPending) {
                        PendingNote(
                            icon = "✅",
                            text = Strings.Payroll.includesPendingPayments.format(
                                (client.paidPendingCount * client.pricePerSession).toEuroString(),
                                client.paidPendingCount
                            ),
                            color = PayrollColors.Success
                        )
                    }
                    if (hasPending) {
                        PendingNote(
                            icon = "⏳",
                            text = "${client.pendingSessions} pending (${(client.pendingSessions * client.pricePerSession).toEuroString()}) - will charge next time",
                            color = Color(0xFF64748B)
                        )
                    }
                    if (hasUnresolved) {
                        PendingNote(
                            icon = "⚠️",
                            text = Strings.Payroll.clientOwes.format(client.unresolvedPendingCount),
                            color = PayrollColors.Warning
                        )
                    }
                }
            }

            // Expanded details
            if (expanded) {
                HorizontalDivider(color = PayrollColors.DividerColor)

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Financial breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FinancialItem(
                            label = "Τιμή/Συνεδρία",
                            value = client.pricePerSession.toEuroString(),
                            color = PayrollColors.OnSurface
                        )
                        FinancialItem(
                            label = "Εργαζόμενος",
                            value = client.employeePricePerSession.toEuroString(),
                            color = PayrollColors.Success
                        )
                        FinancialItem(
                            label = "Εταιρία",
                            value = client.companyPricePerSession.toEuroString(),
                            color = PayrollColors.Warning
                        )
                    }

                    // Session details if available
                    if (client.eventDetails.isNotEmpty()) {
                        Text(
                            text = "📅 Events (${client.eventDetails.size})",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PayrollColors.OnSurface
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            client.eventDetails.take(5).forEach { event ->
                                SessionItem(event, client.pricePerSession)
                            }

                            if (client.eventDetails.size > 5) {
                                Text(
                                    text = "... και ${client.eventDetails.size - 5} ακόμη συνεδρίες",
                                    fontSize = 12.sp,
                                    color = PayrollColors.TextSecondary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionBadge(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PendingNote(
    icon: String,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FinancialItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = PayrollColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SessionItem(
    event: com.payroll.app.desktop.domain.models.EventDetail,
    pricePerSession: Double = 0.0
) {
    // Determine status icon and color based on event status and pending flags
    val (statusIcon, statusColor, statusText) = when {
        event.paidForPending -> Triple("✅💰", PayrollColors.Success, "Paid for pending ${event.pendingDate ?: ""}")
        event.isPending || event.status == "pending_payment" -> Triple("⏳", Color(0xFF64748B), Strings.Payroll.pendingPayment)
        event.status == "completed" -> Triple("✅", PayrollColors.Success, Strings.Payroll.statusCompleted)
        event.status == "cancelled" -> Triple("❌", PayrollColors.Error, Strings.Payroll.statusCancelled)
        else -> Triple("📅", PayrollColors.TextSecondary, "")
    }

    // Calculate revenue display - pending payments show €0
    val revenueText = if (event.isPending || event.status == "pending_payment") {
        "€0"
    } else {
        pricePerSession.toEuroString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = statusIcon, fontSize = 12.sp)
            Column {
                Text(
                    text = "${event.date} ${event.time}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (event.paidForPending && event.pendingDate != null) {
                    Text(
                        text = "(Paid for pending ${event.pendingDate})",
                        fontSize = 10.sp,
                        color = PayrollColors.Success,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.duration,
                fontSize = 11.sp,
                color = PayrollColors.TextSecondary
            )
            Text(
                text = revenueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (event.isPending || event.status == "pending_payment") Color(0xFF64748B) else PayrollColors.Success
            )
        }
    }
}
