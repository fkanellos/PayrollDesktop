package com.payroll.app.desktop.core.base

/**
 * Result wrapper for repository operations
 * Used consistently across all data layer operations
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Exception) : RepositoryResult<Nothing>()

    /**
     * Transform the success value
     */
    inline fun <R> map(transform: (T) -> R): RepositoryResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    /**
     * Execute action on success
     */
    inline fun onSuccess(action: (T) -> Unit): RepositoryResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute action on error
     */
    inline fun onError(action: (Exception) -> Unit): RepositoryResult<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Get the value or null if error
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Get the value or throw the exception
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    /**
     * Get the value or a default value
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }

    /**
     * Check if the result is successful
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Check if the result is an error
     */
    val isError: Boolean get() = this is Error
}