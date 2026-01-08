package com.payroll.app.desktop.core.config

import com.payroll.app.desktop.core.logging.Logger
import java.io.File
import java.util.Properties

/**
 * JVM implementation of ConfigLoader
 * Reads configuration from app.config file in project root or user home
 */
actual class ConfigLoader {

    companion object {
        private const val TAG = "ConfigLoader"
        private const val CONFIG_FILENAME = "app.config"
        private const val APP_FOLDER = ".payroll-app"
    }

    actual fun loadConfig(): AppConfig {
        // Try to load from multiple locations in order:
        // 1. Current working directory (for development)
        // 2. User home .payroll-app folder (for production)

        val configLocations = listOf(
            File(System.getProperty("user.dir"), CONFIG_FILENAME),
            File(File(System.getProperty("user.home"), APP_FOLDER), CONFIG_FILENAME)
        )

        for (configFile in configLocations) {
            if (configFile.exists() && configFile.isFile) {
                try {
                    Logger.info(TAG, "Loading config from: ${configFile.absolutePath}")
                    return loadFromFile(configFile)
                } catch (e: Exception) {
                    Logger.error(TAG, "Error loading config from ${configFile.absolutePath}", e)
                }
            }
        }

        Logger.warning(TAG, "No config file found, using defaults")
        Logger.warning(TAG, "Create app.config in project root with required settings")
        return AppConfig.default()
    }

    private fun loadFromFile(file: File): AppConfig {
        val props = Properties()
        file.inputStream().use { props.load(it) }

        return AppConfig(
            googleSheetsSpreadsheetId = props.getProperty("google.sheets.spreadsheet_id", ""),
            oauthRedirectUri = props.getProperty("google.oauth.redirect_uri", "http://localhost"),
            apiBaseUrl = props.getProperty("api.base_url", "http://localhost:8080"),
            appName = props.getProperty("app.name", "Payroll Desktop"),
            appVersion = props.getProperty("app.version", "1.0.0")
        )
    }
}
