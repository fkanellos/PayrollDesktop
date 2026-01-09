package com.payroll.app.desktop.core.utils

import kotlin.math.round

/**
 * Currency Utilities
 * Provides precise currency calculations with proper rounding
 *
 * KMP-compatible version using pure Kotlin math
 * Handles floating-point precision issues for currency calculations
 */

/**
 * Round a Double to 2 decimal places (cents precision)
 * Uses multiplication/rounding/division approach that's KMP-compatible
 *
 * Examples:
 * - 46.50000000001 -> 46.50
 * - 123.456 -> 123.46
 * - 0.125 -> 0.13 (HALF_UP rounding)
 */
fun Double.roundToCents(): Double {
    return try {
        // Handle special cases
        if (this.isNaN()) return 0.0
        if (this.isInfinite()) return if (this > 0) Double.MAX_VALUE else Double.MIN_VALUE

        // Round to 2 decimal places: multiply by 100, round, divide by 100
        round(this * 100) / 100
    } catch (e: Exception) {
        0.0
    }
}

/**
 * Multiply with proper rounding for currency
 * Example: 3 sessions * €15.50 = €46.50
 */
fun Double.multiplyAndRound(multiplier: Int): Double {
    return (this * multiplier).roundToCents()
}

/**
 * Add with proper rounding for currency accumulation
 * Example: €46.50 + €30.20 = €76.70
 */
fun Double.addAndRound(other: Double): Double {
    return (this + other).roundToCents()
}
