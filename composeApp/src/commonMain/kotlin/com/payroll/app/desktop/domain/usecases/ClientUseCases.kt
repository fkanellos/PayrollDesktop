package com.payroll.app.desktop.domain.usecases

import com.payroll.app.desktop.domain.models.ClientSimple
import com.payroll.app.desktop.domain.validation.ClientValidator
import com.payroll.app.desktop.domain.validation.ValidationResult

class ClientUseCases {

    fun createClient(
        client: ClientSimple,
        existingClients: List<ClientSimple>
    ): Result<ClientSimple> {
        val validationResult = ClientValidator.validateClient(client, existingClients)

        return when (validationResult) {
            is ValidationResult.Valid -> Result.success(client)
            is ValidationResult.Invalid -> Result.failure(
                ClientValidationException(validationResult.errors)
            )
        }
    }

    fun updateClient(
        client: ClientSimple,
        existingClients: List<ClientSimple>
    ): Result<ClientSimple> {
        val validationResult = ClientValidator.validateClient(client, existingClients)

        return when (validationResult) {
            is ValidationResult.Valid -> Result.success(client)
            is ValidationResult.Invalid -> Result.failure(
                ClientValidationException(validationResult.errors)
            )
        }
    }

    fun validateClient(
        client: ClientSimple,
        existingClients: List<ClientSimple>
    ): ValidationResult {
        return ClientValidator.validateClient(client, existingClients)
    }

    /**
     * 🆕 Auto-calculates COMPANY price from total and employee price
     * Formula: CompanyPrice = TotalPrice - EmployeePrice
     */
    fun calculateCompanyPrice(totalPrice: Double, employeePrice: Double): Double {
        return (totalPrice - employeePrice).coerceAtLeast(0.0)
    }

    /**
     * 🆕 Auto-calculates EMPLOYEE price from total and company price
     * Formula: EmployeePrice = TotalPrice - CompanyPrice
     */
    fun calculateEmployeePrice(totalPrice: Double, companyPrice: Double): Double {
        return (totalPrice - companyPrice).coerceAtLeast(0.0)
    }
}

class ClientValidationException(
    val errors: List<com.payroll.app.desktop.domain.validation.ValidationError>
) : Exception("Client validation failed: ${errors.joinToString { it.message }}")