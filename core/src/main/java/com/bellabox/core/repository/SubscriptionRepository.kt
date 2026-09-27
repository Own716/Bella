package com.bellabox.core.repository

import androidx.room.withTransaction
import com.bellabox.core.database.BellaDatabase
import com.bellabox.core.database.NodeDao
import com.bellabox.core.database.NodeEntity
import com.bellabox.core.database.SubscriptionDao
import com.bellabox.core.database.SubscriptionEntity
import com.bellabox.core.model.Subscription
import com.bellabox.core.network.SubscriptionFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepository(
    private val database: BellaDatabase,
    private val fetcher: SubscriptionFetcher = SubscriptionFetcher()
) {
    private val subDao: SubscriptionDao = database.subscriptionDao()
    private val nodeDao: NodeDao = database.nodeDao()

    fun getAllSubscriptionsFlow(): Flow<List<Subscription>> {
        return subDao.getAllSubscriptionsFlow().map { list -> list.map { it.toModel() } }
    }

    suspend fun getAllSubscriptions(): List<Subscription> {
        return subDao.getAllSubscriptions().map { it.toModel() }
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return subDao.getSubscriptionById(id)?.toModel()
    }

    suspend fun insertSubscription(sub: Subscription): Long {
        val entity = SubscriptionEntity.fromModel(sub)
        return subDao.insert(entity)
    }

    suspend fun updateSubscription(sub: Subscription) {
        val entity = SubscriptionEntity.fromModel(sub)
        subDao.update(entity)
    }

    suspend fun deleteSubscription(id: Long, deleteAssociatedNodes: Boolean = true) {
        val sub = subDao.getSubscriptionById(id) ?: return
        database.withTransaction {
            if (deleteAssociatedNodes) {
                nodeDao.deleteBySubscriptionId(id)
            }
            subDao.delete(sub)
        }
    }

    suspend fun updateSubscriptionNodes(id: Long): Result<SubscriptionFetcher.FetchResult> {
        val subEntity = subDao.getSubscriptionById(id)
            ?: return Result.failure(Exception("找不到该订阅 (ID: $id)"))

        val subModel = subEntity.toModel()
        val fetchResult = fetcher.fetch(subModel)

        if (fetchResult.isFailure) {
            val err = fetchResult.exceptionOrNull()?.message ?: "未知网络错误"
            // Update last status message without deleting old nodes
            subDao.update(subEntity.copy(lastStatusMessage = "更新失败: $err"))
            return Result.failure(Exception(err))
        }

        val result = fetchResult.getOrThrow()
        if (result.nodes.isEmpty()) {
            val msg = "更新异常: 解析到 0 个节点，已自动保护现有节点不受影响"
            subDao.update(subEntity.copy(lastStatusMessage = msg))
            return Result.failure(Exception(msg))
        }

        // Fetch existing nodes to preserve favorite status, latency, and quality scores
        val existingNodes = nodeDao.getNodesBySubscriptionId(id)
        val existingMap = existingNodes.associateBy { it.fingerprintHash }

        val newEntities = result.nodes.map { node ->
            val fp = node.computeFingerprint()
            val old = existingMap[fp]
            NodeEntity.fromModel(
                node.copy(
                    isFavorite = old?.isFavorite ?: node.isFavorite,
                    latencyMs = if (old != null && old.latencyMs > 0) old.latencyMs else node.latencyMs,
                    qualityScore = if (old != null && old.qualityScore > 0) old.qualityScore else node.qualityScore,
                    lastTestTime = if (old != null && old.lastTestTime > 0) old.lastTestTime else node.lastTestTime
                )
            )
        }

        // Perform transactional database write
        database.withTransaction {
            nodeDao.deleteBySubscriptionId(id)
            nodeDao.insertAll(newEntities)
            subDao.update(SubscriptionEntity.fromModel(result.subscription))
        }

        return Result.success(result)
    }
}
