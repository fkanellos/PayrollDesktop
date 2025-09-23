package com.payroll.app.desktop.core.base

/**
 * Marker interface for all UI states
 * All ViewModels states must implement this
 */
interface UiState

/**
 * Marker interface for all user actions
 * All user interactions must implement this
 */
interface UiAction

/**
 * Marker interface for all side effects
 * One-time events like navigation, toasts, etc.
 */
interface UiEffect

/**
 * Common UI states that can be mixed into any screen state
 */
interface LoadingState : UiState {
    val isLoading: Boolean
}

interface ErrorState : UiState {
    val error: String?
}

/**
 * Base state for screens with loading and error capabilities
 */
abstract class BaseUiState : UiState, LoadingState, ErrorState {
    abstract override val isLoading: Boolean
    abstract override val error: String?
}

/**
 * Common effects for navigation and user feedback
 */
sealed class CommonEffect : UiEffect {
    data class ShowToast(val message: String) : CommonEffect()
    data class ShowError(val error: String) : CommonEffect()
    data class Navigate(val route: String) : CommonEffect()
    object NavigateBack : CommonEffect()
}