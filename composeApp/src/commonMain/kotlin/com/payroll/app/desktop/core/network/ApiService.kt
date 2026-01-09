package com.payroll.app.desktop.core.network

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
class PayrollApiService(
    baseUrl: String = "http://localhost:8080"
) {

    private val baseUrl = baseUrl

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        // Timeout configuration to prevent hanging requests
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000  // 30 seconds for entire request
            connectTimeoutMillis = 10_000  // 10 seconds to establish connection
            socketTimeoutMillis = 30_000   // 30 seconds between packets
        }
    }

    /**
     * Helper function to handle API calls with proper exception handling
     * Differentiates between client errors (4xx), server errors (5xx),
     * serialization errors, and timeout errors
     */
    private suspend inline fun <T> safeApiCall(
        crossinline apiCall: suspend () -> T
    ): RepositoryResult<T> {
        return try {
            val result = apiCall()
            RepositoryResult.Success(result)
        } catch (e: ClientRequestException) {
            // 4xx errors (client-side errors like 404, 400, 401, etc.)
            RepositoryResult.Error(
                Exception("Client error [${e.response.status.value}]: ${e.message}")
            )
        } catch (e: ServerResponseException) {
            // 5xx errors (server-side errors like 500, 503, etc.)
            RepositoryResult.Error(
                Exception("Server error [${e.response.status.value}]: ${e.message}")
            )
        } catch (e: RedirectResponseException) {
            // 3xx errors (redirects)
            RepositoryResult.Error(
                Exception("Redirect error [${e.response.status.value}]: ${e.message}")
            )
        } catch (e: SerializationException) {
            // JSON parsing errors
            RepositoryResult.Error(
                Exception("Failed to parse response: ${e.message}")
            )
        } catch (e: HttpRequestTimeoutException) {
            // Timeout errors
            RepositoryResult.Error(
                Exception("Request timeout: ${e.message}")
            )
        } catch (e: Exception) {
            // Generic fallback for unexpected errors
            RepositoryResult.Error(e)
        }
    }

    /**
     * Get all employees
     */
    suspend fun getEmployees(): RepositoryResult<List<Employee>> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/employees").body()
        }
    }

    /**
     * Get employee by ID
     */
    suspend fun getEmployee(id: String): RepositoryResult<Employee> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/employees/$id").body()
        }
    }

    /**
     * Get clients for an employee
     * 🔧 FIXED: Backend returns wrapper { employeeId, clients }
     */
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return safeApiCall {
            val response: EmployeeClientsResponse = httpClient.get("$baseUrl/api/clients/employee/$employeeId").body()
            response.clients
        }
    }

    /**
     * Get all clients
     */
    suspend fun getAllClients(): RepositoryResult<List<Client>> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/clients").body()
        }
    }

    /**
     * Create a new client
     */
    suspend fun createClient(client: Client): RepositoryResult<Client> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/clients") {
                contentType(ContentType.Application.Json)
                setBody(client)
            }.body()
        }
    }

    /**
     * Update an existing client
     */
    suspend fun updateClient(id: Long, client: Client): RepositoryResult<Client> {
        return safeApiCall {
            httpClient.put("$baseUrl/api/clients/$id") {
                contentType(ContentType.Application.Json)
                setBody(client)
            }.body()
        }
    }

    /**
     * Delete a client
     */
    suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return safeApiCall {
            httpClient.delete("$baseUrl/api/clients/$id")
            true
        }
    }

    /**
     * Calculate payroll
     * 🔴 UPDATED: Backend now returns wrapper with ID
     */
    suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return safeApiCall {
            // Backend returns: { id: "...", payroll: {...} }
            val response: PayrollCalculationResponse = httpClient.post("$baseUrl/payroll/calculate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            // Extract payroll with ID already set
            response.payroll.copy(id = response.id)
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
        return safeApiCall {
            httpClient.get("$baseUrl/api/calendar/events/$employeeId") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }.body()
        }
    }

    /**
     * Test backend connection
     */
    suspend fun testConnection(): RepositoryResult<String> {
        return safeApiCall {
            httpClient.get("$baseUrl/hello").body()
        }
    }

    /**
     * Download PDF for a payroll calculation
     */
    suspend fun downloadPdf(payrollId: String): RepositoryResult<ByteArray> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/export/payroll/$payrollId/pdf").body()
        }
    }

    /**
     * 🆕 Check if payroll exists in Google Sheets
     */
    suspend fun checkPayrollInSheets(payrollId: String): RepositoryResult<CheckSheetsResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/payroll/$payrollId/check-sheets").body()
        }
    }

    /**
     * 🆕 Sync payroll to Google Sheets
     */
    suspend fun syncPayrollToSheets(payrollId: String): RepositoryResult<SyncSheetsResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/payroll/$payrollId/sync-to-sheets").body()
        }
    }

    // ==================== EMPLOYEE CRUD ====================

    /**
     * Create a new employee
     */
    suspend fun createEmployee(employee: Employee): RepositoryResult<Employee> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/employees") {
                contentType(ContentType.Application.Json)
                setBody(employee)
            }.body()
        }
    }

    /**
     * Update an existing employee
     */
    suspend fun updateEmployee(id: String, employee: Employee): RepositoryResult<Employee> {
        return safeApiCall {
            httpClient.put("$baseUrl/api/employees/$id") {
                contentType(ContentType.Application.Json)
                setBody(employee)
            }.body()
        }
    }

    /**
     * Delete an employee
     */
    suspend fun deleteEmployee(id: String): RepositoryResult<Boolean> {
        return safeApiCall {
            httpClient.delete("$baseUrl/api/employees/$id")
            true
        }
    }

    // ==================== DATABASE SYNC ====================

    /**
     * Sync database from Excel file in Google Drive
     */
    suspend fun syncDatabase(): RepositoryResult<SyncDatabaseResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/db/sync").body()
        }
    }

    /**
     * Get database sync statistics
     */
    suspend fun getDatabaseStats(): RepositoryResult<DatabaseStatsResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/db/stats").body()
        }
    }

    fun close() {
        httpClient.close()
    }
}