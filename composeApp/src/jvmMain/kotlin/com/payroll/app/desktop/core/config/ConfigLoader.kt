package com.payroll.app.desktop.core.config

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.security.EncryptionManager
import java.io.File
import java.util.Properties

/**
 * JVM implementation of ConfigLoader
 * Reads configuration from app.config file in project root or user home
 *
 * 🔒 Security Features:
 * - Supports encrypted config values (AES-256)
 * - Auto-detects encrypted vs plaintext
 * - Encrypts sensitive values on first run
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

        // 🔒 Decrypt sensitive values if they are encrypted
        val spreadsheetId = getPropertyDecrypted(props, "google.sheets.spreadsheet_id", "")
        val oauthUri = getPropertyDecrypted(props, "google.oauth.redirect_uri", "http://localhost")
        val apiUrl = getPropertyDecrypted(props, "api.base_url", "http://localhost:8080")

        // 🔒 Auto-encrypt plaintext sensitive values on first load
        autoEncryptSensitiveValues(file, props)

        return AppConfig(
            googleSheetsSpreadsheetId = spreadsheetId,
            oauthRedirectUri = oauthUri,
            apiBaseUrl = apiUrl,
            appName = props.getProperty("app.name", "Payroll Desktop"),
            appVersion = props.getProperty("app.version", "1.0.0")
        )
    }

    /**
     * Get property and decrypt if encrypted
     */
    private fun getPropertyDecrypted(props: Properties, key: String, default: String): String {
        val value = props.getProperty(key, default)
        if (value.isBlank()) return value

        return try {
            // Check if value is encrypted
            if (EncryptionManager.isEncrypted(value)) {
                Logger.debug(TAG, "🔒 Decrypting config value: $key")
                EncryptionManager.decrypt(value)
            } else {
                value
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to decrypt $key, using as-is: ${e.message}")
            value
        }
    }

    /**
     * Auto-encrypt sensitive values if they are still plaintext
     * This provides seamless migration from plaintext to encrypted configs
     */
    private fun autoEncryptSensitiveValues(configFile: File, props: Properties) {
        val sensitiveKeys = listOf(
            "google.sheets.spreadsheet_id",
            "api.base_url"
        )

        var needsSave = false

        for (key in sensitiveKeys) {
            val value = props.getProperty(key)
            if (!value.isNullOrBlank() && !EncryptionManager.isEncrypted(value)) {
                try {
                    // Encrypt the plaintext value
                    val encrypted = EncryptionManager.encrypt(value)
                    props.setProperty(key, encrypted)
                    needsSave = true
                    Logger.info(TAG, "🔒 Auto-encrypted config value: $key")
                } catch (e: Exception) {
                    Logger.error(TAG, "Failed to auto-encrypt $key: ${e.message}")
                }
            }
        }

        // Save back to file if we encrypted anything
        if (needsSave) {
            try {
                configFile.outputStream().use { props.store(it, "Auto-encrypted sensitive values") }
                Logger.info(TAG, "✅ Config file updated with encrypted values")
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to save encrypted config: ${e.message}")
            }
        }
    }
}
