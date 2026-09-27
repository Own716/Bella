package com.bellabox.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "proxy_nodes")
data class NodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriptionId: Long? = null,
    val name: String,
    val server: String,
    val port: Int,
    val protocol: String,
    val uuid: String = "",
    val password: String = "",
    val flow: String = "",
    val security: String = "tls",
    val sni: String = "",
    val alpn: String = "", // comma-separated
    val fingerprint: String = "chrome",
    val publicKey: String = "",
    val shortId: String = "",
    val transport: String = "tcp",
    val transportPath: String = "",
    val transportHost: String = "",
    val isFavorite: Boolean = false,
    val latencyMs: Long = -1,
    val lastTestTime: Long = 0,
    val qualityScore: Int = -1,
    val isAvailable: Boolean = true,
    val fingerprintHash: String = ""
) {
    fun toModel(): ProxyNode {
        return ProxyNode(
            id = id,
            subscriptionId = subscriptionId,
            name = name,
            server = server,
            port = port,
            protocol = ProtocolType.fromString(protocol),
            uuid = uuid,
            password = password,
            flow = flow,
            security = security,
            sni = sni,
            alpn = if (alpn.isBlank()) emptyList() else alpn.split(","),
            fingerprint = fingerprint,
            publicKey = publicKey,
            shortId = shortId,
            transport = transport,
            transportPath = transportPath,
            transportHost = transportHost,
            isFavorite = isFavorite,
            latencyMs = latencyMs,
            lastTestTime = lastTestTime,
            qualityScore = qualityScore,
            isAvailable = isAvailable
        )
    }

    companion object {
        fun fromModel(model: ProxyNode): NodeEntity {
            return NodeEntity(
                id = model.id,
                subscriptionId = model.subscriptionId,
                name = model.name,
                server = model.server,
                port = model.port,
                protocol = model.protocol.name,
                uuid = model.uuid,
                password = model.password,
                flow = model.flow,
                security = model.security,
                sni = model.sni,
                alpn = model.alpn.joinToString(","),
                fingerprint = model.fingerprint,
                publicKey = model.publicKey,
                shortId = model.shortId,
                transport = model.transport,
                transportPath = model.transportPath,
                transportHost = model.transportHost,
                isFavorite = model.isFavorite,
                latencyMs = model.latencyMs,
                lastTestTime = model.lastTestTime,
                qualityScore = model.qualityScore,
                isAvailable = model.isAvailable,
                fingerprintHash = model.computeFingerprint()
            )
        }
    }
}

@Dao
interface NodeDao {
    @Query("SELECT * FROM proxy_nodes ORDER BY isFavorite DESC, latencyMs ASC, id ASC")
    fun getAllNodesFlow(): Flow<List<NodeEntity>>

    @Query("SELECT * FROM proxy_nodes WHERE isFavorite = 1 ORDER BY latencyMs ASC, id ASC")
    fun getFavoriteNodesFlow(): Flow<List<NodeEntity>>

    @Query("SELECT * FROM proxy_nodes ORDER BY isFavorite DESC, latencyMs ASC, id ASC")
    suspend fun getAllNodes(): List<NodeEntity>

    @Query("SELECT * FROM proxy_nodes WHERE id = :id LIMIT 1")
    suspend fun getNodeById(id: Long): NodeEntity?

    @Query("SELECT * FROM proxy_nodes WHERE subscriptionId = :subId")
    suspend fun getNodesBySubscription(subId: Long): List<NodeEntity>

    @Query("SELECT * FROM proxy_nodes WHERE subscriptionId = :subId")
    suspend fun getNodesBySubscriptionId(subId: Long): List<NodeEntity>

    @Query("SELECT * FROM proxy_nodes WHERE fingerprintHash = :hash LIMIT 1")
    suspend fun getNodeByFingerprint(hash: String): NodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: NodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<NodeEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NodeEntity>): List<Long>

    @Update
    suspend fun update(node: NodeEntity)

    @Update
    suspend fun updateNode(node: NodeEntity)

    @Delete
    suspend fun delete(node: NodeEntity)

    @Delete
    suspend fun deleteNode(node: NodeEntity)

    @Query("DELETE FROM proxy_nodes WHERE subscriptionId = :subId")
    suspend fun deleteBySubscription(subId: Long)

    @Query("DELETE FROM proxy_nodes WHERE subscriptionId = :subId")
    suspend fun deleteBySubscriptionId(subId: Long)

    @Query("DELETE FROM proxy_nodes")
    suspend fun deleteAll()

    @Query("UPDATE proxy_nodes SET latencyMs = :latency, qualityScore = :score, lastTestTime = :testTime WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Long, score: Int, testTime: Long)

    @Query("UPDATE proxy_nodes SET isFavorite = :fav WHERE id = :id")
    suspend fun updateFavorite(id: Long, fav: Boolean)
}
