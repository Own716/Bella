package com.bellabox.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.Subscription
import com.bellabox.core.network.NodeUriParser
import com.bellabox.core.network.SpeedTestEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NodeSortMode {
    DEFAULT, LATENCY, NAME
}

class NodesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BellaApplication
    private val nodeRepo = app.nodeRepository
    private val subRepo = app.subscriptionRepository
    private val speedTestEngine = SpeedTestEngine(maxConcurrency = 4)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(NodeSortMode.DEFAULT)
    val sortMode: StateFlow<NodeSortMode> = _sortMode.asStateFlow()

    private val _selectedProtocol = MutableStateFlow<String?>(null)
    val selectedProtocol: StateFlow<String?> = _selectedProtocol.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val _updatingSubId = MutableStateFlow<Long?>(null)
    val updatingSubId: StateFlow<Long?> = _updatingSubId.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    val subscriptions: StateFlow<List<Subscription>> = subRepo.getAllSubscriptionsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val nodes: StateFlow<List<ProxyNode>> = combine(
        nodeRepo.getAllNodesFlow(),
        _searchQuery,
        _sortMode,
        _selectedProtocol
    ) { nodeList, query, sort, proto ->
        var list = nodeList

        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.server.contains(query, ignoreCase = true) ||
                it.protocol.label.contains(query, ignoreCase = true)
            }
        }

        if (proto != null) {
            list = list.filter { it.protocol.name.equals(proto, ignoreCase = true) }
        }

        when (sort) {
            NodeSortMode.LATENCY -> list.sortedWith(
                compareBy<ProxyNode> { it.latencyMs <= 0 }.thenBy { it.latencyMs }
            )
            NodeSortMode.NAME -> list.sortedBy { it.name }
            NodeSortMode.DEFAULT -> list
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: NodeSortMode) {
        _sortMode.value = mode
    }

    fun setProtocolFilter(protocol: String?) {
        _selectedProtocol.value = protocol
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    fun toggleFavorite(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepo.toggleFavorite(node.id)
        }
    }

    fun addNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepo.insertNode(node)
            _operationMessage.value = "已成功添加节点: ${node.name}"
        }
    }

    fun updateNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepo.updateNode(node)
            _operationMessage.value = "已更新节点: ${node.name}"
        }
    }

    fun deleteNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepo.deleteNode(node.id)
            _operationMessage.value = "已删除节点: ${node.name}"
        }
    }

    fun importNodeFromUrl(url: String): Boolean {
        val node = NodeUriParser.parse(url) ?: return false
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepo.insertNode(node)
            _operationMessage.value = "已成功解析并导入节点: ${node.name}"
        }
        return true
    }

    fun addSubscription(name: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sub = Subscription(name = name, url = url)
            val subId = subRepo.insertSubscription(sub)
            updateSubscriptionNodes(subId)
        }
    }

    fun updateSubscription(sub: Subscription) {
        viewModelScope.launch(Dispatchers.IO) {
            subRepo.updateSubscription(sub)
            _operationMessage.value = "已保存订阅配置: ${sub.name}"
        }
    }

    fun deleteSubscription(id: Long, deleteAssociatedNodes: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            subRepo.deleteSubscription(id, deleteAssociatedNodes)
            _operationMessage.value = "已删除该订阅"
        }
    }

    fun updateSubscriptionNodes(id: Long) {
        if (_updatingSubId.value != null) return
        _updatingSubId.value = id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = subRepo.updateSubscriptionNodes(id)
                if (res.isSuccess) {
                    val data = res.getOrThrow()
                    _operationMessage.value = "更新成功: 共导入 ${data.nodes.size} 个节点" +
                            if (data.duplicateCount > 0) " (过滤 ${data.duplicateCount} 个重复项)" else ""
                } else {
                    _operationMessage.value = res.exceptionOrNull()?.message ?: "更新失败"
                }
            } finally {
                _updatingSubId.value = null
            }
        }
    }

    fun testAllNodes() {
        if (_isTesting.value) return
        _isTesting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentNodes = nodes.value
                for (node in currentNodes) {
                    val result = speedTestEngine.testNode(node)
                    if (result.isSuccess) {
                        nodeRepo.updateSpeedResult(
                            node.id,
                            result.httpDelayMs,
                            result.qualityScore
                        )
                    } else {
                        nodeRepo.updateSpeedResult(node.id, -1, 0)
                    }
                }
            } finally {
                _isTesting.value = false
            }
        }
    }

    fun testSingleNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = speedTestEngine.testNode(node)
            if (result.isSuccess) {
                nodeRepo.updateSpeedResult(
                    node.id,
                    result.httpDelayMs,
                    result.qualityScore
                )
            } else {
                nodeRepo.updateSpeedResult(node.id, -1, 0)
            }
        }
    }
}
