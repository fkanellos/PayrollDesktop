package com.payroll.app.desktop.core.config

/**
 * Application Configuration
 * Contains all configurable settings for the application
 */
data class AppConfig(
    // Google Sheets Configuration
    val googleSheetsSpreadsheetId: String,

    // Google OAuth Configuration
    val oauthRedirectUri: String = "http://localhost",

    // API Configuration
    val apiBaseUrl: String = "http://localhost:8080",

    // Application Settings
    val appName: String = "Payroll Desktop",
    val appVersion: String = "1.0.0"
) {
    companion object {
        /**
         * Default configuration with placeholder values
         */
        fun default() = AppConfig(
            googleSheetsSpreadsheetId = "",
            oauthRedirectUri = "http://localhost",
            apiBaseUrl = "http://localhost:8080",
            appName = "Payroll Desktop",
            appVersion = "1.0.0"
        )
    }
}
