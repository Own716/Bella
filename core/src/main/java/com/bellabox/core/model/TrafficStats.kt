package com.bellabox.core.model

data class TrafficStats(
    val uplinkSpeedBytesPerSec: Long = 0,
    val downlinkSpeedBytesPerSec: Long = 0,
    val totalUplinkBytes: Long = 0,
    val totalDownlinkBytes: Long = 0,
    val connectedDurationSeconds: Long = 0
) {
    fun formattedUplinkSpeed(): String = formatBytesPerSec(uplinkSpeedBytesPerSec)
    fun formattedDownlinkSpeed(): String = formatBytesPerSec(downlinkSpeedBytesPerSec)
    fun formattedTotalUplink(): String = formatBytes(totalUplinkBytes)
    fun formattedTotalDownlink(): String = formatBytes(totalDownlinkBytes)

    companion object {
        fun formatBytesPerSec(bytesPerSec: Long): String {
            return when {
                bytesPerSec >= 1024 * 1024 * 1024 -> String.format("%.2f GB/s", bytesPerSec / (1024.0 * 1024 * 1024))
                bytesPerSec >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSec / (1024.0 * 1024))
                bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
                else -> "$bytesPerSec B/s"
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024L * 1024 * 1024 * 1024 -> String.format("%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024))
                bytes >= 1024L * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
                bytes >= 1024L * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
                bytes >= 1024L -> String.format("%.1f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}
