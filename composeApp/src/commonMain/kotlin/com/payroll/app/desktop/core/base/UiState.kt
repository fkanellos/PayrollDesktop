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