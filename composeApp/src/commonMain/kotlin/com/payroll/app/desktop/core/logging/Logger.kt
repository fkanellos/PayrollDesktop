package com.payroll.app.desktop.core.logging

/**
 * Simple logging utility for the Payroll application
 * Provides different log levels and formatted output
 */
object Logger {

    private var isDebugEnabled = true

    enum class Level {
        DEBUG, INFO, WARNING, ERROR
    }

    /**
     * Enable or disable debug logging
     */
    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }

    /**
     * Log debug message (only shown when debug is enabled)
     */
    fun debug(tag: String, message: String) {
        if (isDebugEnabled) {
            log(Level.DEBUG, tag, message)
        }
    }

    /**
     * Log info message
     */
    fun info(tag: String, message: String) {
        log(Level.INFO, tag, message)
    }

    /**
     * Log warning message
     */
    fun warning(tag: String, message: String) {
        log(Level.WARNING, tag, message)
    }

    /**
     * Log error message
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message)
        throwable?.let {
            log(Level.ERROR, tag, "Exception: ${it.message}")
            log(Level.ERROR, tag, it.stackTraceToString())
        }
    }

    private fun log(level: Level, tag: String, message: String) {
        val icon = when (level) {
            Level.DEBUG -> "🔍"
            Level.INFO -> "ℹ️"
            Level.WARNING -> "⚠️"
            Level.ERROR -> "❌"
        }

        val timestamp = getCurrentTimestamp()
        println("$timestamp $icon [$level] $tag: $message")
    }

    /**
     * Platform-specific timestamp implementation
     * Will be overridden by expect/actual if needed
     */
    private fun getCurrentTimestamp(): String {
        return kotlinx.datetime.Clock.System.now().toString().substringBefore(".")
    }
}