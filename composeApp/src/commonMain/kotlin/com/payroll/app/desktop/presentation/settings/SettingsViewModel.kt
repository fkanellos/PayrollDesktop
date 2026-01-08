package com.payroll.app.desktop.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payroll.app.desktop.domain.service.DatabaseSyncService
import com.payroll.app.desktop.presentation.settings.SettingsContract.SettingsAction
import com.payroll.app.desktop.presentation.settings.SettingsContract.SettingsEffect
import com.payroll.app.desktop.presentation.settings.SettingsContract.SettingsState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * Handles syncing data between local DB and Google Sheets
 */
class SettingsViewModel(
    private val databaseSyncService: DatabaseSyncService
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    fun handleAction(action: SettingsAction) {
        when (action) {
            SettingsAction.SyncFromGoogleSheets -> syncFromSheets()
            SettingsAction.PushToGoogleSheets -> pushToSheets()
            SettingsAction.ClearMessages -> clearMessages()
        }
    }

    private fun syncFromSheets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSyncingFromSheets = true,
                error = null,
                lastSyncResult = "Ξεκινά συγχρονισμός από Google Sheets..."
            )

            val result = databaseSyncService.syncFromSheets()

            result.fold(
                onSuccess = { response ->
                    val durationSec = response.durationMs / 1000.0
                    val message = buildString {
                        append("📥 Συγχρονισμός ολοκληρώθηκε!\n\n")
                        append("Εργαζόμενοι: +${response.employeesInserted} / ↻${response.employeesUpdated}\n")
                        append("Πελάτες: +${response.clientsInserted} / ↻${response.clientsUpdated}\n\n")
                        append("⏱️ Διάρκεια: ${String.format("%.1f", durationSec)} δευτερόλεπτα")
                    }

                    _state.value = _state.value.copy(
                        isSyncingFromSheets = false,
                        lastSyncResult = message
                    )

                    _effect.emit(SettingsEffect.SyncComplete(message))
                },
                onFailure = { error ->
                    val errorMsg = "Αποτυχία συγχρονισμού: ${error.message}"
                    _state.value = _state.value.copy(
                        isSyncingFromSheets = false,
                        error = errorMsg
                    )

                    _effect.emit(SettingsEffect.ShowError(errorMsg))
                }
            )
        }
    }

    private fun pushToSheets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isPushingToSheets = true,
                error = null,
                lastSyncResult = "Ξεκινά push στο Google Sheets..."
            )

            val result = databaseSyncService.pushToSheets()

            result.fold(
                onSuccess = { response ->
                    val durationSec = response.durationMs / 1000.0
                    val message = buildString {
                        append("🚀 Push ολοκληρώθηκε!\n\n")
                        append("Εργαζόμενοι: ✓${response.employeesPushed}")
                        if (response.employeesFailed > 0) append(" / ✗${response.employeesFailed}")
                        append("\n")
                        append("Πελάτες: ✓${response.clientsPushed}")
                        if (response.clientsFailed > 0) append(" / ✗${response.clientsFailed}")
                        append("\n\n")
                        append("⏱️ Διάρκεια: ${String.format("%.1f", durationSec)} δευτερόλεπτα")
                    }

                    _state.value = _state.value.copy(
                        isPushingToSheets = false,
                        lastSyncResult = message
                    )

                    _effect.emit(SettingsEffect.SyncComplete(message))
                },
                onFailure = { error ->
                    val errorMsg = "Αποτυχία push: ${error.message}"
                    _state.value = _state.value.copy(
                        isPushingToSheets = false,
                        error = errorMsg
                    )

                    _effect.emit(SettingsEffect.ShowError(errorMsg))
                }
            )
        }
    }

    private fun clearMessages() {
        _state.value = _state.value.copy(
            error = null,
            lastSyncResult = null
        )
    }
}
