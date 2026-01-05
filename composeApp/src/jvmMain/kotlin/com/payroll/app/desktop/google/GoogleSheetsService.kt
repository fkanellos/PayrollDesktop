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
        // Main payroll spreadsheet ID (Google Sheets format)
        // TODO: This should be configurable, not hardcoded
        private const val SPREADSHEET_ID = "1r_fY7YF3ZWnhDkGq84Cxub6Ok2Dnfm0iqPTB8lzHZRU"

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

    /**
     * Create a new sheet tab for an employee with headers
     * @param sheetName The name of the new sheet tab
     * @return true if successful, false otherwise
     */
    suspend fun createEmployeeSheet(sheetName: String): Boolean = withContext(Dispatchers.IO) {
        val service = sheetsService ?: return@withContext false

        if (sheetName.isBlank()) {
            println("❌ Cannot create sheet with blank name")
            return@withContext false
        }

        try {
            println("📝 Creating new sheet tab: $sheetName")

            // First, check if sheet already exists
            val existingSheets = getSheetNames()
            if (existingSheets.contains(sheetName)) {
                println("⚠️ Sheet tab '$sheetName' already exists")
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
            service.spreadsheets()
                .batchUpdate(SPREADSHEET_ID, batchUpdateRequest)
                .execute()

            println("✅ Created sheet tab: $sheetName")

            // Now add headers to the new sheet
            val headers = listOf(
                listOf("Client Name", "Price (€)", "Employee Price (€)", "Company Price (€)")
            )

            val headerRange = ValueRange()
                .setValues(headers)

            val headerRangeNotation = "'$sheetName'!A1:D1"

            service.spreadsheets().values()
                .update(SPREADSHEET_ID, headerRangeNotation, headerRange)
                .setValueInputOption("RAW")
                .execute()

            println("✅ Added headers to sheet: $sheetName")

            true
        } catch (e: Exception) {
            println("❌ Error creating sheet tab '$sheetName': ${e.message}")
            e.printStackTrace()
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
            println("📖 Reading employees from Google Sheets...")
            println("   Spreadsheet ID: $SPREADSHEET_ID")

            // First, verify this is a valid Sheets document
            try {
                val metadata = service.spreadsheets()
                    .get(SPREADSHEET_ID)
                    .setFields("spreadsheetId,properties(title)")
                    .execute()
                println("   ✓ Spreadsheet found: ${metadata.properties?.title}")
            } catch (e: Exception) {
                println("❌ Failed to access spreadsheet: ${e.message}")
                if (e.message?.contains("404") == true) {
                    return@withContext SheetsReadResult.Error(
                        "Spreadsheet not found. Please verify SPREADSHEET_ID: $SPREADSHEET_ID"
                    )
                } else if (e.message?.contains("This operation is not supported") == true) {
                    return@withContext SheetsReadResult.Error(
                        "This appears to be a Google Drive Excel file, not a native Google Sheet. " +
                        "Please convert to Google Sheets format or use the correct spreadsheet ID."
                    )
                }
                throw e
            }

            // Read from "Employees" sheet, columns A-F
            val range = "Employees!A2:F"  // Skip header row

            val response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute()

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                println("⚠️ No employee data found in sheet")
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
                    println("⚠️ Error parsing employee row: ${e.message}")
                    null
                }
            }

            println("✅ Read ${employees.size} employees from sheet")
            SheetsReadResult.Success(employees)

        } catch (e: Exception) {
            println("❌ Error reading employees: ${e.message}")
            e.printStackTrace()
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
            println("📖 Reading clients from sheet tab: $sheetName")

            // Read from employee's sheet tab, columns A-D
            val range = "'$sheetName'!A2:D"  // Skip header row

            val response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute()

            val values = response.getValues()

            if (values.isNullOrEmpty()) {
                println("⚠️ No client data found in sheet tab: $sheetName")
                return@withContext SheetsReadResult.Success(emptyList())
            }

            val clients = values.mapNotNull { row ->
                try {
                    // Skip rows that don't have at least a name
                    if (row.isEmpty() || row[0].toString().isBlank()) {
                        return@mapNotNull null
                    }

                    ClientSheetData(
                        name = row.getOrNull(0)?.toString()?.trim() ?: "",
                        price = row.getOrNull(1)?.toString()?.toDoubleOrNull() ?: 0.0,
                        employeePrice = row.getOrNull(2)?.toString()?.toDoubleOrNull() ?: 0.0,
                        companyPrice = row.getOrNull(3)?.toString()?.toDoubleOrNull() ?: 0.0,
                        employeeId = employeeId
                    )
                } catch (e: Exception) {
                    println("⚠️ Error parsing client row: ${e.message}")
                    null
                }
            }

            println("✅ Read ${clients.size} clients from sheet tab: $sheetName")
            SheetsReadResult.Success(clients)

        } catch (e: Exception) {
            // If sheet tab doesn't exist, that's okay - employee just has no clients yet
            if (e.message?.contains("Unable to parse range") == true ||
                e.message?.contains("not found") == true) {
                println("ℹ️ Sheet tab '$sheetName' not found (employee has no clients yet)")
                return@withContext SheetsReadResult.Success(emptyList())
            }

            println("❌ Error reading clients from $sheetName: ${e.message}")
            e.printStackTrace()
            SheetsReadResult.Error("Failed to read clients from $sheetName: ${e.message}")
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
