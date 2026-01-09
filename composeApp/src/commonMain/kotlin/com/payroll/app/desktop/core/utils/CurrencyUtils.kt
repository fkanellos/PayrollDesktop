package com.payroll.app.desktop.core.utils

/**
 * Currency Utilities
 * Provides precise currency calculations with proper rounding
 *
 * 🔥 CRITICAL FIX: Uses BigDecimal internally to avoid floating-point precision errors
 * The old approach (round(this * 100) / 100) had precision issues with values like 46.50000000001
 */

/**
 * Round a Double to 2 decimal places (cents precision)
 * Uses BigDecimal internally to ensure exact precision
 *
 * Examples:
 * - 46.50000000001 -> 46.50
 * - 123.456 -> 123.46
 * - 0.125 -> 0.13 (HALF_UP rounding)
 *
 * Why BigDecimal:
 * - Double: 3 * 15.50 = 46.50000000001 (floating-point error)
 * - BigDecimal: 3 * 15.50 = 46.50 (exact)
 */
fun Double.roundToCents(): Double {
    return try {
        // Convert to BigDecimal, round to 2 decimals, convert back to Double
        val bigDecimal = this.toBigDecimal()
        bigDecimal.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    } catch (e: Exception) {
        // Fallback for NaN/Infinity
        if (this.isNaN()) 0.0
        else if (this.isInfinite()) if (this > 0) Double.MAX_VALUE else Double.MIN_VALUE
        else this
    }
}

/**
 * Multiply with proper rounding for currency
 * Example: 3 sessions * €15.50 = €46.50 (exact, not 46.50000000001)
 *
 * Uses BigDecimal for intermediate calculation to avoid precision loss
 */
fun Double.multiplyAndRound(multiplier: Int): Double {
    return try {
        val bigDecimal = this.toBigDecimal()
        val result = bigDecimal.multiply(multiplier.toBigDecimal())
        result.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    } catch (e: Exception) {
        (this * multiplier).roundToCents()
    }
}

/**
 * Add with proper rounding for currency accumulation
 * Example: €46.50 + €30.20 = €76.70 (exact)
 */
fun Double.addAndRound(other: Double): Double {
    return try {
        val bigDecimal1 = this.toBigDecimal()
        val bigDecimal2 = other.toBigDecimal()
        val result = bigDecimal1.add(bigDecimal2)
        result.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    } catch (e: Exception) {
        (this + other).roundToCents()
    }
}
