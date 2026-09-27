package com.bellabox.core.common

import android.util.Log
import com.bellabox.core.security.DataMasker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface DispatcherProvider {
    val main: CoroutineDispatcher get() = Dispatchers.Main
    val io: CoroutineDispatcher get() = Dispatchers.IO
    val default: CoroutineDispatcher get() = Dispatchers.Default
}

class DefaultDispatcherProvider : DispatcherProvider

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

data class LogMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
)

object AppLogger {
    private const val DEFAULT_TAG = "BellaBox"
    private val _logsFlow = MutableSharedFlow<LogMessage>(extraBufferCapacity = 500)
    val logsFlow: SharedFlow<LogMessage> = _logsFlow.asSharedFlow()

    fun d(tag: String = DEFAULT_TAG, message: String) {
        val sanitized = sanitize(message)
        Log.d(tag, sanitized)
        _logsFlow.tryEmit(LogMessage(level = LogLevel.DEBUG, tag = tag, message = sanitized))
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        val sanitized = sanitize(message)
        Log.i(tag, sanitized)
        _logsFlow.tryEmit(LogMessage(level = LogLevel.INFO, tag = tag, message = sanitized))
    }

    fun w(tag: String = DEFAULT_TAG, message: String) {
        val sanitized = sanitize(message)
        Log.w(tag, sanitized)
        _logsFlow.tryEmit(LogMessage(level = LogLevel.WARN, tag = tag, message = sanitized))
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        Log.e(tag, sanitized, throwable)
        val fullMsg = if (throwable != null) "$sanitized: ${throwable.message}" else sanitized
        _logsFlow.tryEmit(LogMessage(level = LogLevel.ERROR, tag = tag, message = fullMsg))
    }

    private fun sanitize(input: String): String {
        // Automatically sanitize sensitive UUID or Password patterns
        return input.replace(Regex("(?i)(password|uuid|token|pbk)=([a-zA-Z0-9_-]{8,})")) { matchResult ->
            val key = matchResult.groupValues[1]
            val value = matchResult.groupValues[2]
            "$key=${DataMasker.maskSensitive(value)}"
        }
    }
}
