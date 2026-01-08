package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.google.GoogleSheetsService
import com.payroll.app.desktop.google.SheetsReadResult

/**
 * JVM implementation of ClientRepository using local database and Google Sheets
 */
actual class ClientRepository(
    private val localClientRepo: LocalClientRepository,
    private val googleSheetsService: GoogleSheetsService
) {
    actual suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getByEmployeeId(employeeId)
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getAll(): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getAll()
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun createClient(client: Client): RepositoryResult<Client> {
        return try {
            val clientId = localClientRepo.insert(client)
            val createdClient = client.copy(id = clientId)
            RepositoryResult.Success(createdClient)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun updateClient(client: Client): RepositoryResult<Client> {
        return try {
            localClientRepo.update(client)
            RepositoryResult.Success(client)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return try {
            localClientRepo.delete(id)
            RepositoryResult.Success(true)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun syncFromSheets(employeeId: String, sheetName: String): RepositoryResult<SyncResult> {
        return try {
            if (sheetName.isBlank()) {
                return RepositoryResult.Error(Exception("Sheet name is empty"))
            }

            // Read clients from Google Sheets
            when (val sheetsResult = googleSheetsService.readClientsForEmployee(sheetName, employeeId)) {
                is SheetsReadResult.Success -> {
                    val sheetsClients = sheetsResult.data
                    val localClients = localClientRepo.getByEmployeeId(employeeId)

                    var createdCount = 0
                    var updatedCount = 0
                    var unchangedCount = 0

                    // Compare and sync clients
                    sheetsClients.forEach { sheetClient ->
                        // Find matching local client by name
                        val localClient = localClients.find {
                            it.name.equals(sheetClient.name, ignoreCase = true)
                        }

                        if (localClient != null) {
                            // Client exists - check if prices are different
                            val pricesChanged =
                                localClient.price != sheetClient.price ||
                                localClient.employeePrice != sheetClient.employeePrice ||
                                localClient.companyPrice != sheetClient.companyPrice

                            if (pricesChanged) {
                                // Update the client with new prices from sheets
                                val updatedClient = localClient.copy(
                                    price = sheetClient.price,
                                    employeePrice = sheetClient.employeePrice,
                                    companyPrice = sheetClient.companyPrice
                                )
                                localClientRepo.update(updatedClient)
                                updatedCount++
                            } else {
                                unchangedCount++
                            }
                        } else {
                            // Client doesn't exist locally - create it
                            val newClient = Client(
                                id = 0, // Will be auto-generated
                                name = sheetClient.name,
                                price = sheetClient.price,
                                employeePrice = sheetClient.employeePrice,
                                companyPrice = sheetClient.companyPrice,
                                employeeId = employeeId,
                                pendingPayment = false
                            )
                            localClientRepo.insert(newClient)
                            createdCount++
                        }
                    }

                    RepositoryResult.Success(SyncResult(createdCount, updatedCount, unchangedCount))
                }
                is SheetsReadResult.Error -> {
                    RepositoryResult.Error(Exception("Google Sheets error: ${sheetsResult.message}"))
                }
            }
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }
}
