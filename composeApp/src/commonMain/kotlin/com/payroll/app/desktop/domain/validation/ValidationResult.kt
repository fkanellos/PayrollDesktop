package com.payroll.app.desktop.domain.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()

    val isValid: Boolean
        get() = this is Valid

    val isInvalid: Boolean
        get() = this is Invalid

    // 🔧 RENAMED from getErrors() to errorsList()
    fun errorsList(): List<ValidationError> {
        return when (this) {
            is Invalid -> errors
            is Valid -> emptyList()
        }
    }
}

data class ValidationError(
    val field: String,
    val message: String,
    val code: ErrorCode
)

enum class ErrorCode {
    REQUIRED_FIELD,
    INVALID_FORMAT,
    INVALID_VALUE,
    PRICE_MISMATCH,
    NEGATIVE_VALUE,
    DUPLICATE
}