package com.bellabox.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bellabox.core.model.ConnectionState

class VpnNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bellabox_vpn_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_DISCONNECT = "com.bellabox.vpn.ACTION_DISCONNECT"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BellaBox Service Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time connection status and tunnel details"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        state: ConnectionState,
        activeNodeName: String = "Proxy Server",
        trafficSummary: String = ""
    ): Notification {
        val disconnectIntent = Intent(context, BellaVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            context,
            0,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val isZh = context.getSharedPreferences("bellabox_settings", Context.MODE_PRIVATE)
            .getString("app_language", "zh") != "en"

        val title = when (state) {
            ConnectionState.CONNECTED -> if (isZh) "BellaBox: 已安全连接" else "BellaBox: Connected"
            ConnectionState.CONNECTING -> if (isZh) "BellaBox: 正在建立隧道…" else "BellaBox: Connecting…"
            ConnectionState.RECONNECTING -> if (isZh) "BellaBox: 网络波动重连中…" else "BellaBox: Reconnecting…"
            ConnectionState.STARTING -> if (isZh) "BellaBox: 正在初始化内核…" else "BellaBox: Initializing…"
            else -> if (isZh) "BellaBox: 代理通道" else "BellaBox: Proxy Tunnel"
        }

        val content = if (trafficSummary.isNotBlank()) {
            "$activeNodeName | $trafficSummary"
        } else {
            if (isZh) "当前节点: $activeNodeName" else "Active: $activeNodeName"
        }

        val disconnectText = if (isZh) "断开连接" else "Disconnect"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, disconnectText, disconnectPendingIntent)
            .build()
    }
}
