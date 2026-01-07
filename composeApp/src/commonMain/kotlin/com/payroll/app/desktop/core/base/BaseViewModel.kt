package com.payroll.app.desktop.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel implementing MVI pattern with generics
 * @param State - The UI state type
 * @param Action - The user action type
 * @param Effect - The side effect type (navigation, toasts, etc)
 *
 * Features:
 * - Lifecycle-aware viewModelScope that auto-cancels on clear
 * - Thread-safe state management with StateFlow
 * - Structured concurrency with SupervisorJob
 */
abstract class BaseViewModel<State : UiState, Action : UiAction, Effect : UiEffect> {

    abstract val initialState: State

    protected val _uiState: MutableStateFlow<State> by lazy { MutableStateFlow(initialState) }
    val uiState: StateFlow<State> by lazy { _uiState.asStateFlow() }

    /**
     * Lifecycle-aware coroutine scope for this ViewModel
     * Automatically cancelled when onCleared() is called
     */
    protected val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Handle user actions - delegates to reduce for state updates
     */
    fun handleAction(action: Action) {
        val newState = reduce(currentState = _uiState.value, action = action)
        _uiState.value = newState
    }

    /**
     * Update state with a transform function
     */
    protected fun updateState(transform: (State) -> State) {
        _uiState.value = transform(_uiState.value)
    }

    /**
     * Reduce function to handle state changes
     * Pure function that takes current state + action and returns new state
     */
    protected abstract fun reduce(currentState: State, action: Action): State

    /**
     * Called when this ViewModel is cleared
     * Cancels all coroutines launched in viewModelScope
     * Override to add custom cleanup logic
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}