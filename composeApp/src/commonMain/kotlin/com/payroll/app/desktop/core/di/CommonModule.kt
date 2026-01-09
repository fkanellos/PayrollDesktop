package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.domain.service.ClientMatchingService
import com.payroll.app.desktop.domain.service.PayrollCalculationService
import org.koin.dsl.module

/**
 * Common Koin DI module for shared dependencies
 * Contains platform-independent services and utilities
 *
 * Platform-specific dependencies (repositories, database, Google APIs) are in:
 * - jvmMain/localModule
 * - androidMain/androidModule (future)
 * - iosMain/iosModule (future)
 */
val commonModule = module {
    // 🔥 FIX ARCHITECTURE SMELL: Populate with shared services

    // Core business logic services - platform independent
    factory { ClientMatchingService() }
    factory { PayrollCalculationService(clientMatchingService = get()) }

    // Future shared services can be added here:
    // - Validators (ClientValidator, EmployeeValidator)
    // - UseCases (if we extract from services)
    // - Mappers (domain <-> presentation)
}