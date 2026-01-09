package com.payroll.app.desktop.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.payroll.app.desktop.core.config.AppConfig
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.utils.RetryUtils
import com.payroll.app.desktop.domain.models.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

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
    private val credentialProvider: GoogleCredentialProvider,
    private val appConfig: AppConfig
) {
    companion object {
        private const val TAG = "GoogleSheetsService"

        // Column mapping (A=Name, B=Price, C=Employee, D=Company)
        private const val CLIENT_DATA_RANGE = "A:D"
    }

    private var sheetsService: Sheets? = null
    private val spreadsheetId: String = appConfig.googleSheetsSpreadsheetId

    init {
        try {
            sheetsService = credentialProvider.getSheetsService()
        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error initializing Sheets: ${e.statusCode} - ${e.statusMessage}", e)
        } catch (e: IOException) {
            Logger.error(TAG, "Network error initializing Google Sheets", e)
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error initializing Google Sheets: ${e::class.simpleName}", e)
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

            Logger.info(TAG, "Writing to Google Sheets")
            // 🔒 SECURITY: Don't log spreadsheet IDs or sensitive data

            val result = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute()
            }

            val updatedRange = result.updates?.updatedRange ?: "unknown"
            Logger.info(TAG, "Successfully wrote to: $updatedRange")

            SheetsWriteResult.Success(
                sheetName = sheetName,
                updatedRange = updatedRange,
                rowsAdded = 1
            )

        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error writing to Sheets: ${e.statusCode}", e)

            // Provide helpful error messages based on HTTP status code
            val errorMessage = when (e.statusCode) {
                404 -> "Sheet tab '$sheetName' not found. Check employee's sheetName setting."
                403 -> "Permission denied. Check Google Sheets API access."
                401 -> "Authentication failed. Please re-authenticate."
                429 -> "Rate limit exceeded. Please try again later."
                else -> "Google API error (${e.statusCode}): ${e.statusMessage}"
            }

            SheetsWriteResult.Error(errorMessage)
        } catch (e: IOException) {
            Logger.error(TAG, "Network error writing to Google Sheets", e)
            SheetsWriteResult.Error("Network error: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Logger.error(TAG, "Invalid data for Google Sheets", e)
            SheetsWriteResult.Error("Invalid data: ${e.message}")
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error writing to Google Sheets: ${e::class.simpleName}", e)
            SheetsWriteResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Update an existing client in the employee's sheet tab
     * Finds the client by name and updates the price values
     *
     * @param client The client data to update
     * @param sheetName The tab name (from employee.sheetName)
     * @return Result with success/failure info
     */
    suspend fun updateClientInSheet(
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
            // First, find the row number where this client exists
            val range = "'$sheetName'!A2:D"  // Skip header row

            val response = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            }

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                return@withContext SheetsWriteResult.Error("No clients found in sheet '$sheetName'")
            }

            // Find the row index for this client (by name)
            val rowIndex = values.indexOfFirst { row ->
                row.isNotEmpty() && row[0].toString().trim().equals(client.name, ignoreCase = true)
            }

            if (rowIndex == -1) {
                return@withContext SheetsWriteResult.Error("Client '${client.name}' not found in sheet '$sheetName'")
            }

            // Calculate the actual row number in the sheet (adding 2 because: 1 for header, 1 for 0-based index)
            val actualRowNumber = rowIndex + 2

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

            // Update the specific row
            val updateRange = "'$sheetName'!A$actualRowNumber:D$actualRowNumber"

            Logger.info(TAG, "Updating client in Google Sheets")
            Logger.debug(TAG, "Spreadsheet: $spreadsheetId")
            Logger.debug(TAG, "Sheet/Tab: $sheetName")
            Logger.debug(TAG, "Range: $updateRange")
            Logger.debug(TAG, "Data: ${client.name} | €${client.price} | €${client.employeePrice} | €${client.companyPrice}")

            val result = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .update(spreadsheetId, updateRange, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute()
            }

            val updatedRange = result.updatedRange ?: updateRange
            Logger.info(TAG, "Successfully updated: $updatedRange")

            SheetsWriteResult.Success(
                sheetName = sheetName,
                updatedRange = updatedRange,
                rowsAdded = 0  // 0 because we're updating, not adding
            )

        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error updating client in Sheets: ${e.statusCode}", e)

            val errorMessage = when (e.statusCode) {
                404 -> "Sheet tab '$sheetName' not found. Check employee's sheetName setting."
                403 -> "Permission denied. Check Google Sheets API access."
                401 -> "Authentication failed. Please re-authenticate."
                429 -> "Rate limit exceeded. Please try again later."
                else -> "Google API error (${e.statusCode}): ${e.statusMessage}"
            }

            SheetsWriteResult.Error(errorMessage)
        } catch (e: IOException) {
            Logger.error(TAG, "Network error updating client in Google Sheets", e)
            SheetsWriteResult.Error("Network error: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Logger.error(TAG, "Invalid data for Google Sheets", e)
            SheetsWriteResult.Error("Invalid data: ${e.message}")
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error updating client in Google Sheets: ${e::class.simpleName}", e)
            SheetsWriteResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Update an existing employee in the "Employees" sheet
     * Finds the employee by name and updates all their data
     *
     * @param employee The employee data to update
     * @return Result with success/failure info
     */
    suspend fun updateEmployeeInSheet(
        employee: com.payroll.app.desktop.domain.models.Employee
    ): SheetsWriteResult = withContext(Dispatchers.IO) {
        val service = sheetsService
        if (service == null) {
            return@withContext SheetsWriteResult.Error("Google Sheets service not available")
        }

        try {
            // First, find the row number where this employee exists
            val range = "Employees!A2:F"  // Skip header row

            val response = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            }

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                return@withContext SheetsWriteResult.Error("No employees found in 'Employees' sheet")
            }

            // Find the row index for this employee (by name)
            val rowIndex = values.indexOfFirst { row ->
                row.isNotEmpty() && row[0].toString().trim().equals(employee.name, ignoreCase = true)
            }

            if (rowIndex == -1) {
                return@withContext SheetsWriteResult.Error("Employee '${employee.name}' not found in 'Employees' sheet")
            }

            // Calculate the actual row number in the sheet (adding 2 because: 1 for header, 1 for 0-based index)
            val actualRowNumber = rowIndex + 2

            // Prepare the row data: Name | Email | Calendar ID | Client Sheet Name | Supervision (€)
            // Note: Column F (Clients) is usually a formula, so we don't update it
            val rowData = listOf(
                listOf(
                    employee.name,
                    employee.email,
                    employee.calendarId,
                    employee.sheetName,
                    employee.supervisionPrice
                    // Omit column F (Clients) - it's usually a formula
                )
            )

            val body = ValueRange().setValues(rowData)

            // Update the specific row (A to E only, not F)
            val updateRange = "Employees!A$actualRowNumber:E$actualRowNumber"

            Logger.info(TAG, "Updating employee in Google Sheets")
            Logger.debug(TAG, "Spreadsheet: $spreadsheetId")
            Logger.debug(TAG, "Range: $updateRange")
            Logger.debug(TAG, "Data: ${employee.name} | ${employee.email} | ${employee.calendarId} | ${employee.sheetName} | €${employee.supervisionPrice}")

            val result = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .update(spreadsheetId, updateRange, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute()
            }

            val updatedRange = result.updatedRange ?: updateRange
            Logger.info(TAG, "Successfully updated employee: $updatedRange")

            SheetsWriteResult.Success(
                sheetName = "Employees",
                updatedRange = updatedRange,
                rowsAdded = 0  // 0 because we're updating, not adding
            )

        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error updating employee in Sheets: ${e.statusCode}", e)

            val errorMessage = when (e.statusCode) {
                404 -> "Employees sheet not found in spreadsheet."
                403 -> "Permission denied. Check Google Sheets API access."
                401 -> "Authentication failed. Please re-authenticate."
                429 -> "Rate limit exceeded. Please try again later."
                else -> "Google API error (${e.statusCode}): ${e.statusMessage}"
            }

            SheetsWriteResult.Error(errorMessage)
        } catch (e: IOException) {
            Logger.error(TAG, "Network error updating employee in Google Sheets", e)
            SheetsWriteResult.Error("Network error: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Logger.error(TAG, "Invalid data for Google Sheets", e)
            SheetsWriteResult.Error("Invalid data: ${e.message}")
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error updating employee in Google Sheets: ${e::class.simpleName}", e)
            SheetsWriteResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Batch update multiple clients in a sheet tab at once
     * Much faster than individual updates - uses Google Sheets batchUpdate API
     *
     * @param clients List of clients to update
     * @param sheetName The tab name (from employee.sheetName)
     * @return Number of clients successfully updated
     */
    suspend fun batchUpdateClientsInSheet(
        clients: List<Client>,
        sheetName: String
    ): Int = withContext(Dispatchers.IO) {
        val service = sheetsService
        if (service == null || sheetName.isBlank() || clients.isEmpty()) {
            return@withContext 0
        }

        try {
            // First, read all existing clients to get their row numbers
            val range = "'$sheetName'!A2:D"
            val response = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            }

            val values = response.getValues() ?: return@withContext 0

            // Create a map of client name -> row number
            val clientRowMap = mutableMapOf<String, Int>()
            values.forEachIndexed { index, row ->
                if (row.isNotEmpty()) {
                    val name = row[0].toString().trim()
                    clientRowMap[name.lowercase()] = index + 2 // +2 for header and 0-based index
                }
            }

            // Build batch update data
            val batchData = mutableListOf<com.google.api.services.sheets.v4.model.ValueRange>()

            clients.forEach { client ->
                val rowNumber = clientRowMap[client.name.lowercase()]
                if (rowNumber != null) {
                    val updateRange = "'$sheetName'!A$rowNumber:D$rowNumber"
                    val rowData = listOf(
                        listOf(
                            client.name,
                            client.price,
                            client.employeePrice,
                            client.companyPrice
                        )
                    )
                    batchData.add(
                        com.google.api.services.sheets.v4.model.ValueRange()
                            .setRange(updateRange)
                            .setValues(rowData)
                    )
                }
            }

            if (batchData.isEmpty()) {
                return@withContext 0
            }

            Logger.info(TAG, "Batch updating ${batchData.size} clients in sheet: $sheetName")

            // Execute batch update
            val batchUpdateRequest = com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(batchData)

            val result = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute()
            }

            val updatedCells = result.totalUpdatedCells ?: 0
            Logger.info(TAG, "Batch update complete: ${batchData.size} clients, $updatedCells cells updated")

            batchData.size
        } catch (e: Exception) {
            Logger.error(TAG, "Batch update failed for sheet $sheetName", e)
            0
        }
    }

    /**
     * Verify that a sheet tab exists for an employee
     */
    suspend fun verifySheetExists(sheetName: String): Boolean = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext false

        try {
            val spreadsheet = RetryUtils.retryWithBackoff {
                service.spreadsheets()
                    .get(spreadsheetId)
                    .execute()
            }

            val sheetNames = spreadsheet.sheets?.map { it.properties?.title } ?: emptyList()

            Logger.info(TAG, "Available sheets: $sheetNames")

            sheetNames.contains(sheetName)
        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error checking sheet: ${e.statusCode}", e)
            false
        } catch (e: IOException) {
            Logger.error(TAG, "Network error checking sheet existence", e)
            false
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error checking sheet: ${e::class.simpleName}", e)
            false
        }
    }

    /**
     * Get list of all sheet tab names in the spreadsheet
     */
    suspend fun getSheetNames(): List<String> = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext emptyList()

        try {
            val spreadsheet = RetryUtils.retryWithBackoff {
                service.spreadsheets()
                    .get(spreadsheetId)
                    .execute()
            }

            spreadsheet.sheets?.mapNotNull { it.properties?.title } ?: emptyList()
        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error getting sheet names: ${e.statusCode}", e)
            emptyList()
        } catch (e: IOException) {
            Logger.error(TAG, "Network error getting sheet names", e)
            emptyList()
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error getting sheet names: ${e::class.simpleName}", e)
            emptyList()
        }
    }

    /**
     * Create a new sheet tab for an employee with headers
     * @param sheetName The name of the new sheet tab
     * @return true if successful, false otherwise
     */
    suspend fun createEmployeeSheet(sheetName: String): Boolean = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext false

        if (sheetName.isBlank()) {
            Logger.error(TAG, "Cannot create sheet with blank name")
            return@withContext false
        }

        try {
            Logger.info(TAG, "Creating new sheet tab: $sheetName")

            // First, check if sheet already exists
            val existingSheets = getSheetNames()
            if (existingSheets.contains(sheetName)) {
                Logger.warning(TAG, "Sheet tab '$sheetName' already exists")
                return@withContext true  // Consider it success since it exists
            }

            // Create AddSheetRequest
            val addSheetRequest = com.google.api.services.sheets.v4.model.Request()
                .setAddSheet(
                    com.google.api.services.sheets.v4.model.AddSheetRequest()
                        .setProperties(
                            com.google.api.services.sheets.v4.model.SheetProperties()
                                .setTitle(sheetName)
                        )
                )

            val batchUpdateRequest = com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
                .setRequests(listOf(addSheetRequest))

            // Execute the request to create the sheet
            RetryUtils.retryWithBackoff {
                service.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute()
            }

            Logger.info(TAG, "Created sheet tab: $sheetName")

            // Now add headers to the new sheet
            val headers = listOf(
                listOf("Client Name", "Price (€)", "Employee Price (€)", "Company Price (€)")
            )

            val headerRange = ValueRange()
                .setValues(headers)

            val headerRangeNotation = "'$sheetName'!A1:D1"

            RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .update(spreadsheetId, headerRangeNotation, headerRange)
                    .setValueInputOption("RAW")
                    .execute()
            }

            Logger.info(TAG, "Added headers to sheet: $sheetName")

            true
        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error creating sheet '$sheetName': ${e.statusCode}", e)
            false
        } catch (e: IOException) {
            Logger.error(TAG, "Network error creating sheet tab", e)
            false
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error creating sheet '$sheetName': ${e::class.simpleName}", e)
            false
        }
    }

    /**
     * Read all employees from the "Employees" sheet
     * Expected columns: Name | Email | Calendar ID | Client Sheet Name | Supervision (€) | Clients
     */
    suspend fun readEmployees(): SheetsReadResult<List<EmployeeSheetData>> = withContext(Dispatchers.IO) {
        val service = sheetsService
        if (service == null) {
            return@withContext SheetsReadResult.Error("Google Sheets service not available")
        }

        try {
            Logger.info(TAG, "Reading employees from Google Sheets...")
            Logger.debug(TAG, "Spreadsheet ID: $spreadsheetId")

            // First, verify this is a valid Sheets document
            try {
                val metadata = RetryUtils.retryWithBackoff {
                    service.spreadsheets()
                        .get(spreadsheetId)
                        .setFields("spreadsheetId,properties(title)")
                        .execute()
                }
                Logger.info(TAG, "Spreadsheet found: ${metadata.properties?.title}")
            } catch (e: GoogleJsonResponseException) {
                Logger.error(TAG, "Failed to access spreadsheet", e)
                if (e.statusCode == 404) {
                    return@withContext SheetsReadResult.Error(
                        "Spreadsheet not found. Please verify spreadsheetId: $spreadsheetId"
                    )
                }
                throw e
            } catch (e: IOException) {
                Logger.error(TAG, "Network error accessing spreadsheet", e)
                return@withContext SheetsReadResult.Error("Network error: ${e.message}")
            } catch (e: Exception) {
                Logger.error(TAG, "Error accessing spreadsheet: ${e::class.simpleName}", e)
                if (e.message?.contains("This operation is not supported") == true) {
                    return@withContext SheetsReadResult.Error(
                        "This appears to be a Google Drive Excel file, not a native Google Sheet. " +
                        "Please convert to Google Sheets format or use the correct spreadsheet ID."
                    )
                }
                throw e
            }

            // Read from "Employees" sheet, columns A-F
            val range = "Employees!A2:F"  // Skip header row

            val response = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            }

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                Logger.warning(TAG, "No employee data found in sheet")
                return@withContext SheetsReadResult.Success(emptyList())
            }

            val employees = values.mapNotNull { row ->
                try {
                    // Skip rows that don't have at least name and email
                    if (row.size < 2 || row[0].toString().isBlank()) {
                        return@mapNotNull null
                    }

                    EmployeeSheetData(
                        name = row.getOrNull(0)?.toString()?.trim() ?: "",
                        email = row.getOrNull(1)?.toString()?.trim() ?: "",
                        calendarId = row.getOrNull(2)?.toString()?.trim() ?: "",
                        sheetName = row.getOrNull(3)?.toString()?.trim() ?: "",
                        supervisionPrice = row.getOrNull(4)?.toString()?.toDoubleOrNull() ?: 0.0
                    )
                } catch (e: Exception) {
                    Logger.warning(TAG, "Error parsing employee row: ${e.message}")
                    null
                }
            }

            Logger.info(TAG, "Read ${employees.size} employees from sheet")
            SheetsReadResult.Success(employees)

        } catch (e: GoogleJsonResponseException) {
            Logger.error(TAG, "Google API error reading employees: ${e.statusCode}", e)
            SheetsReadResult.Error("Google API error (${e.statusCode}): ${e.statusMessage}")
        } catch (e: IOException) {
            Logger.error(TAG, "Network error reading employees", e)
            SheetsReadResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error reading employees: ${e::class.simpleName}", e)
            SheetsReadResult.Error("Failed to read employees: ${e.message}")
        }
    }

    /**
     * Read all clients for a specific employee from their sheet tab
     * Expected columns: Client Name | Price (€) | Employee Price (€) | Company Price (€)
     */
    suspend fun readClientsForEmployee(
        sheetName: String,
        employeeId: String
    ): SheetsReadResult<List<ClientSheetData>> = withContext(Dispatchers.IO) {
        val service = sheetsService
        if (service == null) {
            return@withContext SheetsReadResult.Error("Google Sheets service not available")
        }

        if (sheetName.isBlank()) {
            return@withContext SheetsReadResult.Success(emptyList())
        }

        try {
            Logger.info(TAG, "Reading clients from sheet tab: $sheetName")

            // Read from employee's sheet tab, columns A-D
            val range = "'$sheetName'!A2:D"  // Skip header row

            val response = RetryUtils.retryWithBackoff {
                service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()
            }

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                Logger.warning(TAG, "No client data found in sheet tab: $sheetName")
                return@withContext SheetsReadResult.Success(emptyList())
            }

            val clients = values.mapNotNull { row ->
                try {
                    // Skip rows that don't have at least a name
                    if (row.isEmpty() || row[0].toString().isBlank()) {
                        return@mapNotNull null
                    }

                    val name = row.getOrNull(0)?.toString()?.trim() ?: ""
                    // Replace comma with dot for European number format (e.g., "22,5" -> "22.5")
                    val price = row.getOrNull(1)?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                    val employeePrice = row.getOrNull(2)?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                    val companyPrice = row.getOrNull(3)?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                    ClientSheetData(
                        name = name,
                        price = price,
                        employeePrice = employeePrice,
                        companyPrice = companyPrice,
                        employeeId = employeeId
                    )
                } catch (e: Exception) {
                    Logger.warning(TAG, "Error parsing client row: ${e.message}")
                    null
                }
            }

            Logger.info(TAG, "Read ${clients.size} clients from sheet tab: $sheetName")
            SheetsReadResult.Success(clients)

        } catch (e: GoogleJsonResponseException) {
            // If sheet tab doesn't exist (404), that's okay - employee just has no clients yet
            if (e.statusCode == 404) {
                Logger.info(TAG, "Sheet tab '$sheetName' not found (employee has no clients yet)")
                return@withContext SheetsReadResult.Success(emptyList())
            }

            Logger.error(TAG, "Google API error reading clients from $sheetName: ${e.statusCode}", e)
            SheetsReadResult.Error("Google API error (${e.statusCode}): ${e.statusMessage}")
        } catch (e: IOException) {
            Logger.error(TAG, "Network error reading clients from $sheetName", e)
            SheetsReadResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error reading clients from $sheetName: ${e::class.simpleName}", e)
            SheetsReadResult.Error("Failed to read clients: ${e.message}")
        }
    }
}

/**
 * Data classes for reading from sheets
 */
data class EmployeeSheetData(
    val name: String,
    val email: String,
    val calendarId: String,
    val sheetName: String,
    val supervisionPrice: Double
)

data class ClientSheetData(
    val name: String,
    val price: Double,
    val employeePrice: Double,
    val companyPrice: Double,
    val employeeId: String
)

/**
 * Result of a sheets read operation
 */
sealed class SheetsReadResult<out T> {
    data class Success<T>(val data: T) : SheetsReadResult<T>()
    data class Error(val message: String) : SheetsReadResult<Nothing>()
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
