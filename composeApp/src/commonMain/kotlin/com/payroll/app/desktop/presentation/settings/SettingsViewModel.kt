package com.payroll.app.desktop.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payroll.app.desktop.core.resources.StringMessage
import com.payroll.app.desktop.core.strings.Strings
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
                lastSyncResult = Strings.Settings.syncStarting
            )

            val result = databaseSyncService.syncFromSheets()

            result.fold(
                onSuccess = { response ->
                    val durationSec = String.format("%.1f", response.durationMs / 1000.0)
                    val message = Strings.Success.syncComplete(
                        response.employeesInserted,
                        response.employeesUpdated,
                        response.clientsInserted,
                        response.clientsUpdated,
                        durationSec
                    )

                    _state.value = _state.value.copy(
                        isSyncingFromSheets = false,
                        lastSyncResult = message
                    )

                    _effect.emit(SettingsEffect.SyncComplete(StringMessage.CustomError(message)))
                },
                onFailure = { error ->
                    val errorMsg = Strings.Errors.syncFailed(error.message ?: "")
                    _state.value = _state.value.copy(
                        isSyncingFromSheets = false,
                        error = errorMsg
                    )

                    _effect.emit(SettingsEffect.ShowError(StringMessage.CustomError(errorMsg)))
                }
            )
        }
    }

    private fun pushToSheets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isPushingToSheets = true,
                error = null,
                lastSyncResult = Strings.Settings.pushStarting
            )

            val result = databaseSyncService.pushToSheets()

            result.fold(
                onSuccess = { response ->
                    val durationSec = String.format("%.1f", response.durationMs / 1000.0)
                    val message = if (response.employeesFailed > 0 || response.clientsFailed > 0) {
                        Strings.Success.pushCompleteWithErrors(
                            response.employeesPushed,
                            response.employeesFailed,
                            response.clientsPushed,
                            response.clientsFailed,
                            durationSec
                        )
                    } else {
                        Strings.Success.pushComplete(
                            response.employeesPushed,
                            response.clientsPushed,
                            durationSec
                        )
                    }

                    _state.value = _state.value.copy(
                        isPushingToSheets = false,
                        lastSyncResult = message
                    )

                    _effect.emit(SettingsEffect.SyncComplete(StringMessage.CustomError(message)))
                },
                onFailure = { error ->
                    val errorMsg = Strings.Errors.pushFailed(error.message ?: "")
                    _state.value = _state.value.copy(
                        isPushingToSheets = false,
                        error = errorMsg
                    )

                    _effect.emit(SettingsEffect.ShowError(StringMessage.CustomError(errorMsg)))
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
