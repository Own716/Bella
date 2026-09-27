package com.bellabox.vpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import com.bellabox.core.common.AppLogger
import com.bellabox.core.model.ConnectionState
import com.bellabox.engine.ConnectionStateMachine
import com.bellabox.engine.SingboxEngineAdapter
import io.nekohasekai.libbox.TunOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BellaVpnService : VpnService() {

    companion object {
        private const val TAG = "BellaVpnService"
        const val ACTION_START = "com.bellabox.vpn.ACTION_START"
        const val ACTION_STOP = "com.bellabox.vpn.ACTION_STOP"
        const val EXTRA_CONFIG = "extra_config"
        const val EXTRA_NODE_NAME = "extra_node_name"

        fun startService(context: Context, configJson: String, nodeName: String = "Proxy Server") {
            val intent = Intent(context, BellaVpnService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONFIG, configJson)
                putExtra(EXTRA_NODE_NAME, nodeName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BellaVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var tunFd: ParcelFileDescriptor? = null
    private lateinit var notificationManager: VpnNotificationManager
    private lateinit var engineAdapter: SingboxEngineAdapter
    private lateinit var platformInterface: PlatformInterfaceImpl
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var activeNodeName: String = "Proxy Server"

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            AppLogger.i(TAG, "Network available: $network")
            if (ConnectionStateMachine.currentState == ConnectionState.RECONNECTING) {
                ConnectionStateMachine.markConnecting()
            }
        }

        override fun onLost(network: Network) {
            AppLogger.w(TAG, "Network lost: $network")
            if (ConnectionStateMachine.currentState == ConnectionState.CONNECTED) {
                ConnectionStateMachine.markReconnecting("Default network disconnected")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = VpnNotificationManager(this)
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        platformInterface = object : PlatformInterfaceImpl(cm) {
            override fun autoDetectInterfaceControl(fd: Int) {
                protect(fd)
            }

            override fun openTun(options: TunOptions): Int {
                return openTunInterface(options)
            }
        }

        engineAdapter = SingboxEngineAdapter(this, platformInterface)
        val baseDir = filesDir.apply { mkdirs() }
        val workingDir = (getExternalFilesDir(null) ?: filesDir).apply { mkdirs() }
        val tempDir = cacheDir.apply { mkdirs() }
        engineAdapter.setup(baseDir, workingDir, tempDir)

        registerNetworkListener(cm)
        observeConnectionState()
    }

    private fun registerNetworkListener(cm: ConnectivityManager) {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to register network callback: ${e.message}")
        }
    }

    private fun observeConnectionState() {
        serviceScope.launch {
            ConnectionStateMachine.state.collectLatest { state ->
                val notification = notificationManager.buildNotification(state, activeNodeName)
                startForeground(VpnNotificationManager.NOTIFICATION_ID, notification)
                if (state == ConnectionState.STOPPED || state == ConnectionState.FAILED) {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val config = intent.getStringExtra(EXTRA_CONFIG) ?: ""
                activeNodeName = intent.getStringExtra(EXTRA_NODE_NAME) ?: "Proxy Server"
                ConnectionStateMachine.markStarting()
                startTunnel(config)
            }
            ACTION_STOP, VpnNotificationManager.ACTION_DISCONNECT -> {
                stopTunnel()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(configJson: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                engineAdapter.start(configJson)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Tunnel startup failed: ${e.message}", e)
                ConnectionStateMachine.markFailed(e.message ?: "Tunnel failed to start")
                cleanupTun()
            }
        }
    }

    private fun stopTunnel() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                engineAdapter.stop()
            } finally {
                cleanupTun()
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun openTunInterface(options: TunOptions): Int {
        val builder = Builder()
            .setSession("BellaBox")
            .setMtu(options.mtu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        // Add IPv4 addresses
        val inet4Address = options.inet4Address
        while (inet4Address.hasNext()) {
            val addr = inet4Address.next()
            builder.addAddress(addr.address(), addr.prefix())
        }

        // Add IPv6 addresses
        val inet6Address = options.inet6Address
        while (inet6Address.hasNext()) {
            val addr = inet6Address.next()
            builder.addAddress(addr.address(), addr.prefix())
        }

        // Add DNS server
        val dnsServer = options.dnsServerAddress
        while (dnsServer.hasNext()) {
            builder.addDnsServer(dnsServer.next())
        }

        // Add default routes
        builder.addRoute("0.0.0.0", 0)
        builder.addRoute("::", 0)

        // Included / Excluded packages
        val incPkg = options.includePackage
        while (incPkg.hasNext()) {
            try { builder.addAllowedApplication(incPkg.next()) } catch (ignored: Exception) {}
        }
        val excPkg = options.excludePackage
        while (excPkg.hasNext()) {
            try { builder.addDisallowedApplication(excPkg.next()) } catch (ignored: Exception) {}
        }

        val pfd = builder.establish() ?: throw IllegalStateException("VpnService.Builder.establish() returned null")
        this.tunFd = pfd
        AppLogger.i(TAG, "TUN interface established successfully with fd: ${pfd.fd}")
        return pfd.detachFd()
    }

    private fun cleanupTun() {
        try {
            tunFd?.close()
            tunFd = null
        } catch (ignored: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try { cm.unregisterNetworkCallback(networkCallback) } catch (ignored: Exception) {}
        cleanupTun()
        AppLogger.i(TAG, "BellaVpnService destroyed")
    }

    override fun onRevoke() {
        super.onRevoke()
        AppLogger.w(TAG, "VPN permission revoked by user or system")
        stopTunnel()
    }
}
