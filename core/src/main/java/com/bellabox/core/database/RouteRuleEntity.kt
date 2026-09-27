package com.bellabox.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.bellabox.core.model.RouteRule
import com.bellabox.core.model.RuleActionType
import com.bellabox.core.model.RuleType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "route_rules")
data class RouteRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ruleType: String,
    val values: String, // comma-separated
    val action: String,
    val targetOutboundTag: String = "",
    val isEnabled: Boolean = true,
    val priority: Int = 0
) {
    fun toModel(): RouteRule = RouteRule(
        id = id,
        name = name,
        ruleType = try { RuleType.valueOf(ruleType) } catch (e: Exception) { RuleType.DOMAIN_SUFFIX },
        values = if (values.isBlank()) emptyList() else values.split(",").map { it.trim() },
        action = try { RuleActionType.valueOf(action) } catch (e: Exception) { RuleActionType.PROXY },
        targetOutboundTag = targetOutboundTag,
        isEnabled = isEnabled,
        priority = priority
    )

    companion object {
        fun fromModel(model: RouteRule): RouteRuleEntity = RouteRuleEntity(
            id = model.id,
            name = model.name,
            ruleType = model.ruleType.name,
            values = model.values.joinToString(","),
            action = model.action.name,
            targetOutboundTag = model.targetOutboundTag,
            isEnabled = model.isEnabled,
            priority = model.priority
        )
    }
}

@Dao
interface RouteRuleDao {
    @Query("SELECT * FROM route_rules ORDER BY priority ASC, id ASC")
    fun getAllRulesFlow(): Flow<List<RouteRuleEntity>>

    @Query("SELECT * FROM route_rules ORDER BY priority ASC, id ASC")
    suspend fun getAllRules(): List<RouteRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RouteRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RouteRuleEntity>)

    @Update
    suspend fun update(rule: RouteRuleEntity)

    @Delete
    suspend fun delete(rule: RouteRuleEntity)

    @Query("SELECT * FROM route_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): RouteRuleEntity?

    @Query("UPDATE route_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE route_rules SET priority = :priority WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: Int)

    @Query("DELETE FROM route_rules")
    suspend fun deleteAll()
}
