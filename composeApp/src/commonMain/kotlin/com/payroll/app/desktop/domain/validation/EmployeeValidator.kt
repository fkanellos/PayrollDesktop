package com.payroll.app.desktop.domain.validation

import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.ValidationUtils
import com.payroll.app.desktop.core.utils.format
import com.payroll.app.desktop.domain.models.Employee

object EmployeeValidator {

    object EmployeeFormFields {
        const val NAME = "name"
        const val EMAIL = "email"
        const val SUPERVISION_PRICE = "supervisionPrice"
    }

    /**
     * Validates an employee for creation or update
     *
     * Rules:
     * 1. Name must not be blank
     * 2. Email must be valid format
     * 3. Supervision price must be >= 0
     * 4. Supervision price must not exceed maximum
     * 5. Supervision price must be a valid number (not NaN or Infinite)
     */
    fun validateEmployee(employee: Employee, existingEmployees: List<Employee> = emptyList()): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Rule 1: Name validation
        if (employee.name.isBlank()) {
            errors.add(
                ValidationError(
                    field = EmployeeFormFields.NAME,
                    message = Strings.Validation.employeeNameRequired,
                    code = ErrorCode.REQUIRED_FIELD
                )
            )
        }

        // Rule 2: Email validation
        if (employee.email.isNotBlank()) {
            val emailValidation = ValidationUtils.validateEmail(employee.email)
            if (emailValidation is ValidationUtils.ValidationResult.Error) {
                errors.add(
                    ValidationError(
                        field = EmployeeFormFields.EMAIL,
                        message = emailValidation.message,
                        code = ErrorCode.INVALID_FORMAT
                    )
                )
            }
        }

        // Rule 3: Supervision price must be non-negative
        if (employee.supervisionPrice < 0) {
            errors.add(
                ValidationError(
                    field = EmployeeFormFields.SUPERVISION_PRICE,
                    message = Strings.Validation.supervisionPriceNegative,
                    code = ErrorCode.NEGATIVE_VALUE
                )
            )
        }

        // Rule 4: Check for NaN or Infinite values
        if (!employee.supervisionPrice.isFinite()) {
            errors.add(
                ValidationError(
                    field = EmployeeFormFields.SUPERVISION_PRICE,
                    message = Strings.Validation.supervisionPriceInvalid,
                    code = ErrorCode.INVALID_NUMBER
                )
            )
        }

        // Rule 5: Check maximum value
        if (employee.supervisionPrice > ValidationUtils.PriceLimits.MAX_SUPERVISION_PRICE) {
            errors.add(
                ValidationError(
                    field = EmployeeFormFields.SUPERVISION_PRICE,
                    message = Strings.Validation.supervisionPriceExceedsMax.format(ValidationUtils.PriceLimits.MAX_SUPERVISION_PRICE),
                    code = ErrorCode.EXCEEDS_MAXIMUM
                )
            )
        }

        // Rule 6: Duplicate email check (if email is provided)
        if (employee.email.isNotBlank()) {
            val isDuplicateEmail = existingEmployees.any {
                it.id != employee.id &&
                        it.email.equals(employee.email, ignoreCase = true)
            }

            if (isDuplicateEmail) {
                errors.add(
                    ValidationError(
                        field = EmployeeFormFields.EMAIL,
                        message = Strings.Validation.employeeEmailDuplicate.format(employee.email),
                        code = ErrorCode.DUPLICATE
                    )
                )
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates supervision price string input
     * Returns parsed value or null on error
     */
    fun validateSupervisionPriceInput(input: String): ValidationUtils.ValidationResult<Double> {
        return ValidationUtils.validateSupervisionPrice(input)
    }
}