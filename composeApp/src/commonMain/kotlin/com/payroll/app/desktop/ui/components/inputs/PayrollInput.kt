package com.payroll.app.desktop.ui.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isError) PayrollColors.Error else PayrollColors.OnSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Text Field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TextStyle(fontSize = 14.sp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) PayrollColors.Error else PayrollColors.Primary,
                unfocusedBorderColor = if (isError) PayrollColors.Error else PayrollColors.DividerColor,
                focusedLabelColor = if (isError) PayrollColors.Error else PayrollColors.Primary,
                errorBorderColor = PayrollColors.Error,
                errorLabelColor = PayrollColors.Error,
                errorSupportingTextColor = PayrollColors.Error
            )
        )
    }
}

@Composable
fun PayrollNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String? = null
) {
    PayrollTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow only numbers and decimal point
            val filteredValue = newValue.filter { it.isDigit() || it == '.' }
            // Ensure only one decimal point
            val decimalCount = filteredValue.count { it == '.' }
            if (decimalCount <= 1) {
                onValueChange(filteredValue)
            }
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = prefix?.let { { Text(it, color = PayrollColors.TextSecondary) } },
        trailingIcon = suffix?.let { { Text(it, color = PayrollColors.TextSecondary) } },
        supportingText = errorMessage?.let { { Text(it, color = PayrollColors.Error) } },
        isError = isError,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Preview
@Composable
private fun PayrollTextFieldPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var text1 by remember { mutableStateOf("") }
                PayrollTextField(
                    value = text1,
                    onValueChange = { text1 = it },
                    label = "Όνομα Εργαζομένου",
                    placeholder = "Εισάγετε όνομα..."
                )

                var text2 by remember { mutableStateOf("test@example.com") }
                PayrollTextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    label = "Email",
                    placeholder = "Εισάγετε email...",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                var text3 by remember { mutableStateOf("") }
                PayrollTextField(
                    value = text3,
                    onValueChange = { text3 = it },
                    label = "Σφάλμα Παράδειγμα",
                    placeholder = "Αυτό έχει σφάλμα...",
                    isError = true,
                    supportingText = { Text("Αυτό το πεδίο είναι υποχρεωτικό") }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PayrollNumberFieldPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var price by remember { mutableStateOf("50.00") }
                PayrollNumberField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Τιμή ανά Συνεδρία",
                    placeholder = "0.00",
                    prefix = "€"
                )

                var percentage by remember { mutableStateOf("") }
                PayrollNumberField(
                    value = percentage,
                    onValueChange = { percentage = it },
                    label = "Ποσοστό Εργαζομένου",
                    placeholder = "0",
                    suffix = "%",
                    isError = true,
                    errorMessage = "Το ποσοστό πρέπει να είναι μεταξύ 0-100"
                )
            }
        }
    }
}