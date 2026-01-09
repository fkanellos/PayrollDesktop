package com.payroll.app.desktop.ui.components.payroll.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.format

/**
 * Unmatched Events Card Component
 * Displays unmatched calendar events with quick-add functionality
 *
 * Usage:
 * ```kotlin
 * UnmatchedEventsCard(
 *     unmatchedEvents = result.eventTracking?.unmatchedEvents.orEmpty(),
 *     addedClients = uiState.addedClients,
 *     onManageClients = { /* Navigate to client management */ },
 *     onAddClient = { original, edited, price, empPrice, compPrice ->
 *         viewModel.handleAction(AddUnmatchedClient(original, edited, price, empPrice, compPrice))
 *     }
 * )
 * ```
 */
@Composable
fun UnmatchedEventsCard(
    unmatchedEvents: List<com.payroll.app.desktop.domain.models.UnmatchedEvent>,
    modifier: Modifier = Modifier,
    addedClients: Set<String> = emptySet(),
    onManageClients: () -> Unit = {},
    onAddClient: (originalTitle: String, editedName: String, price: Double, employeePrice: Double, companyPrice: Double) -> Unit = { _, _, _, _, _ -> }
) {
    if (unmatchedEvents.isEmpty()) return

    // Get unique unmatched names (excluding already added ones)
    val uniqueNames = remember(unmatchedEvents, addedClients) {
        unmatchedEvents.map { it.title }.distinct().sorted().filterNot { it in addedClients }
    }

    // State for editing
    var editingName by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("50.0") }
    var editEmployeePrice by remember { mutableStateOf("22.5") }
    var editCompanyPrice by remember { mutableStateOf("27.5") }

    if (uniqueNames.isEmpty() && addedClients.isNotEmpty()) {
        // All clients have been added
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9) // Light green
            ),
            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = Strings.Payroll.allUnmatchedAdded,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
        return
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0) // Light orange background
        ),
        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = Strings.Payroll.unmatchedEventsLabel(unmatchedEvents.size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = Strings.Payroll.clickToAddClient,
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                // Manage Clients button
                OutlinedButton(
                    onClick = onManageClients,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Text(Strings.ClientManagement.title)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFFF9800).copy(alpha = 0.3f))

            // Default prices info
            Surface(
                color = Color(0xFFFFE0B2),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Strings.Payroll.defaultPrices,
                        fontSize = 12.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = Strings.Payroll.defaultPriceTotal, fontSize = 12.sp, color = Color(0xFFE65100))
                    Text(text = Strings.Payroll.defaultPriceEmployee, fontSize = 12.sp, color = Color(0xFFE65100))
                    Text(text = Strings.Payroll.defaultPriceCompany, fontSize = 12.sp, color = Color(0xFFE65100))
                }
            }

            // List of unique unmatched names
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uniqueNames.forEach { name ->
                    val isEditing = editingName == name
                    val count = unmatchedEvents.count { it.title == name }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isEditing) Color(0xFFFFCC80) else Color(0xFFFFE0B2),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Name row with add button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Medium
                                )
                                if (count > 1) {
                                    Surface(
                                        color = Color(0xFFFF9800).copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "×$count sessions",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Edit button
                                IconButton(
                                    onClick = {
                                        if (isEditing) {
                                            editingName = null
                                        } else {
                                            editingName = name
                                            editName = name  // Initialize with original name
                                            editPrice = "50.0"
                                            editEmployeePrice = "22.5"
                                            editCompanyPrice = "27.5"
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (isEditing) "Cancel" else "Edit name and prices",
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Quick add button (with default values)
                                if (!isEditing) {
                                    IconButton(
                                        onClick = {
                                            onAddClient(
                                                name,
                                                name,
                                                AppConstants.Pricing.DEFAULT_SESSION_PRICE,
                                                AppConstants.Pricing.DEFAULT_EMPLOYEE_SHARE,
                                                AppConstants.Pricing.DEFAULT_COMPANY_SHARE
                                            )
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = Color(0xFF4CAF50),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add client",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Editable prices row (when editing)
                        if (isEditing) {
                            HorizontalDivider(color = Color(0xFFFF9800).copy(alpha = 0.3f))

                            // Name field
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text(Strings.ClientManagement.clientName, fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Price field
                                OutlinedTextField(
                                    value = editPrice,
                                    onValueChange = { editPrice = it },
                                    label = { Text(Strings.ClientManagement.clientPrice, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )

                                // Employee price field
                                OutlinedTextField(
                                    value = editEmployeePrice,
                                    onValueChange = { editEmployeePrice = it },
                                    label = { Text(Strings.ClientManagement.employeePrice, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )

                                // Company price field
                                OutlinedTextField(
                                    value = editCompanyPrice,
                                    onValueChange = { editCompanyPrice = it },
                                    label = { Text(Strings.ClientManagement.companyPrice, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )

                                // Save button
                                Button(
                                    onClick = {
                                        val price = editPrice.toDoubleOrNull() ?: 50.0
                                        val empPrice = editEmployeePrice.toDoubleOrNull() ?: 22.5
                                        val compPrice = editCompanyPrice.toDoubleOrNull() ?: 27.5
                                        onAddClient(name, editName.trim(), price, empPrice, compPrice)
                                        editingName = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.Common.save)
                                }
                            }
                        }
                    }
                }
            }

            // Added clients info
            if (addedClients.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFFF9800).copy(alpha = 0.3f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${addedClients.size} client(s) added. Re-calculate to update results.",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}
