package com.payroll.app.desktop.domain.usecases

import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.domain.models.UncertainMatch
import com.payroll.app.desktop.presentation.payroll.IMatchConfirmationRepository

/**
 * UseCase for managing match confirmations and rejections
 *
 * Responsibilities:
 * - Confirm uncertain matches
 * - Reject uncertain matches
 * - Save confirmations/rejections to database
 */
class MatchConfirmationUseCase(
    private val matchConfirmationRepository: IMatchConfirmationRepository
) {
    companion object {
        private const val TAG = "MatchConfirmationUseCase"
    }

    /**
     * Confirm a match between event and client
     */
    suspend fun confirmMatch(
        match: UncertainMatch,
        employeeId: String
    ): ConfirmationResult {
        return try {
            val suggestedMatch = match.suggestedMatch
                ?: return ConfirmationResult.Error("Δεν υπάρχει προτεινόμενη αντιστοιχία")

            Logger.info(TAG, "Confirming match: '${match.eventTitle}' → '${suggestedMatch.clientName}'")

            matchConfirmationRepository.saveConfirmation(
                eventTitle = match.eventTitle,
                matchedClientName = suggestedMatch.clientName,
                employeeId = employeeId
            )

            ConfirmationResult.Confirmed(
                eventTitle = match.eventTitle,
                clientName = suggestedMatch.clientName
            )
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to confirm match", e)
            ConfirmationResult.Error("Σφάλμα αποθήκευσης: ${e.message}")
        }
    }

    /**
     * Reject a match - mark event as unmatched
     */
    suspend fun rejectMatch(
        match: UncertainMatch,
        employeeId: String
    ): ConfirmationResult {
        return try {
            Logger.info(TAG, "Rejecting match: '${match.eventTitle}'")

            // Save rejection marker to database
            matchConfirmationRepository.saveConfirmation(
                eventTitle = match.eventTitle,
                matchedClientName = AppConstants.Markers.REJECTED_MATCH_MARKER,
                employeeId = employeeId
            )

            ConfirmationResult.Rejected(eventTitle = match.eventTitle)
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to reject match", e)
            ConfirmationResult.Error("Σφάλμα αποθήκευσης rejection: ${e.message}")
        }
    }

    /**
     * Result of confirmation/rejection operation
     */
    sealed class ConfirmationResult {
        data class Confirmed(
            val eventTitle: String,
            val clientName: String
        ) : ConfirmationResult()

        data class Rejected(
            val eventTitle: String
        ) : ConfirmationResult()

        data class Error(val message: String) : ConfirmationResult()
    }
}
