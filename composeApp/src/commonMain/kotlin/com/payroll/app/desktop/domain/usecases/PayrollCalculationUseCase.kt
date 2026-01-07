package com.payroll.app.desktop.domain.usecases

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.models.PayrollRequest
import com.payroll.app.desktop.domain.models.PayrollResponse
import com.payroll.app.desktop.domain.models.UncertainMatch
import com.payroll.app.desktop.presentation.payroll.IMatchConfirmationRepository
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for payroll calculation
 *
 * Responsibilities:
 * - Calculate payroll for employee and date range
 * - Filter out already confirmed/rejected matches
 * - Return calculation result with uncertain matches
 */
class PayrollCalculationUseCase(
    private val payrollRepository: PayrollRepository,
    private val matchConfirmationRepository: IMatchConfirmationRepository
) {
    companion object {
        private const val TAG = "PayrollCalculationUseCase"
    }

    /**
     * Calculate payroll for an employee within a date range
     *
     * @return Result with PayrollResponse and filtered uncertain matches
     */
    suspend operator fun invoke(
        employee: Employee,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): CalculationResult {
        return try {
            Logger.info(TAG, "Calculating payroll for ${employee.name} from $startDate to $endDate")

            val request = PayrollRequest(
                employeeId = employee.id,
                startDate = startDate.toString(),
                endDate = endDate.toString()
            )

            when (val result = payrollRepository.calculatePayroll(request)) {
                is RepositoryResult.Success -> {
                    val payrollData = result.data

                    // Filter uncertain matches
                    val filteredMatches = filterUncertainMatches(
                        uncertainMatches = payrollData.eventTracking?.uncertainMatches ?: emptyList(),
                        employeeId = employee.id
                    )

                    Logger.info(TAG, "Calculation successful. ${filteredMatches.size} uncertain matches need review")

                    CalculationResult.Success(
                        payrollResponse = payrollData,
                        uncertainMatches = filteredMatches
                    )
                }
                is RepositoryResult.Error -> {
                    Logger.error(TAG, "Calculation failed", result.exception)
                    CalculationResult.Error(result.exception.message ?: "Σφάλμα υπολογισμού μισθοδοσίας")
                }
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error during calculation", e)
            CalculationResult.Error(e.message ?: "Απροσδόκητο σφάλμα υπολογισμού")
        }
    }

    /**
     * Filter out matches that have already been confirmed or rejected
     */
    private suspend fun filterUncertainMatches(
        uncertainMatches: List<UncertainMatch>,
        employeeId: String
    ): List<UncertainMatch> {
        Logger.debug(TAG, "Filtering ${uncertainMatches.size} uncertain matches")

        return uncertainMatches.filter { match ->
            val confirmed = matchConfirmationRepository.getConfirmedMatch(
                eventTitle = match.eventTitle,
                employeeId = employeeId
            )

            val shouldKeep = confirmed == null

            if (!shouldKeep) {
                val action = if (confirmed == AppConstants.Markers.REJECTED_MATCH_MARKER) {
                    "rejected"
                } else {
                    "confirmed as '$confirmed'"
                }
                Logger.debug(TAG, "Filtered out '${match.eventTitle}' (already $action)")
            }

            shouldKeep
        }
    }

    /**
     * Result of payroll calculation
     */
    sealed class CalculationResult {
        data class Success(
            val payrollResponse: PayrollResponse,
            val uncertainMatches: List<UncertainMatch>
        ) : CalculationResult()

        data class Error(val message: String) : CalculationResult()
    }
}
