package com.payroll.app.desktop.domain.validation

import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.ValidationUtils
import com.payroll.app.desktop.core.utils.format
import com.payroll.app.desktop.domain.models.ClientSimple
import kotlin.math.abs

object ClientValidator {

    object ClientFormFields {
        const val NAME = "name"
        const val PRICE = "price"
        const val EMPLOYEE_PRICE = "employeePrice"
        const val COMPANY_PRICE = "companyPrice"
    }

    /**
     * Validates a client for creation or update
     *
     * Rules:
     * 1. Name must not be blank
     * 2. All prices must be >= 0
     * 3. EmployeePrice + CompanyPrice = Price (with 0.01€ tolerance for rounding)
     * 4. EmployeePrice <= Price
     * 5. CompanyPrice <= Price
     */
    fun validateClient(client: ClientSimple, existingClients: List<ClientSimple> = emptyList()): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Rule 1: Name validation
        if (client.name.isBlank()) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.NAME,
                    message = Strings.Validation.clientNameRequired,
                    code = ErrorCode.REQUIRED_FIELD
                )
            )
        }

        // Rule 2: Price validation - must be >= 0
        if (client.price < 0) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.PRICE,
                    message = Strings.Validation.clientPriceNegative,
                    code = ErrorCode.NEGATIVE_VALUE
                )
            )
        }

        if (client.employeePrice < 0) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.EMPLOYEE_PRICE,
                    message = Strings.Validation.employeePriceNegative,
                    code = ErrorCode.NEGATIVE_VALUE
                )
            )
        }

        if (client.companyPrice < 0) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.COMPANY_PRICE,
                    message = Strings.Validation.companyPriceNegative,
                    code = ErrorCode.NEGATIVE_VALUE
                )
            )
        }

        // Check for NaN or Infinite values
        if (!client.price.isFinite()) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.PRICE,
                    message = Strings.Validation.clientPriceInvalid,
                    code = ErrorCode.INVALID_NUMBER
                )
            )
        }

        if (!client.employeePrice.isFinite()) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.EMPLOYEE_PRICE,
                    message = Strings.Validation.employeePriceInvalid,
                    code = ErrorCode.INVALID_NUMBER
                )
            )
        }

        if (!client.companyPrice.isFinite()) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.COMPANY_PRICE,
                    message = Strings.Validation.companyPriceInvalid,
                    code = ErrorCode.INVALID_NUMBER
                )
            )
        }

        // Check maximum values
        if (client.price > ValidationUtils.PriceLimits.MAX_SESSION_PRICE) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.PRICE,
                    message = Strings.Validation.clientPriceExceedsMax(ValidationUtils.PriceLimits.MAX_SESSION_PRICE.toString()),
                    code = ErrorCode.EXCEEDS_MAXIMUM
                )
            )
        }

        if (client.employeePrice > ValidationUtils.PriceLimits.MAX_SESSION_PRICE) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.EMPLOYEE_PRICE,
                    message = Strings.Validation.employeePriceExceedsMax(ValidationUtils.PriceLimits.MAX_SESSION_PRICE.toString()),
                    code = ErrorCode.EXCEEDS_MAXIMUM
                )
            )
        }

        if (client.companyPrice > ValidationUtils.PriceLimits.MAX_SESSION_PRICE) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.COMPANY_PRICE,
                    message = Strings.Validation.companyPriceExceedsMax(ValidationUtils.PriceLimits.MAX_SESSION_PRICE.toString()),
                    code = ErrorCode.EXCEEDS_MAXIMUM
                )
            )
        }

        // Rule 3: Check if prices don't exceed total
        if (client.employeePrice > client.price) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.EMPLOYEE_PRICE,
                    message = Strings.Validation.employeePriceExceedsTotal(client.employeePrice.toString(), client.price.toString()),
                    code = ErrorCode.INVALID_VALUE
                )
            )
        }

        if (client.companyPrice > client.price) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.COMPANY_PRICE,
                    message = Strings.Validation.companyPriceExceedsTotal(client.companyPrice.toString(), client.price.toString()),
                    code = ErrorCode.INVALID_VALUE
                )
            )
        }

        // Rule 4: EmployeePrice + CompanyPrice = Price
        // Allow 0.01€ tolerance for rounding errors
        val sum = client.employeePrice + client.companyPrice
        val difference = abs(client.price - sum)

        if (difference > 0.01) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.COMPANY_PRICE,
                    message = Strings.Validation.pricesMismatch(
                        client.employeePrice.toString(),
                        client.companyPrice.toString(),
                        client.price.toString(),
                        sum.toString()
                    ),
                    code = ErrorCode.PRICE_MISMATCH
                )
            )
        }

        // Rule 5: Duplicate name check (optional)
        val isDuplicate = existingClients.any {
            it.id != client.id &&
                    it.name.equals(client.name, ignoreCase = true) &&
                    it.employeeId == client.employeeId
        }

        if (isDuplicate) {
            errors.add(
                ValidationError(
                    field = ClientFormFields.NAME,
                    message = Strings.Validation.clientNameDuplicate(client.name),
                    code = ErrorCode.DUPLICATE
                )
            )
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates price distribution
     */
    fun validatePriceDistribution(totalPrice: Double, employeePrice: Double, companyPrice: Double): Boolean {
        val sum = employeePrice + companyPrice
        val difference = abs(totalPrice - sum)
        return difference <= 0.01 && employeePrice >= 0 && companyPrice >= 0
    }
}