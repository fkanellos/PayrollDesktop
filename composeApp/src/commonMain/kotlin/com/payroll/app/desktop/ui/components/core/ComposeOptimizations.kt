package com.payroll.app.desktop.ui.components.core

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Composable optimization utilities
 * Use these to prevent unnecessary recompositions
 */

/**
 * Remember a callback that won't cause recomposition when lambda changes
 * Use for callbacks passed to child composables
 */
@Composable
fun <T> rememberCallback(
    callback: (T) -> Unit
): (T) -> Unit {
    val currentCallback by rememberUpdatedState(callback)
    return remember { { value: T -> currentCallback(value) } }
}

/**
 * Remember a no-arg callback that won't cause recomposition
 */
@Composable
fun rememberCallback(
    callback: () -> Unit
): () -> Unit {
    val currentCallback by rememberUpdatedState(callback)
    return remember { { currentCallback() } }
}

/**
 * Derive a value from state only when it actually changes
 * Use for computed values from state
 */
@Composable
fun <T, R> StateFlow<T>.deriveValue(
    calculation: (T) -> R
): State<R> {
    val state by this.collectAsState()
    return remember { derivedStateOf { calculation(state) } }
}

/**
 * Collect flow as state with a remembered initial value
 * Prevents recomposition on flow emission if value hasn't changed
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initial: T
): State<T> {
    return this.collectAsState(initial)
}

/**
 * Remember derived value that only recalculates when keys change
 */
@Composable
inline fun <T> rememberDerived(
    vararg keys: Any?,
    crossinline calculation: () -> T
): T {
    return remember(*keys) { derivedStateOf { calculation() } }.value
}

/**
 * Use this for expensive calculations that should be memoized
 */
@Composable
inline fun <T> rememberMemoized(
    key1: Any?,
    crossinline calculation: () -> T
): T = remember(key1) { calculation() }

@Composable
inline fun <T> rememberMemoized(
    key1: Any?,
    key2: Any?,
    crossinline calculation: () -> T
): T = remember(key1, key2) { calculation() }

@Composable
inline fun <T> rememberMemoized(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    crossinline calculation: () -> T
): T = remember(key1, key2, key3) { calculation() }

/**
 * Stable wrapper for lambda that prevents recomposition
 * Use when passing lambdas to composables
 */
@Stable
class StableCallback<T>(private val callback: (T) -> Unit) {
    operator fun invoke(value: T) = callback(value)
}

/**
 * Stable wrapper for a value
 */
@Stable
data class StableValue<T>(val value: T)

/**
 * Create a stable callback from a lambda
 */
@Composable
fun <T> stableCallback(callback: (T) -> Unit): StableCallback<T> {
    return remember { StableCallback(callback) }
}

/**
 * Create a stable value
 */
@Composable
fun <T> stableValueOf(value: T): StableValue<T> {
    return remember(value) { StableValue(value) }
}

/**
 * Skip recomposition if condition is true
 * Useful for conditional UI that shouldn't recompose often
 */
@Composable
inline fun RecomposeGuard(
    shouldSkip: Boolean,
    crossinline content: @Composable () -> Unit
) {
    if (!shouldSkip) {
        content()
    }
}

/**
 * Immutable list wrapper for stable parameters
 * Lists are not considered stable by Compose
 */
@Immutable
data class ImmutableList<T>(private val list: List<T>) : List<T> by list {
    companion object {
        fun <T> of(vararg items: T): ImmutableList<T> = ImmutableList(items.toList())
        fun <T> from(list: List<T>): ImmutableList<T> = ImmutableList(list.toList())
    }
}

/**
 * Extension to convert list to immutable
 */
fun <T> List<T>.toImmutable(): ImmutableList<T> = ImmutableList.from(this)

/**
 * Remember a list as immutable to prevent recomposition
 */
@Composable
fun <T> rememberImmutableList(list: List<T>): ImmutableList<T> {
    return remember(list) { list.toImmutable() }
}
