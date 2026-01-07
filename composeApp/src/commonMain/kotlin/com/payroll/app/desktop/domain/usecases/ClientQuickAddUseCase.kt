package com.payroll.app.desktop.domain.usecases

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.Client

/**
 * UseCase for quickly adding unmatched clients
 *
 * Responsibilities:
 * - Create new client with pricing
 * - Handle quick-add from unmatched events
 * - Validate client data
 */
class ClientQuickAddUseCase(
    private val payrollRepository: PayrollRepository
) {
    companion object {
        private const val TAG = "ClientQuickAddUseCase"
    }

    /**
     * Quickly add a new client
     *
     * @param name Client name
     * @param price Total session price
     * @param employeePrice Employee's share
     * @param companyPrice Company's share
     * @param employeeId Employee ID this client belongs to
     * @return Result of the operation
     */
    suspend operator fun invoke(
        name: String,
        price: Double,
        employeePrice: Double,
        companyPrice: Double,
        employeeId: String
    ): QuickAddResult {
        return try {
            Logger.info(TAG, "Quick-adding client: $name for employee $employeeId")

            // Create client object
            val newClient = Client(
                id = 0,
                name = name,
                price = price,
                employeePrice = employeePrice,
                companyPrice = companyPrice,
                employeeId = employeeId,
                pendingPayment = false
            )

            // Create via repository
            when (val result = payrollRepository.createClient(newClient)) {
                is RepositoryResult.Success -> {
                    Logger.info(TAG, "Client '$name' added successfully")
                    QuickAddResult.Success(
                        clientName = name,
                        client = result.data
                    )
                }
                is RepositoryResult.Error -> {
                    Logger.error(TAG, "Failed to add client '$name'", result.exception)
                    QuickAddResult.Error(
                        clientName = name,
                        message = result.exception.message ?: "Unknown error"
                    )
                }
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Error adding client '$name'", e)
            QuickAddResult.Error(
                clientName = name,
                message = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Result of quick-add operation
     */
    sealed class QuickAddResult {
        data class Success(
            val clientName: String,
            val client: Client
        ) : QuickAddResult()

        data class Error(
            val clientName: String,
            val message: String
        ) : QuickAddResult()
    }
}
