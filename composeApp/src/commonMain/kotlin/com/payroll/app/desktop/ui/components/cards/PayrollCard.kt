package com.payroll.app.desktop.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PayrollCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    backgroundColor: Color = PayrollColors.CardBackground,
    borderColor: Color = PayrollColors.DividerColor,
    elevation: Int = 2,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.then(Modifier.clickable { onClick() })
    } else modifier

    Card(
        modifier = cardModifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header section
            if (title != null || subtitle != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PayrollColors.OnSurface
                            )
                        )
                    }

                    subtitle?.let {
                        Text(
                            text = it,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = PayrollColors.TextSecondary
                            )
                        )
                    }
                }

                if (title != null || subtitle != null) {
                    HorizontalDivider(color = PayrollColors.DividerColor)
                }
            }

            // Content
            content()
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    valueColor: Color = PayrollColors.Primary,
    backgroundColor: Color = Color(0xFFF7FAFC)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, PayrollColors.DividerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PayrollColors.TextSecondary
                )
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayrollColors.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun AdminCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x1AFC7D00),
    onAddClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$title ($count)",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )

                TextButton(
                    onClick = onAddClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("+ Προσθήκη")
                }
            }

            content()
        }
    }
}

// Preview Composables
@Preview
@Composable
private fun PayrollCardPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PayrollCard(
                    title = "Υπολογισμός Μισθοδοσίας",
                    subtitle = "Επιλέξτε εργαζόμενο και περίοδος"
                ) {
                    Text("Περιεχόμενο κάρτας...")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Περισσότερο περιεχόμενο...")
                }

                PayrollCard {
                    Text("Απλή κάρτα χωρίς τίτλο")
                }
            }
        }
    }
}

@Preview
@Composable
private fun SummaryCardPreview() {
    PayrollTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Συνολικές Συνεδρίες",
                    value = "42",
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Συνολικά Έσοδα",
                    value = "€2,080",
                    subtitle = "Αυτός ο μήνας",
                    valueColor = PayrollColors.Success,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Μισθός Εργαζομένου",
                    value = "€832",
                    valueColor = PayrollColors.Info,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdminCardPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdminCard(
                    title = "Εργαζόμενοι",
                    count = 3,
                    backgroundColor = Color(0x1AFC7D00)
                ) {
                    Text(
                        "Λίστα εργαζομένων...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                AdminCard(
                    title = "Πελάτες",
                    count = 108,
                    backgroundColor = Color(0x1A667EEA)
                ) {
                    Text(
                        "Λίστα πελατών...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}