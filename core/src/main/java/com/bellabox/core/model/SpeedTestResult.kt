package com.bellabox.core.model

data class SpeedTestResult(
    val nodeId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val tcpHandshakeMs: Long = -1,
    val tlsHandshakeMs: Long = -1,
    val httpDelayMs: Long = -1,
    val downloadSpeedBytesPerSec: Long = 0,
    val uploadSpeedBytesPerSec: Long = 0,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val qualityScore: Int = 0,
    val scoreBreakdown: List<String> = emptyList()
) {
    /**
     * Compute an explainable transparent quality score from 0 to 100 based on multi-stage metrics.
     */
    companion object {
        fun computeQuality(
            tcpMs: Long,
            tlsMs: Long,
            httpMs: Long,
            successRate: Float,
            downloadBps: Long
        ): Pair<Int, List<String>> {
            val breakdown = mutableListOf<String>()
            var score = 0

            // 1. Latency factor (max 40 pts)
            if (httpMs in 1..80) {
                score += 40
                breakdown.add("Ultra-low latency (<80ms): +40")
            } else if (httpMs in 81..150) {
                score += 30
                breakdown.add("Low latency (80-150ms): +30")
            } else if (httpMs in 151..300) {
                score += 20
                breakdown.add("Moderate latency (150-300ms): +20")
            } else if (httpMs > 300) {
                score += 10
                breakdown.add("High latency (>300ms): +10")
            } else {
                breakdown.add("Unreachable latency: +0")
            }

            // 2. Handshake speed factor (max 30 pts)
            val totalHandshake = (if (tcpMs > 0) tcpMs else 0) + (if (tlsMs > 0) tlsMs else 0)
            if (totalHandshake in 1..100) {
                score += 30
                breakdown.add("Fast handshake (<100ms): +30")
            } else if (totalHandshake in 101..250) {
                score += 20
                breakdown.add("Standard handshake (100-250ms): +20")
            } else if (totalHandshake > 250) {
                score += 10
                breakdown.add("Slow handshake (>250ms): +10")
            }

            // 3. Reliability & Success rate (max 30 pts)
            val reliabilityPoints = (successRate.coerceIn(0f, 1f) * 30).toInt()
            score += reliabilityPoints
            breakdown.add("Reliability (${(successRate * 100).toInt()}%): +$reliabilityPoints")

            return Pair(score.coerceIn(0, 100), breakdown)
        }
    }
}

data class Subscription(
    val id: Long = 0,
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
)
