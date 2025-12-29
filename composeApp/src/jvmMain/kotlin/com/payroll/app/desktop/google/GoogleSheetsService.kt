package com.payroll.app.desktop.google

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.payroll.app.desktop.domain.models.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google Sheets Service for writing client data
 *
 * IMPORTANT: This service ONLY writes to Google Sheets.
 * Google Calendar is READ-ONLY - we never write to it!
 *
 * Spreadsheet structure:
 * - Each employee has their own tab (sheet) identified by employee.sheetName
 * - Client data format: Name | Price | Employee Price | Company Price
 */
class GoogleSheetsService(
    private val credentialProvider: GoogleCredentialProvider
) {
    companion object {
        // Main payroll spreadsheet ID
        // TODO: This should be configurable, not hardcoded
        private const val SPREADSHEET_ID = "1NRXJV6Cd_fzdrzQwXL4exje38yOIrYSV"

        // Column mapping (A=Name, B=Price, C=Employee, D=Company)
        private const val CLIENT_DATA_RANGE = "A:D"
    }

    private var sheetsService: Sheets? = null

    init {
        try {
            sheetsService = credentialProvider.getSheetsService()
        } catch (e: Exception) {
            println("Failed to initialize Google Sheets: ${e.message}")
        }
    }

    /**
     * Check if sheets service is available
     */
    fun isAvailable(): Boolean = sheetsService != null

    /**
     * Add a new client to the employee's sheet tab
     *
     * @param client The client to add
     * @param sheetName The tab name (from employee.sheetName)
     * @return Result with success/failure info
     */
    suspend fun addClientToSheet(
        client: Client,
        sheetName: String
    ): SheetsWriteResult = withContext(Dispatchers.IO) {
        val service = sheetsService
        if (service == null) {
            return@withContext SheetsWriteResult.Error("Google Sheets service not available")
        }

        if (sheetName.isBlank()) {
            return@withContext SheetsWriteResult.Error("Employee sheet name is empty")
        }

        try {
            // Prepare the row data: Name | Price | Employee Price | Company Price
            val rowData = listOf(
                listOf(
                    client.name,
                    client.price,
                    client.employeePrice,
                    client.companyPrice
                )
            )

            val body = ValueRange().setValues(rowData)

            // Append to the employee's sheet tab
            // Format: 'SheetName'!A:D
            val range = "'$sheetName'!$CLIENT_DATA_RANGE"

            println("📝 Writing to Google Sheets:")
            println("   Spreadsheet: $SPREADSHEET_ID")
            println("   Sheet/Tab: $sheetName")
            println("   Range: $range")
            println("   Data: ${client.name} | €${client.price} | €${client.employeePrice} | €${client.companyPrice}")

            val result = service.spreadsheets().values()
                .append(SPREADSHEET_ID, range, body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute()

            val updatedRange = result.updates?.updatedRange ?: "unknown"
            println("✅ Successfully wrote to: $updatedRange")

            SheetsWriteResult.Success(
                sheetName = sheetName,
                updatedRange = updatedRange,
                rowsAdded = 1
            )

        } catch (e: Exception) {
            println("❌ Error writing to Google Sheets: ${e.message}")
            e.printStackTrace()

            // Provide helpful error messages
            val errorMessage = when {
                e.message?.contains("404") == true ->
                    "Sheet tab '$sheetName' not found. Check employee's sheetName setting."
                e.message?.contains("403") == true ->
                    "Permission denied. Check Google Sheets API access."
                e.message?.contains("401") == true ->
                    "Authentication failed. Please re-authenticate."
                else ->
                    "Failed to write to sheet: ${e.message}"
            }

            SheetsWriteResult.Error(errorMessage)
        }
    }

    /**
     * Verify that a sheet tab exists for an employee
     */
    suspend fun verifySheetExists(sheetName: String): Boolean = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext false

        try {
            val spreadsheet = service.spreadsheets()
                .get(SPREADSHEET_ID)
                .execute()

            val sheetNames = spreadsheet.sheets?.map { it.properties?.title } ?: emptyList()

            println("📋 Available sheets: $sheetNames")

            sheetNames.contains(sheetName)
        } catch (e: Exception) {
            println("Error checking sheet existence: ${e.message}")
            false
        }
    }

    /**
     * Get list of all sheet tab names in the spreadsheet
     */
    suspend fun getSheetNames(): List<String> = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext emptyList()

        try {
            val spreadsheet = service.spreadsheets()
                .get(SPREADSHEET_ID)
                .execute()

            spreadsheet.sheets?.mapNotNull { it.properties?.title } ?: emptyList()
        } catch (e: Exception) {
            println("Error getting sheet names: ${e.message}")
            emptyList()
        }
    }
}

/**
 * Result of a sheets write operation
 */
sealed class SheetsWriteResult {
    data class Success(
        val sheetName: String,
        val updatedRange: String,
        val rowsAdded: Int
    ) : SheetsWriteResult()

    data class Error(val message: String) : SheetsWriteResult()
}
