package com.payroll.app.desktop.core.config

/**
 * Configuration Loader
 * Loads app configuration from file system
 */
expect class ConfigLoader {
    /**
     * Load configuration from app.config file
     * Falls back to default if file doesn't exist or has errors
     */
    fun loadConfig(): AppConfig
}
