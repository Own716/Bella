package com.bellabox.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.database.NodeEntity
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.Subscription
import com.bellabox.core.network.NodeUriParser
import com.bellabox.core.network.SpeedTestEngine
import com.bellabox.core.network.SubscriptionFetcher
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

    private val db = (application as BellaApplication).database
    private val speedTestEngine = SpeedTestEngine(maxConcurrency = 4)
    private val subscriptionFetcher = SubscriptionFetcher()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(NodeSortMode.DEFAULT)
    val sortMode: StateFlow<NodeSortMode> = _sortMode.asStateFlow()

    private val _selectedProtocol = MutableStateFlow<String?>(null)
    val selectedProtocol: StateFlow<String?> = _selectedProtocol.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    val nodes: StateFlow<List<ProxyNode>> = combine(
        db.nodeDao().getAllNodesFlow(),
        _searchQuery,
        _sortMode,
        _selectedProtocol
    ) { entityList, query, sort, proto ->
        var list = entityList.map { it.toModel() }

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
            NodeSortMode.DEFAULT -> list // already sorted by favorite and id in DAO
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

    fun toggleFavorite(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            db.nodeDao().updateFavorite(node.id, !node.isFavorite)
        }
    }

    fun deleteNode(node: ProxyNode) {
        viewModelScope.launch(Dispatchers.IO) {
            db.nodeDao().deleteNode(NodeEntity.fromModel(node))
        }
    }

    fun importNodeFromUrl(url: String): Boolean {
        val node = NodeUriParser.parse(url) ?: return false
        viewModelScope.launch(Dispatchers.IO) {
            db.nodeDao().insertNode(NodeEntity.fromModel(node))
        }
        return true
    }

    fun importSubscription(name: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sub = Subscription(name = name, url = url)
            val subId = db.subscriptionDao().insert(com.bellabox.core.database.SubscriptionEntity.fromModel(sub))
            val result = subscriptionFetcher.fetch(sub.copy(id = subId))
            if (result.isSuccess) {
                val data = result.getOrThrow()
                db.subscriptionDao().update(com.bellabox.core.database.SubscriptionEntity.fromModel(data.subscription))
                val entities = data.nodes.map { NodeEntity.fromModel(it.copy(subscriptionId = subId)) }
                db.nodeDao().insertNodes(entities)
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
                        db.nodeDao().updateLatency(
                            node.id,
                            result.httpDelayMs,
                            result.qualityScore,
                            result.timestamp
                        )
                    } else {
                        db.nodeDao().updateLatency(node.id, -1, 0, result.timestamp)
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
                db.nodeDao().updateLatency(
                    node.id,
                    result.httpDelayMs,
                    result.qualityScore,
                    result.timestamp
                )
            } else {
                db.nodeDao().updateLatency(node.id, -1, 0, result.timestamp)
            }
        }
    }
}
