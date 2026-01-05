package com.payroll.app.desktop.core.network

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PayrollCalculationResponse(
    val id: String,
    val payroll: PayrollResponse
)

/**
 * Wrapper for employee clients response
 * Backend returns: { "employeeId": "...", "clients": [...] }
 */
@Serializable
data class EmployeeClientsResponse(
    val employeeId: String,
    val clients: List<Client>
)

/**
 * API Service for communicating with Spring Boot backend
 * 🔴 UPDATED: Removed PayrollCalculationResponse wrapper
 */
class PayrollApiService {

    private val baseUrl = "http://localhost:8080"

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Get all employees
     */
    suspend fun getEmployees(): RepositoryResult<List<Employee>> {
        return try {
            val response: List<Employee> = httpClient.get("$baseUrl/api/employees").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get employee by ID
     */
    suspend fun getEmployee(id: String): RepositoryResult<Employee> {
        return try {
            val response: Employee = httpClient.get("$baseUrl/api/employees/$id").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get clients for an employee
     * 🔧 FIXED: Backend returns wrapper { employeeId, clients }
     */
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val response: EmployeeClientsResponse = httpClient.get("$baseUrl/api/clients/employee/$employeeId").body()
            RepositoryResult.Success(response.clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get all clients
     */
    suspend fun getAllClients(): RepositoryResult<List<Client>> {
        return try {
            val response: List<Client> = httpClient.get("$baseUrl/api/clients").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Create a new client
     */
    suspend fun createClient(client: Client): RepositoryResult<Client> {
        return try {
            val response: Client = httpClient.post("$baseUrl/api/clients") {
                contentType(ContentType.Application.Json)
                setBody(client)
            }.body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Update an existing client
     */
    suspend fun updateClient(id: Long, client: Client): RepositoryResult<Client> {
        return try {
            val response: Client = httpClient.put("$baseUrl/api/clients/$id") {
                contentType(ContentType.Application.Json)
                setBody(client)
            }.body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Delete a client
     */
    suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return try {
            httpClient.delete("$baseUrl/api/clients/$id")
            RepositoryResult.Success(true)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Calculate payroll
     * 🔴 UPDATED: Backend now returns wrapper with ID
     */
    suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return try {
            // Backend returns: { id: "...", payroll: {...} }
            val response: PayrollCalculationResponse = httpClient.post("$baseUrl/payroll/calculate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            // Extract payroll with ID already set
            val payrollWithId = response.payroll.copy(id = response.id)

            RepositoryResult.Success(payrollWithId)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get calendar events for employee
     */
    suspend fun getCalendarEvents(
        employeeId: String,
        startDate: String,
        endDate: String
    ): RepositoryResult<Map<String, Any>> {
        return try {
            val response: Map<String, Any> = httpClient.get("$baseUrl/api/calendar/events/$employeeId") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }.body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Test backend connection
     */
    suspend fun testConnection(): RepositoryResult<String> {
        return try {
            val response: String = httpClient.get("$baseUrl/hello").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Download PDF for a payroll calculation
     */
    suspend fun downloadPdf(payrollId: String): RepositoryResult<ByteArray> {
        return try {
            val response: ByteArray = httpClient.get("$baseUrl/api/export/payroll/$payrollId/pdf").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * 🆕 Check if payroll exists in Google Sheets
     */
    suspend fun checkPayrollInSheets(payrollId: String): RepositoryResult<CheckSheetsResponse> {
        return try {
            val response: CheckSheetsResponse = httpClient.get("$baseUrl/payroll/$payrollId/check-sheets").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * 🆕 Sync payroll to Google Sheets
     */
    suspend fun syncPayrollToSheets(payrollId: String): RepositoryResult<SyncSheetsResponse> {
        return try {
            val response: SyncSheetsResponse = httpClient.post("$baseUrl/payroll/$payrollId/sync-to-sheets").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    // ==================== EMPLOYEE CRUD ====================

    /**
     * Create a new employee
     */
    suspend fun createEmployee(employee: Employee): RepositoryResult<Employee> {
        return try {
            val response: Employee = httpClient.post("$baseUrl/api/employees") {
                contentType(ContentType.Application.Json)
                setBody(employee)
            }.body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Update an existing employee
     */
    suspend fun updateEmployee(id: String, employee: Employee): RepositoryResult<Employee> {
        return try {
            val response: Employee = httpClient.put("$baseUrl/api/employees/$id") {
                contentType(ContentType.Application.Json)
                setBody(employee)
            }.body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Delete an employee
     */
    suspend fun deleteEmployee(id: String): RepositoryResult<Boolean> {
        return try {
            httpClient.delete("$baseUrl/api/employees/$id")
            RepositoryResult.Success(true)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    // ==================== DATABASE SYNC ====================

    /**
     * Sync database from Excel file in Google Drive
     */
    suspend fun syncDatabase(): RepositoryResult<SyncDatabaseResponse> {
        return try {
            val response: SyncDatabaseResponse = httpClient.post("$baseUrl/api/db/sync").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get database sync statistics
     */
    suspend fun getDatabaseStats(): RepositoryResult<DatabaseStatsResponse> {
        return try {
            val response: DatabaseStatsResponse = httpClient.get("$baseUrl/api/db/stats").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}