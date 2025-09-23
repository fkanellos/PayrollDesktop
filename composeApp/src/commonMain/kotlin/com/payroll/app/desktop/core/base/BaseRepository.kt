package com.payroll.app.desktop.core.base

/**
 * Generic repository interface for CRUD operations
 * @param T - Entity type
 * @param ID - Primary key type
 */
interface BaseRepository<T, ID> {
    suspend fun getAll(): Result<List<T>>
    suspend fun getById(id: ID): Result<T?>
    suspend fun create(item: T): Result<T>
    suspend fun update(id: ID, item: T): Result<T>
    suspend fun delete(id: ID): Result<Boolean>
}

/**
 * Result wrapper for repository operations
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Exception) : RepositoryResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): RepositoryResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    inline fun onSuccess(action: (T) -> Unit): RepositoryResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Exception) -> Unit): RepositoryResult<T> {
        if (this is Error) action(exception)
        return this
    }
}

/**
 * Extension function to convert Kotlin Result to RepositoryResult
 */
fun <T> Result<T>.toRepositoryResult(): RepositoryResult<T> {
    return fold(
        onSuccess = { RepositoryResult.Success(it) },
        onFailure = { RepositoryResult.Error(it as Exception) }
    )
}