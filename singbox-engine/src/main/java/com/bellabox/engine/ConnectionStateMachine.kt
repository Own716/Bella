package com.bellabox.engine

import com.bellabox.core.common.AppLogger
import com.bellabox.core.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConnectionStateMachine {
    private const val TAG = "ConnectionStateMachine"

    private val _state = MutableStateFlow(ConnectionState.IDLE)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    val currentState: ConnectionState
        get() = _state.value

    @Synchronized
    fun transitionTo(newState: ConnectionState, errorMessage: String? = null) {
        val current = _state.value
        if (current == newState) return

        AppLogger.i(TAG, "State transition: $current -> $newState ${if (errorMessage != null) "($errorMessage)" else ""}")
        _lastErrorMessage.value = errorMessage
        _state.value = newState
    }

    fun markStarting() = transitionTo(ConnectionState.STARTING)
    fun markConnecting() = transitionTo(ConnectionState.CONNECTING)
    fun markConnected() = transitionTo(ConnectionState.CONNECTED)
    fun markReconnecting(reason: String? = null) = transitionTo(ConnectionState.RECONNECTING, reason)
    fun markStopping() = transitionTo(ConnectionState.STOPPING)
    fun markStopped() = transitionTo(ConnectionState.STOPPED)
    fun markFailed(error: String) = transitionTo(ConnectionState.FAILED, error)
}
