package com.payroll.app.desktop.core.network

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API Service for communicating with Spring Boot backend
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
     */
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val response: List<Client> = httpClient.get("$baseUrl/api/employees/$employeeId/clients").body()
            RepositoryResult.Success(response)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    /**
     * Calculate payroll
     */
    suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return try {
            val response: PayrollResponse = httpClient.post("$baseUrl/payroll/calculate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            RepositoryResult.Success(response)
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

    fun close() {
        httpClient.close()
    }
}