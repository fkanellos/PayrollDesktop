package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.domain.usecases.ClientQuickAddUseCase
import com.payroll.app.desktop.domain.usecases.MatchConfirmationUseCase
import com.payroll.app.desktop.domain.usecases.PayrollCalculationUseCase
import com.payroll.app.desktop.domain.usecases.PayrollExportUseCase
import org.koin.dsl.module

/**
 * Koin module for UseCases
 *
 * UseCases encapsulate business logic and coordinate between repositories and services.
 * ViewModels should depend on UseCases rather than directly on repositories.
 */
val useCaseModule = module {

    // PayrollCalculationUseCase - calculates payroll and filters uncertain matches
    factory {
        PayrollCalculationUseCase(
            payrollRepository = get(),
            matchConfirmationRepository = get()
        )
    }

    // MatchConfirmationUseCase - confirms or rejects uncertain matches
    factory {
        MatchConfirmationUseCase(
            matchConfirmationRepository = get()
        )
    }

    // ClientQuickAddUseCase - quickly adds unmatched clients
    factory {
        ClientQuickAddUseCase(
            payrollRepository = get()
        )
    }

    // PayrollExportUseCase - exports payroll to PDF/Excel
    factory {
        PayrollExportUseCase(
            exportService = get()
        )
    }
}
