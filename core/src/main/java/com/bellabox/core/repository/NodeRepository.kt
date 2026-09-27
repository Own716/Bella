package com.bellabox.core.repository

import com.bellabox.core.database.BellaDatabase
import com.bellabox.core.database.NodeDao
import com.bellabox.core.database.NodeEntity
import com.bellabox.core.model.ProxyNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NodeRepository(private val database: BellaDatabase) {
    private val nodeDao: NodeDao = database.nodeDao()

    fun getAllNodesFlow(): Flow<List<ProxyNode>> {
        return nodeDao.getAllNodesFlow().map { list -> list.map { it.toModel() } }
    }

    fun getFavoriteNodesFlow(): Flow<List<ProxyNode>> {
        return nodeDao.getFavoriteNodesFlow().map { list -> list.map { it.toModel() } }
    }

    suspend fun getAllNodes(): List<ProxyNode> {
        return nodeDao.getAllNodes().map { it.toModel() }
    }

    suspend fun getNodeById(id: Long): ProxyNode? {
        return nodeDao.getNodeById(id)?.toModel()
    }

    suspend fun insertNode(node: ProxyNode): Long {
        val entity = NodeEntity.fromModel(node)
        return nodeDao.insert(entity)
    }

    suspend fun updateNode(node: ProxyNode) {
        val entity = NodeEntity.fromModel(node)
        nodeDao.update(entity)
    }

    suspend fun deleteNode(id: Long) {
        val entity = nodeDao.getNodeById(id)
        if (entity != null) {
            nodeDao.delete(entity)
        }
    }

    suspend fun toggleFavorite(id: Long) {
        val node = nodeDao.getNodeById(id) ?: return
        nodeDao.updateFavorite(id, !node.isFavorite)
    }

    suspend fun updateSpeedResult(id: Long, latency: Long, score: Int) {
        nodeDao.updateLatency(id, latency, score, System.currentTimeMillis())
    }

    suspend fun updateSpeedResult(id: Long, latency: Long, jitter: Long = 0, loss: Float = 0f, score: Int = 0) {
        nodeDao.updateLatency(id, latency, score, System.currentTimeMillis())
    }

    suspend fun clearAllNodes() {
        nodeDao.deleteAll()
    }

    suspend fun deleteNodesBySubscriptionId(subId: Long) {
        nodeDao.deleteBySubscriptionId(subId)
    }
}
