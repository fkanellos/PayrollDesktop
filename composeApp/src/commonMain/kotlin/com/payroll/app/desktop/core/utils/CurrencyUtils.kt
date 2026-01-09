package com.payroll.app.desktop.core.utils

import kotlin.math.round

/**
 * Currency Utilities
 * Provides precise currency calculations with proper rounding
 */

/**
 * Round a Double to 2 decimal places (cents precision)
 * Uses banker's rounding (round half to even) for fairness
 *
 * Examples:
 * - 46.50000000001 -> 46.50
 * - 123.456 -> 123.46
 * - 0.125 -> 0.12 (banker's rounding)
 */
fun Double.roundToCents(): Double {
    return round(this * 100) / 100
}

/**
 * Multiply with proper rounding for currency
 * Example: 3 sessions * €15.50 = €46.50 (not 46.50000000001)
 */
fun Double.multiplyAndRound(multiplier: Int): Double {
    return (this * multiplier).roundToCents()
}

/**
 * Add with proper rounding for currency accumulation
 */
fun Double.addAndRound(other: Double): Double {
    return (this + other).roundToCents()
}
