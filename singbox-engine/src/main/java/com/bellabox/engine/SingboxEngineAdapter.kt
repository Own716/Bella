package com.bellabox.engine

import android.content.Context
import com.bellabox.core.common.AppLogger
import com.bellabox.core.model.TrafficStats
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.SetupOptions
import io.nekohasekai.libbox.SystemProxyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class SingboxEngineAdapter(
    private val context: Context,
    private val platformInterface: PlatformInterface
) : CommandServerHandler {

    companion object {
        private const val TAG = "SingboxEngineAdapter"
        const val CORE_VERSION = "1.15.0-alpha.9"
        const val CORE_CHANNEL = "testing"
        const val CORE_COMMIT = "af60b5e"

        private val _trafficStats = MutableStateFlow(TrafficStats())
        val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

        fun getCoreVersion(): String {
            return try {
                Libbox.version()
            } catch (e: Throwable) {
                CORE_VERSION
            }
        }
    }

    private var commandServer: CommandServer? = null
    private val adapterScope = CoroutineScope(Dispatchers.IO + Job())
    private var trafficMonitorJob: Job? = null
    private var connectedStartTime = 0L

    fun setup(baseDir: File, workingDir: File, tempDir: File) {
        try {
            val setupOptions = SetupOptions().apply {
                basePath = baseDir.absolutePath
                workingPath = workingDir.absolutePath
                tempPath = tempDir.absolutePath
                logMaxLines = 2000
                debug = false
                crashReportSource = "BellaBox"
                appVersion = "1.0.1-preview"
                appMarketingVersion = "1.0.1"
            }
            Libbox.setup(setupOptions)
            AppLogger.i(TAG, "Libbox setup completed successfully (Core: ${getCoreVersion()})")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to setup Libbox: ${e.message}", e)
        }
    }

    @Synchronized
    fun start(configJson: String) {
        try {
            SingboxConfigValidator.validateJsonConfig(configJson)
            ConnectionStateMachine.markConnecting()

            val server = CommandServer(this, platformInterface)
            server.start()
            this.commandServer = server

            val overrideOptions = OverrideOptions()
            server.startOrReloadService(configJson, overrideOptions)

            connectedStartTime = System.currentTimeMillis()
            startTrafficMonitor()
            ConnectionStateMachine.markConnected()
            AppLogger.i(TAG, "Sing-box core started successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start Sing-box core: ${e.message}", e)
            ConnectionStateMachine.markFailed(e.message ?: "Failed to start Sing-box core")
            stop()
            throw e
        }
    }

    @Synchronized
    fun reload(configJson: String) {
        try {
            SingboxConfigValidator.validateJsonConfig(configJson)
            commandServer?.startOrReloadService(configJson, OverrideOptions())
            AppLogger.i(TAG, "Sing-box core reloaded successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to reload Sing-box core: ${e.message}", e)
            throw e
        }
    }

    @Synchronized
    fun stop() {
        ConnectionStateMachine.markStopping()
        stopTrafficMonitor()
        try {
            commandServer?.close()
            commandServer = null
        } catch (e: Exception) {
            AppLogger.w(TAG, "Error closing CommandServer: ${e.message}")
        }
        ConnectionStateMachine.markStopped()
        AppLogger.i(TAG, "Sing-box core stopped")
    }

    private fun startTrafficMonitor() {
        stopTrafficMonitor()
        trafficMonitorJob = adapterScope.launch {
            val myUid = android.os.Process.myUid()
            val initialTx = android.net.TrafficStats.getUidTxBytes(myUid).let { if (it < 0) 0L else it }
            val initialRx = android.net.TrafficStats.getUidRxBytes(myUid).let { if (it < 0) 0L else it }
            var prevTx = initialTx
            var prevRx = initialRx

            while (isActive) {
                delay(1000)
                val duration = if (connectedStartTime > 0) (System.currentTimeMillis() - connectedStartTime) / 1000 else 0
                val curTx = android.net.TrafficStats.getUidTxBytes(myUid).let { if (it < 0) 0L else it }
                val curRx = android.net.TrafficStats.getUidRxBytes(myUid).let { if (it < 0) 0L else it }

                val upSpeed = (curTx - prevTx).coerceAtLeast(0L)
                val downSpeed = (curRx - prevRx).coerceAtLeast(0L)
                val totalUp = (curTx - initialTx).coerceAtLeast(0L)
                val totalDown = (curRx - initialRx).coerceAtLeast(0L)

                prevTx = curTx
                prevRx = curRx

                _trafficStats.value = TrafficStats(
                    uplinkSpeedBytesPerSec = upSpeed,
                    downlinkSpeedBytesPerSec = downSpeed,
                    totalUplinkBytes = totalUp,
                    totalDownlinkBytes = totalDown,
                    connectedDurationSeconds = duration
                )
            }
        }
    }

    private fun stopTrafficMonitor() {
        trafficMonitorJob?.cancel()
        trafficMonitorJob = null
        _trafficStats.value = TrafficStats()
    }

    override fun serviceStop() {
        AppLogger.i(TAG, "ServiceStop called from libbox")
        stop()
    }

    override fun serviceReload() {
        AppLogger.i(TAG, "ServiceReload called from libbox")
    }

    override fun getSystemProxyStatus(): SystemProxyStatus? {
        return SystemProxyStatus().apply {
            available = true
            enabled = true
        }
    }

    override fun setSystemProxyEnabled(isEnabled: Boolean) {
        AppLogger.i(TAG, "SetSystemProxyEnabled: $isEnabled")
    }

    override fun triggerNativeCrash() {
        Thread {
            Thread.sleep(200)
            throw RuntimeException("debug native crash requested by libbox")
        }.start()
    }

    override fun writeDebugMessage(message: String?) {
        if (!message.isNullOrBlank()) {
            AppLogger.d(TAG, message)
        }
    }

    override fun connectSSHAgent(): Int = -1
}
