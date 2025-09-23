package com.payroll.app.desktop.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class PayrollButtonType {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    WARNING,
    ERROR,
    OUTLINE
}

@Composable
fun PayrollButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: PayrollButtonType = PayrollButtonType.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = when (type) {
        PayrollButtonType.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = PayrollColors.Primary,
            contentColor = PayrollColors.OnPrimary
        )
        PayrollButtonType.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = PayrollColors.TextSecondary,
            contentColor = Color.White
        )
        PayrollButtonType.SUCCESS -> ButtonDefaults.buttonColors(
            containerColor = PayrollColors.Success,
            contentColor = Color.White
        )
        PayrollButtonType.WARNING -> ButtonDefaults.buttonColors(
            containerColor = PayrollColors.Warning,
            contentColor = Color.White
        )
        PayrollButtonType.ERROR -> ButtonDefaults.buttonColors(
            containerColor = PayrollColors.Error,
            contentColor = PayrollColors.OnError
        )
        PayrollButtonType.OUTLINE -> ButtonDefaults.outlinedButtonColors(
            contentColor = PayrollColors.Primary
        )
    }

    if (type == PayrollButtonType.OUTLINE) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled && !isLoading,
            colors = colors,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = PayrollColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled && !isLoading,
            colors = colors,
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Preview Composables
@Preview
@Composable
private fun PayrollButtonPrimaryPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PayrollButton(
                    text = "Υπολογισμός Μισθοδοσίας",
                    onClick = { },
                    type = PayrollButtonType.PRIMARY
                )
            }
        }
    }
}

@Preview
@Composable
private fun PayrollButtonTypesPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PayrollButton(
                    text = "Primary Button",
                    onClick = { },
                    type = PayrollButtonType.PRIMARY
                )

                PayrollButton(
                    text = "Secondary Button",
                    onClick = { },
                    type = PayrollButtonType.SECONDARY
                )

                PayrollButton(
                    text = "Success Button",
                    onClick = { },
                    type = PayrollButtonType.SUCCESS
                )

                PayrollButton(
                    text = "Warning Button",
                    onClick = { },
                    type = PayrollButtonType.WARNING
                )

                PayrollButton(
                    text = "Error Button",
                    onClick = { },
                    type = PayrollButtonType.ERROR
                )

                PayrollButton(
                    text = "Outline Button",
                    onClick = { },
                    type = PayrollButtonType.OUTLINE
                )
            }
        }
    }
}

@Preview
@Composable
private fun PayrollButtonLoadingPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PayrollButton(
                    text = "Φόρτωση...",
                    onClick = { },
                    type = PayrollButtonType.PRIMARY,
                    isLoading = true
                )

                PayrollButton(
                    text = "Απενεργοποιημένο",
                    onClick = { },
                    type = PayrollButtonType.PRIMARY,
                    enabled = false
                )
            }
        }
    }
}