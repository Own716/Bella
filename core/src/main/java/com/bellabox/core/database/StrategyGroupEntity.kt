package com.bellabox.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.bellabox.core.model.StrategyGroup
import com.bellabox.core.model.StrategyType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "strategy_groups")
data class StrategyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val name: String,
    val type: String,
    val nodeIds: String = "", // comma-separated ids
    val selectedNodeId: Long? = null,
    val urlTestUrl: String = "https://www.gstatic.com/generate_204",
    val urlTestIntervalMinutes: Int = 10,
    val urlTestToleranceMs: Int = 50,
    val fallbackCooldownSeconds: Int = 300
) {
    fun toModel(): StrategyGroup = StrategyGroup(
        id = id,
        tag = tag,
        name = name,
        type = try { StrategyType.valueOf(type) } catch (e: Exception) { StrategyType.MANUAL },
        nodeIds = if (nodeIds.isBlank()) emptyList() else nodeIds.split(",").mapNotNull { it.trim().toLongOrNull() },
        selectedNodeId = selectedNodeId,
        urlTestUrl = urlTestUrl,
        urlTestIntervalMinutes = urlTestIntervalMinutes,
        urlTestToleranceMs = urlTestToleranceMs,
        fallbackCooldownSeconds = fallbackCooldownSeconds
    )

    companion object {
        fun fromModel(model: StrategyGroup): StrategyGroupEntity = StrategyGroupEntity(
            id = model.id,
            tag = model.tag,
            name = model.name,
            type = model.type.name,
            nodeIds = model.nodeIds.joinToString(","),
            selectedNodeId = model.selectedNodeId,
            urlTestUrl = model.urlTestUrl,
            urlTestIntervalMinutes = model.urlTestIntervalMinutes,
            urlTestToleranceMs = model.urlTestToleranceMs,
            fallbackCooldownSeconds = model.fallbackCooldownSeconds
        )
    }
}

@Dao
interface StrategyGroupDao {
    @Query("SELECT * FROM strategy_groups ORDER BY id ASC")
    fun getAllGroupsFlow(): Flow<List<StrategyGroupEntity>>

    @Query("SELECT * FROM strategy_groups ORDER BY id ASC")
    suspend fun getAllGroups(): List<StrategyGroupEntity>

    @Query("SELECT * FROM strategy_groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Long): StrategyGroupEntity?

    @Query("SELECT * FROM strategy_groups WHERE tag = :tag LIMIT 1")
    suspend fun getGroupByTag(tag: String): StrategyGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: StrategyGroupEntity): Long

    @Update
    suspend fun update(group: StrategyGroupEntity)

    @Delete
    suspend fun delete(group: StrategyGroupEntity)

    @Query("UPDATE strategy_groups SET selectedNodeId = :nodeId WHERE id = :groupId")
    suspend fun updateSelectedNode(groupId: Long, nodeId: Long?)
}
