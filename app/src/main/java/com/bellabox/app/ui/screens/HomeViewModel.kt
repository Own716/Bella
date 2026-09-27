package com.bellabox.app.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.model.ConnectionState
import com.bellabox.core.model.DnsConfiguration
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.TrafficStats
import com.bellabox.engine.ConnectionStateMachine
import com.bellabox.engine.SingboxConfigBuilder
import com.bellabox.engine.SingboxEngineAdapter
import com.bellabox.vpn.BellaVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BellaApplication
    private val nodeRepo = app.nodeRepository
    private val ruleRepo = app.ruleRepository
    private val groupRepo = app.strategyGroupRepository
    private val settingsRepo = app.settingsRepository
    private val configBuilder = SingboxConfigBuilder()

    val connectionState: StateFlow<ConnectionState> = ConnectionStateMachine.state
    val lastError: StateFlow<String?> = ConnectionStateMachine.lastErrorMessage

    private val _activeNode = MutableStateFlow<ProxyNode?>(null)
    val activeNode: StateFlow<ProxyNode?> = _activeNode.asStateFlow()

    // 100% real traffic stats from SingboxEngineAdapter and android.net.TrafficStats
    val trafficStats: StateFlow<TrafficStats> = SingboxEngineAdapter.trafficStats

    private val _downloadHistory = MutableStateFlow<List<Long>>(listOf(0L, 0L))
    val downloadHistory: StateFlow<List<Long>> = _downloadHistory.asStateFlow()

    private val _uploadHistory = MutableStateFlow<List<Long>>(listOf(0L, 0L))
    val uploadHistory: StateFlow<List<Long>> = _uploadHistory.asStateFlow()

    init {
        loadActiveNode()
        observeRealTraffic()
    }

    private fun loadActiveNode() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.selectedNodeId.collectLatest { savedId ->
                if (savedId != null) {
                    val node = nodeRepo.getNodeById(savedId)
                    if (node != null) {
                        _activeNode.value = node
                        return@collectLatest
                    }
                }
                val all = nodeRepo.getAllNodes()
                if (all.isNotEmpty()) {
                    val first = all.first()
                    _activeNode.value = first
                    settingsRepo.setSelectedNodeId(first.id)
                } else {
                    _activeNode.value = null
                }
            }
        }
    }

    private fun observeRealTraffic() {
        viewModelScope.launch(Dispatchers.Default) {
            SingboxEngineAdapter.trafficStats.collectLatest { stats ->
                if (ConnectionStateMachine.currentState == ConnectionState.CONNECTED) {
                    _downloadHistory.value = (_downloadHistory.value + stats.downlinkSpeedBytesPerSec).takeLast(25)
                    _uploadHistory.value = (_uploadHistory.value + stats.uplinkSpeedBytesPerSec).takeLast(25)
                } else {
                    _downloadHistory.value = listOf(0L, 0L)
                    _uploadHistory.value = listOf(0L, 0L)
                }
            }
        }
    }

    fun selectNode(node: ProxyNode) {
        _activeNode.value = node
        settingsRepo.setSelectedNodeId(node.id)
        if (connectionState.value == ConnectionState.CONNECTED) {
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
                val allNodes = nodeRepo.getAllNodes()
                val rules = ruleRepo.getAllRules()
                val groups = groupRepo.getAllGroups()
                val dnsConfig = DnsConfiguration(
                    directDns = settingsRepo.dnsDirect.value,
                    proxyDns = settingsRepo.dnsRemote.value,
                    fakeIpEnabled = settingsRepo.fakeIpEnabled.value
                )

                val configJson = configBuilder.buildComplete(
                    activeNode = node,
                    allNodes = allNodes,
                    strategyGroups = groups,
                    dnsConfig = dnsConfig,
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
