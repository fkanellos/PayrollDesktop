package com.payroll.app.desktop.presentation.settings

/**
 * Settings Screen MVI Contract
 */
object SettingsContract {
    data class SettingsState(
        val isSyncingFromSheets: Boolean = false,
        val isPushingToSheets: Boolean = false,
        val lastSyncResult: String? = null,
        val error: String? = null
    )

    sealed class SettingsAction {
        object SyncFromGoogleSheets : SettingsAction()
        object PushToGoogleSheets : SettingsAction()
        object ClearMessages : SettingsAction()
    }

    sealed class SettingsEffect {
        data class ShowToast(val message: String) : SettingsEffect()
        data class SyncComplete(val message: String) : SettingsEffect()
        data class ShowError(val error: String) : SettingsEffect()
    }
}
