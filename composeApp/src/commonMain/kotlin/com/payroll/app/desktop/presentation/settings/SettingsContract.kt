package com.payroll.app.desktop.presentation.settings

import com.payroll.app.desktop.core.resources.StringMessage

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
        data class ShowToast(val message: StringMessage) : SettingsEffect()
        data class SyncComplete(val message: StringMessage) : SettingsEffect()
        data class ShowError(val message: StringMessage) : SettingsEffect()
    }
}
