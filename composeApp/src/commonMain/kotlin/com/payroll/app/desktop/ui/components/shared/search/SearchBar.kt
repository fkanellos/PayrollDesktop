package com.payroll.app.desktop.ui.components.shared.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Reusable search bar component
 * Displays a search text field with clear button
 *
 * Usage:
 * ```kotlin
 * SearchBar(
 *     query = searchQuery,
 *     onQueryChange = { viewModel.handleAction(UpdateSearch(it)) },
 *     placeholder = "Search employees..."
 * )
 * ```
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = Strings.Common.search,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PayrollColors.TextSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        enabled = enabled,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PayrollColors.Primary,
            unfocusedBorderColor = PayrollColors.DividerColor
        )
    )
}
