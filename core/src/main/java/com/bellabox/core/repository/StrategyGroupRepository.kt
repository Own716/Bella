package com.bellabox.core.repository

import com.bellabox.core.database.BellaDatabase
import com.bellabox.core.database.StrategyGroupDao
import com.bellabox.core.database.StrategyGroupEntity
import com.bellabox.core.model.StrategyGroup
import com.bellabox.core.model.StrategyType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StrategyGroupRepository(private val database: BellaDatabase) {
    private val groupDao: StrategyGroupDao = database.strategyGroupDao()

    fun getAllGroupsFlow(): Flow<List<StrategyGroup>> {
        return groupDao.getAllGroupsFlow().map { list -> list.map { it.toModel() } }
    }

    suspend fun getAllGroups(): List<StrategyGroup> {
        return groupDao.getAllGroups().map { it.toModel() }
    }

    suspend fun getGroupById(id: Long): StrategyGroup? {
        return groupDao.getGroupById(id)?.toModel()
    }

    suspend fun getGroupByTag(tag: String): StrategyGroup? {
        return groupDao.getGroupByTag(tag)?.toModel()
    }

    suspend fun insertGroup(group: StrategyGroup): Long {
        val entity = StrategyGroupEntity.fromModel(group)
        return groupDao.insert(entity)
    }

    suspend fun updateGroup(group: StrategyGroup) {
        val entity = StrategyGroupEntity.fromModel(group)
        groupDao.update(entity)
    }

    suspend fun deleteGroup(id: Long) {
        val entity = groupDao.getGroupById(id) ?: return
        groupDao.delete(entity)
    }

    suspend fun selectNode(groupId: Long, nodeId: Long?) {
        groupDao.updateSelectedNode(groupId, nodeId)
    }

    suspend fun updateSelectedNode(groupId: Long, nodeId: Long?) {
        selectNode(groupId, nodeId)
    }

    suspend fun initializeDefaultGroups(allNodeIds: List<Long> = emptyList()) {
        val existing = groupDao.getAllGroups()
        if (existing.isEmpty()) {
            val defaults = listOf(
                StrategyGroup(
                    tag = "proxy",
                    name = "默认代理选择 / Proxy Selector",
                    type = StrategyType.MANUAL,
                    nodeIds = allNodeIds,
                    selectedNodeId = allNodeIds.firstOrNull()
                ),
                StrategyGroup(
                    tag = "auto-fast",
                    name = "自动优选 / Auto Lowest Latency",
                    type = StrategyType.URLTEST,
                    nodeIds = allNodeIds,
                    urlTestUrl = "https://www.gstatic.com/generate_204",
                    urlTestIntervalMinutes = 5,
                    urlTestToleranceMs = 50
                ),
                StrategyGroup(
                    tag = "fallback-group",
                    name = "故障转移 / Fallback Backup",
                    type = StrategyType.FALLBACK,
                    nodeIds = allNodeIds,
                    fallbackCooldownSeconds = 180
                ),
                StrategyGroup(
                    tag = "hash-group",
                    name = "一致性哈希 / Consistent Hash",
                    type = StrategyType.CONSISTENT_HASH,
                    nodeIds = allNodeIds
                ),
                StrategyGroup(
                    tag = "round-robin",
                    name = "轮询负载 / Round Robin",
                    type = StrategyType.ROUND_ROBIN,
                    nodeIds = allNodeIds
                )
            )
            defaults.forEach { insertGroup(it) }
        }
    }
}
