package com.bellabox.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.bellabox.core.model.Subscription
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val nodeCount: Int = 0,
    val lastUpdate: Long = 0,
    val isAutoUpdate: Boolean = true,
    val updateIntervalHours: Int = 24,
    val uploadBytes: Long = 0,
    val downloadBytes: Long = 0,
    val totalBytes: Long = 0,
    val expireTime: Long = 0,
    val lastStatusMessage: String = ""
) {
    fun toModel(): Subscription = Subscription(
        id = id,
        name = name,
        url = url,
        nodeCount = nodeCount,
        lastUpdate = lastUpdate,
        isAutoUpdate = isAutoUpdate,
        updateIntervalHours = updateIntervalHours,
        uploadBytes = uploadBytes,
        downloadBytes = downloadBytes,
        totalBytes = totalBytes,
        expireTime = expireTime,
        lastStatusMessage = lastStatusMessage
    )

    companion object {
        fun fromModel(model: Subscription): SubscriptionEntity = SubscriptionEntity(
            id = model.id,
            name = model.name,
            url = model.url,
            nodeCount = model.nodeCount,
            lastUpdate = model.lastUpdate,
            isAutoUpdate = model.isAutoUpdate,
            updateIntervalHours = model.updateIntervalHours,
            uploadBytes = model.uploadBytes,
            downloadBytes = model.downloadBytes,
            totalBytes = model.totalBytes,
            expireTime = model.expireTime,
            lastStatusMessage = model.lastStatusMessage
        )
    }
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY id ASC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY id ASC")
    suspend fun getAllSubscriptions(): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    suspend fun getSubscriptionById(id: Long): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sub: SubscriptionEntity): Long

    @Update
    suspend fun update(sub: SubscriptionEntity)

    @Delete
    suspend fun delete(sub: SubscriptionEntity)
}
