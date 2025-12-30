package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.data.repositories.ClientRepository
import com.payroll.app.desktop.data.repositories.EmployeeRepository
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.service.ClientPersistenceService
import com.payroll.app.desktop.presentation.client.ClientManagementViewModel
import com.payroll.app.desktop.presentation.employee.EmployeeManagementViewModel
import com.payroll.app.desktop.presentation.payroll.PayrollViewModel
import org.koin.dsl.module

/**
 * Common Koin DI module for shared dependencies
 *
 * STANDALONE DESKTOP APP:
 * - PayrollViewModel uses ClientPersistenceService for local SQLite + Sheets writes
 * - PayrollRepository is kept for backward compatibility but will be phased out
 */
val commonModule = module {

    // Network layer (kept for backward compatibility)
    single<PayrollApiService> { PayrollApiService() }

    // Repository layer (legacy - will be migrated to local repositories)
    single<PayrollRepository> { PayrollRepository(get()) }
    single<EmployeeRepository> { EmployeeRepository(get()) }
    single<ClientRepository> { ClientRepository(get()) }

    // ViewModels
    // PayrollViewModel now uses ClientPersistenceService from localModule (if available)
    factory<PayrollViewModel> {
        PayrollViewModel(
            payrollRepository = get(),
            clientPersistenceService = getOrNull<ClientPersistenceService>()
        )
    }
    factory<ClientManagementViewModel> { ClientManagementViewModel(get(), get()) }
    factory<EmployeeManagementViewModel> { EmployeeManagementViewModel(get()) }
}