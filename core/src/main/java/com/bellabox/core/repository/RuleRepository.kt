package com.bellabox.core.repository

import androidx.room.withTransaction
import com.bellabox.core.database.BellaDatabase
import com.bellabox.core.database.RouteRuleDao
import com.bellabox.core.database.RouteRuleEntity
import com.bellabox.core.model.RouteRule
import com.bellabox.core.model.RuleActionType
import com.bellabox.core.model.RuleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleRepository(private val database: BellaDatabase) {
    private val ruleDao: RouteRuleDao = database.routeRuleDao()

    fun getAllRulesFlow(): Flow<List<RouteRule>> {
        return ruleDao.getAllRulesFlow().map { list -> list.map { it.toModel() } }
    }

    suspend fun getAllRules(): List<RouteRule> {
        return ruleDao.getAllRules().map { it.toModel() }
    }

    suspend fun getRuleById(id: Long): RouteRule? {
        return ruleDao.getRuleById(id)?.toModel()
    }

    suspend fun insertRule(rule: RouteRule): Long {
        val entity = RouteRuleEntity.fromModel(rule)
        return ruleDao.insert(entity)
    }

    suspend fun updateRule(rule: RouteRule) {
        val entity = RouteRuleEntity.fromModel(rule)
        ruleDao.update(entity)
    }

    suspend fun deleteRule(id: Long) {
        val entity = ruleDao.getRuleById(id) ?: return
        ruleDao.delete(entity)
    }

    suspend fun duplicateRule(id: Long) {
        val existing = ruleDao.getRuleById(id)?.toModel() ?: return
        val newRule = existing.copy(
            id = 0,
            name = "${existing.name} (副本)",
            priority = existing.priority + 1
        )
        insertRule(newRule)
    }

    suspend fun toggleEnabled(id: Long, isEnabled: Boolean) {
        ruleDao.updateEnabled(id, isEnabled)
    }

    suspend fun toggleRuleEnabled(id: Long, isEnabled: Boolean) {
        toggleEnabled(id, isEnabled)
    }

    suspend fun updateRulePriority(id: Long, priority: Int) {
        ruleDao.updatePriority(id, priority)
    }

    suspend fun reorderRules(orderedIds: List<Long>) {
        database.withTransaction {
            orderedIds.forEachIndexed { index, id ->
                ruleDao.updatePriority(id, index)
            }
        }
    }

    suspend fun resetToDefaultRules() {
        database.withTransaction {
            ruleDao.deleteAll()
            val defaults = listOf(
                RouteRule(
                    name = "劫持 DNS / Hijack DNS",
                    ruleType = RuleType.PORT,
                    values = listOf("53"),
                    action = RuleActionType.PROXY,
                    targetOutboundTag = "dns-out",
                    priority = 0
                ),
                RouteRule(
                    name = "广告拦截 / Ad Block",
                    ruleType = RuleType.GEOSITE,
                    values = listOf("category-ads-all"),
                    action = RuleActionType.BLOCK,
                    priority = 1
                ),
                RouteRule(
                    name = "局域网直连 / Private LAN Direct",
                    ruleType = RuleType.IP_CIDR,
                    values = listOf("10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16", "127.0.0.0/8", "::1/128", "fe80::/10"),
                    action = RuleActionType.DIRECT,
                    priority = 2
                ),
                RouteRule(
                    name = "中国域名直连 / CN Domains Direct",
                    ruleType = RuleType.GEOSITE,
                    values = listOf("cn"),
                    action = RuleActionType.DIRECT,
                    priority = 3
                ),
                RouteRule(
                    name = "中国 IP 直连 / CN IPs Direct",
                    ruleType = RuleType.GEOIP,
                    values = listOf("cn"),
                    action = RuleActionType.DIRECT,
                    priority = 4
                )
            )
            defaults.forEach { insertRule(it) }
        }
    }
}
