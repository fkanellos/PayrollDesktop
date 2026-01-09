package com.payroll.app.desktop.core.utils

import com.payroll.app.desktop.core.strings.Strings
import kotlin.math.round

/**
 * Validation utilities for user input
 * Provides type-safe validation with error messages
 */
object ValidationUtils {

    /**
     * Format a Double to 2 decimal places (KMP-compatible)
     */
    private fun Double.toFormattedString(): String {
        val rounded = round(this * 100) / 100
        return rounded.toString()
    }

    /**
     * Validation result sealed class
     */
    sealed class ValidationResult<out T> {
        data class Success<T>(val value: T) : ValidationResult<T>()
        data class Error(val message: String) : ValidationResult<Nothing>()
    }

    /**
     * Validation limits for prices
     */
    object PriceLimits {
        const val MIN_PRICE = 0.0
        const val MAX_PRICE = 10000.0
        const val MIN_SESSION_PRICE = 0.0
        const val MAX_SESSION_PRICE = 1000.0
        const val MIN_SUPERVISION_PRICE = 0.0
        const val MAX_SUPERVISION_PRICE = 500.0
    }

    /**
     * Validate a price string input
     * Returns ValidationResult with the parsed Double or error message
     *
     * @param input The string input to validate
     * @param fieldName The name of the field for error messages
     * @param minValue Minimum allowed value (inclusive)
     * @param maxValue Maximum allowed value (inclusive)
     * @return ValidationResult with parsed value or error message
     */
    fun validatePrice(
        input: String,
        fieldName: String = Strings.Fields.price,
        minValue: Double = PriceLimits.MIN_PRICE,
        maxValue: Double = PriceLimits.MAX_PRICE
    ): ValidationResult<Double> {
        // Check if empty
        if (input.isBlank()) {
            return ValidationResult.Error(Strings.Validation.fieldEmpty(fieldName))
        }

        // Try to parse as double
        val value = input.toDoubleOrNull()
            ?: return ValidationResult.Error(Strings.Validation.fieldMustBeNumber(fieldName))

        // Check if negative
        if (value < minValue) {
            return ValidationResult.Error(Strings.Validation.fieldMinValue(fieldName, minValue.toString()))
        }

        // Check if too large
        if (value > maxValue) {
            return ValidationResult.Error(Strings.Validation.fieldMaxValue(fieldName, maxValue.toString()))
        }

        // Check if NaN or infinite
        if (!value.isFinite()) {
            return ValidationResult.Error(Strings.Validation.fieldInvalidValue(fieldName))
        }

        return ValidationResult.Success(value)
    }

    /**
     * Validate session price (total price)
     */
    fun validateSessionPrice(input: String): ValidationResult<Double> {
        return validatePrice(
            input,
            fieldName = Strings.Fields.sessionPrice,
            minValue = PriceLimits.MIN_SESSION_PRICE,
            maxValue = PriceLimits.MAX_SESSION_PRICE
        )
    }

    /**
     * Validate employee price (employee share)
     */
    fun validateEmployeePrice(input: String): ValidationResult<Double> {
        return validatePrice(
            input,
            fieldName = Strings.Fields.employeePrice,
            minValue = PriceLimits.MIN_SESSION_PRICE,
            maxValue = PriceLimits.MAX_SESSION_PRICE
        )
    }

    /**
     * Validate company price (company share)
     */
    fun validateCompanyPrice(input: String): ValidationResult<Double> {
        return validatePrice(
            input,
            fieldName = Strings.Fields.companyPrice,
            minValue = PriceLimits.MIN_SESSION_PRICE,
            maxValue = PriceLimits.MAX_SESSION_PRICE
        )
    }

    /**
     * Validate supervision price
     */
    fun validateSupervisionPrice(input: String): ValidationResult<Double> {
        return validatePrice(
            input,
            fieldName = Strings.Fields.supervisionPrice,
            minValue = PriceLimits.MIN_SUPERVISION_PRICE,
            maxValue = PriceLimits.MAX_SUPERVISION_PRICE
        )
    }

    /**
     * Validate that price splits add up correctly
     * Employee price + Company price should equal Total price
     *
     * @param totalPrice Total session price
     * @param employeePrice Employee's share
     * @param companyPrice Company's share
     * @return ValidationResult with Unit on success or error message
     */
    fun validatePriceSplit(
        totalPrice: Double,
        employeePrice: Double,
        companyPrice: Double
    ): ValidationResult<Unit> {
        val sum = employeePrice + companyPrice
        val tolerance = 0.01 // Allow 1 cent tolerance for floating point

        if (kotlin.math.abs(sum - totalPrice) > tolerance) {
            return ValidationResult.Error(
                Strings.Validation.priceSumMismatch(sum.toFormattedString(), totalPrice.toFormattedString())
            )
        }

        return ValidationResult.Success(Unit)
    }

    /**
     * Safe conversion with default value
     * Use this for non-critical parsing where you want a fallback
     *
     * @param input The string to parse
     * @param defaultValue The default value if parsing fails
     * @return Parsed double or default value
     */
    fun parseDoubleOrDefault(input: String, defaultValue: Double): Double {
        return input.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 } ?: defaultValue
    }

    /**
     * Validate email format (basic check)
     */
    fun validateEmail(email: String): ValidationResult<String> {
        if (email.isBlank()) {
            return ValidationResult.Error(Strings.Validation.emailEmpty)
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!email.matches(emailRegex)) {
            return ValidationResult.Error(Strings.Validation.emailInvalidFormat)
        }

        return ValidationResult.Success(email)
    }

    /**
     * Validate non-empty string
     */
    fun validateNonEmpty(input: String, fieldName: String = Strings.Fields.generic): ValidationResult<String> {
        if (input.isBlank()) {
            return ValidationResult.Error(Strings.Validation.fieldEmpty(fieldName))
        }
        return ValidationResult.Success(input.trim())
    }
}