package com.bellabox.core.model

enum class StrategyType(val label: String, val description: String) {
    MANUAL("Manual", "Manually select an active proxy node"),
    URLTEST("Lowest Latency", "Automatically select the node with lowest latency"),
    FALLBACK("Fallback", "Switch to backup node when primary node is unavailable"),
    CONSISTENT_HASH("Consistent Hash", "Map target domains/IPs consistently to same nodes"),
    ROUND_ROBIN("Round Robin", "Cycle sequentially through all healthy nodes")
}

data class StrategyGroup(
    val id: Long = 0,
    val tag: String,
    val name: String,
    val type: StrategyType,
    val nodeIds: List<Long> = emptyList(),
    val selectedNodeId: Long? = null,
    val urlTestUrl: String = "https://www.gstatic.com/generate_204",
    val urlTestIntervalMinutes: Int = 10,
    val urlTestToleranceMs: Int = 50,
    val fallbackCooldownSeconds: Int = 300
)
