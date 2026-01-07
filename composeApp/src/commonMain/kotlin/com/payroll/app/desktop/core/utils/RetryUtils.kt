package com.payroll.app.desktop.core.utils

import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.logging.Logger
import kotlinx.coroutines.delay

/**
 * Utility for retrying operations with exponential backoff
 */
object RetryUtils {

    private const val TAG = "RetryUtils"

    /**
     * Retry an operation with exponential backoff
     *
     * @param maxAttempts Maximum number of retry attempts
     * @param initialDelayMs Initial delay between retries (doubles each time)
     * @param maxDelayMs Maximum delay between retries
     * @param shouldRetry Predicate to determine if we should retry on this exception
     * @param operation The operation to retry
     * @return Result of the operation
     */
    suspend fun <T> retryWithBackoff(
        maxAttempts: Int = AppConstants.Google.MAX_API_RETRY_ATTEMPTS,
        initialDelayMs: Long = AppConstants.Timing.RETRY_DELAY_MS,
        maxDelayMs: Long = 5000L,
        shouldRetry: (Throwable) -> Boolean = { isRetryableError(it) },
        operation: suspend () -> T
    ): T {
        var currentAttempt = 0
        var currentDelay = initialDelayMs

        while (true) {
            try {
                return operation()
            } catch (e: Exception) {
                currentAttempt++

                if (currentAttempt >= maxAttempts || !shouldRetry(e)) {
                    Logger.error(TAG, "Operation failed after $currentAttempt attempts", e)
                    throw e
                }

                Logger.warning(TAG, "Attempt $currentAttempt failed, retrying in ${currentDelay}ms: ${e.message}")
                delay(currentDelay)

                // Exponential backoff: double the delay, but cap at maxDelay
                currentDelay = minOf(currentDelay * 2, maxDelayMs)
            }
        }
    }

    /**
     * Determine if an error is retryable
     */
    private fun isRetryableError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""

        return when {
            // Network errors - retryable
            message.contains("timeout") -> true
            message.contains("connection") -> true
            message.contains("network") -> true
            message.contains("socket") -> true

            // HTTP errors
            message.contains("503") -> true // Service Unavailable
            message.contains("504") -> true // Gateway Timeout
            message.contains("429") -> true // Too Many Requests

            // Client errors - NOT retryable
            message.contains("400") -> false // Bad Request
            message.contains("401") -> false // Unauthorized
            message.contains("403") -> false // Forbidden
            message.contains("404") -> false // Not Found

            // Database errors - NOT retryable
            message.contains("database") -> false
            message.contains("constraint") -> false
            message.contains("unique") -> false

            // Default: retry
            else -> true
        }
    }

    /**
     * Execute with timeout and retry
     */
    suspend fun <T> executeWithTimeoutAndRetry(
        timeoutMs: Long = 30000L,
        maxAttempts: Int = AppConstants.Google.MAX_API_RETRY_ATTEMPTS,
        operation: suspend () -> T
    ): T {
        return retryWithBackoff(
            maxAttempts = maxAttempts,
            operation = {
                kotlinx.coroutines.withTimeout(timeoutMs) {
                    operation()
                }
            }
        )
    }
}
