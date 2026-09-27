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

        val title = when (state) {
            ConnectionState.CONNECTED -> "BellaBox: Connected"
            ConnectionState.CONNECTING -> "BellaBox: Connecting&#8230;"
            ConnectionState.RECONNECTING -> "BellaBox: Reconnecting&#8230;"
            ConnectionState.STARTING -> "BellaBox: Initializing&#8230;"
            else -> "BellaBox: Proxy Tunnel"
        }

        val content = if (trafficSummary.isNotBlank()) {
            "$activeNodeName | $trafficSummary"
        } else {
            "Active: $activeNodeName"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectPendingIntent)
            .build()
    }
}
