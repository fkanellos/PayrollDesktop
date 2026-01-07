package com.payroll.app.desktop.core.constants

/**
 * Application-wide constants
 * Centralizes magic numbers for better maintainability
 */
object AppConstants {

    /**
     * Timing constants (in milliseconds)
     */
    object Timing {
        /** Delay to ensure UI is ready before loading data */
        const val UI_INIT_DELAY_MS = 100L

        /** Delay between retry attempts */
        const val RETRY_DELAY_MS = 500L

        /** Delay for automatic recalculation after user actions */
        const val AUTO_RECALC_DELAY_MS = 500L
    }

    /**
     * Payroll pricing defaults
     */
    object Pricing {
        /** Default price per therapy session (€) */
        const val DEFAULT_SESSION_PRICE = 50.0

        /** Default employee share per session (€) */
        const val DEFAULT_EMPLOYEE_SHARE = 22.5

        /** Default company share per session (€) */
        const val DEFAULT_COMPANY_SHARE = 27.5
    }

    /**
     * Google OAuth & API constants
     */
    object Google {
        /** Token refresh threshold (seconds before expiry) */
        const val TOKEN_REFRESH_THRESHOLD_SECONDS = 300L

        /** Maximum retry attempts for API calls */
        const val MAX_API_RETRY_ATTEMPTS = 3
    }

    /**
     * Database constants
     */
    object Database {
        /** SQLite pending_payment false value */
        const val PENDING_PAYMENT_FALSE = 0L

        /** SQLite pending_payment true value */
        const val PENDING_PAYMENT_TRUE = 1L
    }

    /**
     * Date range constants
     */
    object DateRange {
        /** Maximum date range in days */
        const val MAX_DATE_RANGE_DAYS = 365

        /** Warning threshold for large date ranges (days) */
        const val LARGE_DATE_RANGE_WARNING_DAYS = 90
    }

    /**
     * Special marker values
     */
    object Markers {
        /** Marker for rejected match confirmations */
        const val REJECTED_MATCH_MARKER = "__REJECTED__"
    }
}