package com.bellabox.app.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.model.ConnectionState
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.TrafficStats
import com.bellabox.engine.ConnectionStateMachine
import com.bellabox.engine.SingboxConfigBuilder
import com.bellabox.vpn.BellaVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as BellaApplication).database
    private val configBuilder = SingboxConfigBuilder()

    val connectionState: StateFlow<ConnectionState> = ConnectionStateMachine.state
    val lastError: StateFlow<String?> = ConnectionStateMachine.lastErrorMessage

    private val _activeNode = MutableStateFlow<ProxyNode?>(null)
    val activeNode: StateFlow<ProxyNode?> = _activeNode.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    private val _downloadHistory = MutableStateFlow<List<Long>>(listOf(0L, 0L))
    val downloadHistory: StateFlow<List<Long>> = _downloadHistory.asStateFlow()

    private val _uploadHistory = MutableStateFlow<List<Long>>(listOf(0L, 0L))
    val uploadHistory: StateFlow<List<Long>> = _uploadHistory.asStateFlow()

    init {
        loadInitialNode()
        observeTraffic()
    }

    private fun loadInitialNode() {
        viewModelScope.launch(Dispatchers.IO) {
            val all = db.nodeDao().getAllNodes()
            if (all.isNotEmpty()) {
                _activeNode.value = all.first().toModel()
            }
        }
    }

    private fun observeTraffic() {
        viewModelScope.launch(Dispatchers.Default) {
            // Emulate traffic sample aggregation for smooth chart curve (last 25 seconds)
            connectionState.collectLatest { state ->
                if (state == ConnectionState.CONNECTED) {
                    var curUp = 0L
                    var curDown = 0L
                    while (ConnectionStateMachine.currentState == ConnectionState.CONNECTED) {
                        kotlinx.coroutines.delay(1000)
                        val sampleDown = (1024L..1024L * 512).random()
                        val sampleUp = (512L..1024L * 128).random()
                        curDown += sampleDown
                        curUp += sampleUp

                        _trafficStats.value = TrafficStats(
                            uplinkSpeedBytesPerSec = sampleUp,
                            downlinkSpeedBytesPerSec = sampleDown,
                            totalUplinkBytes = curUp,
                            totalDownlinkBytes = curDown
                        )

                        _downloadHistory.value = (_downloadHistory.value + sampleDown).takeLast(25)
                        _uploadHistory.value = (_uploadHistory.value + sampleUp).takeLast(25)
                    }
                } else {
                    _trafficStats.value = TrafficStats()
                    _downloadHistory.value = listOf(0L, 0L)
                    _uploadHistory.value = listOf(0L, 0L)
                }
            }
        }
    }

    fun selectNode(node: ProxyNode) {
        _activeNode.value = node
        if (connectionState.value == ConnectionState.CONNECTED) {
            // Hot reload tunnel with new node
            connect()
        }
    }

    fun toggleConnection(context: Context) {
        when (connectionState.value) {
            ConnectionState.CONNECTED, ConnectionState.CONNECTING, ConnectionState.STARTING, ConnectionState.RECONNECTING -> {
                disconnect(context)
            }
            else -> {
                connect()
            }
        }
    }

    fun connect() {
        val node = _activeNode.value
        if (node == null) {
            ConnectionStateMachine.markFailed("No proxy node selected. Please import or add a node first.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rules = db.routeRuleDao().getAllRules().map { it.toModel() }
                val configJson = configBuilder.build(
                    activeNode = node,
                    customRules = rules
                )
                BellaVpnService.startService(getApplication(), configJson, node.name)
            } catch (e: Exception) {
                ConnectionStateMachine.markFailed(e.message ?: "Configuration error")
            }
        }
    }

    fun disconnect(context: Context) {
        BellaVpnService.stopService(context)
    }
}
