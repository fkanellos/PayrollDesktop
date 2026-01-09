package com.payroll.app.desktop.domain.validation

import com.payroll.app.desktop.domain.models.ClientSimple
import kotlin.test.*

/**
 * Unit Tests for ClientValidator
 * Tests validation rules for client creation and updates
 */
class ClientValidatorTest {

    @Test
    fun `valid client passes validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Valid, "Valid client should pass validation")
    }

    @Test
    fun `blank name fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid, "Blank name should fail")
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.NAME })
        assertTrue(errors.any { it.code == ErrorCode.REQUIRED_FIELD })
    }

    @Test
    fun `whitespace-only name fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "   ",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.NAME })
    }

    @Test
    fun `negative total price fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = -50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.PRICE })
        assertTrue(errors.any { it.code == ErrorCode.NEGATIVE_VALUE })
    }

    @Test
    fun `negative employee price fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = -30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.EMPLOYEE_PRICE })
        assertTrue(errors.any { it.code == ErrorCode.NEGATIVE_VALUE })
    }

    @Test
    fun `negative company price fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = -20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.COMPANY_PRICE })
        assertTrue(errors.any { it.code == ErrorCode.NEGATIVE_VALUE })
    }

    @Test
    fun `price mismatch fails validation`() {
        // Given - employeePrice + companyPrice != totalPrice
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 25.0, // Sum = 55, not 50!
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.code == ErrorCode.PRICE_MISMATCH })
    }

    @Test
    fun `price mismatch within tolerance passes validation`() {
        // Given - Small rounding difference (0.01€ tolerance)
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.005, // Sum = 50.005, within 0.01€ tolerance
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Valid, "Should pass with 0.01€ tolerance")
    }

    @Test
    fun `employee price exceeds total fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 60.0, // Exceeds total!
            companyPrice = -10.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.EMPLOYEE_PRICE })
        assertTrue(errors.any { it.code == ErrorCode.INVALID_VALUE })
    }

    @Test
    fun `company price exceeds total fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = -10.0,
            companyPrice = 60.0, // Exceeds total!
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.COMPANY_PRICE })
        assertTrue(errors.any { it.code == ErrorCode.INVALID_VALUE })
    }

    @Test
    fun `duplicate name for same employee fails validation`() {
        // Given
        val existingClient = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        val newClient = ClientSimple(
            id = "2",
            name = "John Doe", // Same name
            price = 60.0,
            employeePrice = 35.0,
            companyPrice = 25.0,
            employeeId = "emp1" // Same employee
        )

        // When
        val result = ClientValidator.validateClient(newClient, listOf(existingClient))

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.NAME })
        assertTrue(errors.any { it.code == ErrorCode.DUPLICATE })
    }

    @Test
    fun `duplicate name for different employee passes validation`() {
        // Given - Same name but different employee is OK
        val existingClient = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        val newClient = ClientSimple(
            id = "2",
            name = "John Doe", // Same name
            price = 60.0,
            employeePrice = 35.0,
            companyPrice = 25.0,
            employeeId = "emp2" // Different employee
        )

        // When
        val result = ClientValidator.validateClient(newClient, listOf(existingClient))

        // Then
        assertTrue(result is ValidationResult.Valid, "Same name for different employee should be allowed")
    }

    @Test
    fun `case insensitive duplicate detection`() {
        // Given
        val existingClient = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        val newClient = ClientSimple(
            id = "2",
            name = "JOHN DOE", // Different case
            price = 60.0,
            employeePrice = 35.0,
            companyPrice = 25.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(newClient, listOf(existingClient))

        // Then
        assertTrue(result is ValidationResult.Invalid, "Should detect duplicate regardless of case")
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.code == ErrorCode.DUPLICATE })
    }

    @Test
    fun `update existing client with same name passes validation`() {
        // Given - Updating a client with its own name should be allowed
        val existingClient = ClientSimple(
            id = "1",
            name = "John Doe",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        val updatedClient = ClientSimple(
            id = "1", // Same ID
            name = "John Doe", // Same name
            price = 60.0, // Updated price
            employeePrice = 35.0,
            companyPrice = 25.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(updatedClient, listOf(existingClient))

        // Then
        assertTrue(result is ValidationResult.Valid, "Should allow updating client with its own name")
    }

    @Test
    fun `NaN price fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = Double.NaN,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.field == ClientValidator.ClientFormFields.PRICE })
        assertTrue(errors.any { it.code == ErrorCode.INVALID_NUMBER })
    }

    @Test
    fun `Infinity price fails validation`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "John Doe",
            price = Double.POSITIVE_INFINITY,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.any { it.code == ErrorCode.INVALID_NUMBER })
    }

    @Test
    fun `validatePriceDistribution returns true for valid distribution`() {
        // When
        val isValid = ClientValidator.validatePriceDistribution(
            totalPrice = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0
        )

        // Then
        assertTrue(isValid, "Should validate correct price distribution")
    }

    @Test
    fun `validatePriceDistribution returns false for invalid sum`() {
        // When
        val isValid = ClientValidator.validatePriceDistribution(
            totalPrice = 50.0,
            employeePrice = 30.0,
            companyPrice = 25.0 // Sum = 55
        )

        // Then
        assertFalse(isValid, "Should reject invalid price distribution")
    }

    @Test
    fun `validatePriceDistribution returns false for negative prices`() {
        // When
        val isValid = ClientValidator.validatePriceDistribution(
            totalPrice = 50.0,
            employeePrice = -30.0,
            companyPrice = 80.0
        )

        // Then
        assertFalse(isValid, "Should reject negative prices")
    }

    @Test
    fun `validatePriceDistribution allows rounding tolerance`() {
        // When - 0.005€ difference should pass
        val isValid = ClientValidator.validatePriceDistribution(
            totalPrice = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.005
        )

        // Then
        assertTrue(isValid, "Should allow 0.01€ rounding tolerance")
    }

    @Test
    fun `zero prices are valid`() {
        // Given
        val client = ClientSimple(
            id = "1",
            name = "Free Session Client",
            price = 0.0,
            employeePrice = 0.0,
            companyPrice = 0.0,
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Valid, "Zero prices should be valid")
    }

    @Test
    fun `multiple validation errors are collected`() {
        // Given - Client with multiple errors
        val client = ClientSimple(
            id = "1",
            name = "", // Blank name
            price = -50.0, // Negative price
            employeePrice = -30.0, // Negative employee price
            companyPrice = Double.NaN, // Invalid company price
            employeeId = "emp1"
        )

        // When
        val result = ClientValidator.validateClient(client)

        // Then
        assertTrue(result is ValidationResult.Invalid)
        val errors = (result as ValidationResult.Invalid).errors
        assertTrue(errors.size >= 4, "Should collect all validation errors")
    }
}
