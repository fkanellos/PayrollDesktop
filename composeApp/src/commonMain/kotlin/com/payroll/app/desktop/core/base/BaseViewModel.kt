package com.payroll.app.desktop.core.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel implementing MVI pattern with generics
 * @param State - The UI state type
 * @param Action - The user action type
 * @param Effect - The side effect type (navigation, toasts, etc)
 */
abstract class BaseViewModel<State : UiState, Action : UiAction, Effect : UiEffect> {

    abstract val initialState: State

    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effect = MutableStateFlow<Effect?>(null)
    val effect: StateFlow<Effect?> = _effect.asStateFlow()

    /**
     * Handle user actions and update state
     */
    fun handleAction(action: Action) {
        val newState = reduce(currentState = _uiState.value, action = action)
        _uiState.value = newState
    }

    /**
     * Update state directly (for internal use)
     */
    protected fun updateState(newState: State) {
        _uiState.value = newState
    }

    /**
     * Update state with a transform function
     */
    protected fun updateState(transform: (State) -> State) {
        _uiState.value = transform(_uiState.value)
    }

    /**
     * Emit side effects (navigation, toasts, etc)
     */
    protected fun emitEffect(effect: Effect) {
        _effect.value = effect
    }

    /**
     * Clear the current effect
     */
    fun clearEffect() {
        _effect.value = null
    }

    /**
     * Reduce function to handle state changes
     * Pure function that takes current state + action and returns new state
     */
    protected abstract fun reduce(currentState: State, action: Action): State

    /**
     * Handle side effects (override if needed)
     */
    protected open fun handleSideEffect(effect: Effect) {
        // Override in child classes for custom side effect handling
    }

    /**
     * Cleanup resources when ViewModel is cleared
     * Override in child classes for custom cleanup
     */
    open fun onCleared() {
        // Override in child classes for cleanup
    }
}