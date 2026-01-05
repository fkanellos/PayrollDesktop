package com.payroll.app.desktop.domain.models

/**
 * ClientSimple - Used for validation purposes
 * This model is used by ClientValidator for client validation
 */
data class ClientSimple(
    val id: String,
    val name: String,
    val price: Double,
    val employeePrice: Double,
    val companyPrice: Double,
    val employeeId: String
)