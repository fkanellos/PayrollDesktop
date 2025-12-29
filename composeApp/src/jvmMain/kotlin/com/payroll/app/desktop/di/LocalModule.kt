package com.payroll.app.desktop.di

import com.payroll.app.desktop.data.repositories.CalendarRepository
import com.payroll.app.desktop.data.repositories.LocalClientRepository
import com.payroll.app.desktop.data.repositories.LocalEmployeeRepository
import com.payroll.app.desktop.data.repositories.SqlDelightClientRepository
import com.payroll.app.desktop.data.repositories.SqlDelightEmployeeRepository
import com.payroll.app.desktop.database.DriverFactory
import com.payroll.app.desktop.database.PayrollDatabase
import com.payroll.app.desktop.domain.service.ClientMatchingService
import com.payroll.app.desktop.domain.service.PayrollCalculationService
import com.payroll.app.desktop.google.GoogleCalendarRepository
import com.payroll.app.desktop.google.GoogleCredentialProvider
import com.payroll.app.desktop.google.GoogleSheetsService
import org.koin.dsl.module

/**
 * Local Koin DI module for desktop-specific dependencies
 * Provides SQLDelight database and Google Calendar/Sheets integration
 *
 * IMPORTANT:
 * - Google Calendar = READ ONLY (never write to calendar!)
 * - Google Sheets = WRITE allowed (for adding new clients)
 */
val localModule = module {

    // Database
    single { DriverFactory.createDriver() }
    single { PayrollDatabase(get()) }

    // Local Repositories (SQLDelight)
    single<LocalEmployeeRepository> { SqlDelightEmployeeRepository(get()) }
    single<LocalClientRepository> { SqlDelightClientRepository(get()) }

    // Google APIs
    single { GoogleCredentialProvider() }
    single<CalendarRepository> { GoogleCalendarRepository(get()) }  // READ ONLY!
    single { GoogleSheetsService(get()) }  // WRITE allowed

    // Services
    single { ClientMatchingService() }
    single { PayrollCalculationService(get()) }
}
