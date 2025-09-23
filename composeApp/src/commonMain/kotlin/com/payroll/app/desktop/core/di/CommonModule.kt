package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.data.repositories.EmployeeRepository
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.presentation.payroll.PayrollViewModel
import org.koin.dsl.module

/**
 * Common Koin DI module for shared dependencies
 */
val commonModule = module {

    // Network layer
    single<PayrollApiService> { PayrollApiService() }

    // Repository layer
    single<PayrollRepository> { PayrollRepository(get()) }
    single<EmployeeRepository> { EmployeeRepository(get()) }

    // ViewModels
    factory<PayrollViewModel> { PayrollViewModel(get()) }
}