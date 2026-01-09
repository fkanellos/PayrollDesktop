package com.payroll.app.desktop.ui.screens

import com.payroll.app.desktop.core.resources.toDisplayString
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.presentation.settings.SettingsContract.SettingsAction
import com.payroll.app.desktop.presentation.settings.SettingsContract.SettingsEffect
import com.payroll.app.desktop.presentation.settings.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

/**
 * Settings Screen
 * Provides manual sync buttons for emergency situations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.toDisplayString(),
                        duration = SnackbarDuration.Short
                    )
                }
                is SettingsEffect.SyncComplete -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.toDisplayString(),
                        duration = SnackbarDuration.Long
                    )
                }
                is SettingsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.toDisplayString(),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.Settings.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, Strings.Common.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = Strings.Settings.dataSync,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Strings.Settings.dataSyncDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // Sync from Google Sheets
            SyncCard(
                title = Strings.Settings.syncFromSheets,
                description = Strings.Settings.syncFromSheetsDesc,
                icon = Icons.Default.CloudDownload,
                buttonText = Strings.Settings.syncFromSheetsButton,
                isLoading = state.isSyncingFromSheets,
                onClick = { viewModel.handleAction(SettingsAction.SyncFromGoogleSheets) }
            )

            // Push to Google Sheets
            SyncCard(
                title = Strings.Settings.pushToSheets,
                description = Strings.Settings.pushToSheetsDesc,
                icon = Icons.Default.CloudUpload,
                buttonText = Strings.Settings.pushToSheetsButton,
                isLoading = state.isPushingToSheets,
                onClick = { viewModel.handleAction(SettingsAction.PushToGoogleSheets) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Result/Error display
            state.lastSyncResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            state.error?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isLoading) Strings.Common.loading else buttonText)
            }
        }
    }
}
