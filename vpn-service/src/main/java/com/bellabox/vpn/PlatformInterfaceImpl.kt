package com.bellabox.vpn

import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import com.bellabox.core.common.AppLogger
import io.nekohasekai.libbox.AutoRedirectHandler
import io.nekohasekai.libbox.AutoRedirectSession
import io.nekohasekai.libbox.BridgeOptions
import io.nekohasekai.libbox.BridgeSession
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NeighborUpdateListener
import io.nekohasekai.libbox.NetworkInterface
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.PlatformUser
import io.nekohasekai.libbox.ShellSession
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.net.InetSocketAddress

open class PlatformInterfaceImpl(
    private val connectivityManager: ConnectivityManager
) : PlatformInterface {

    companion object {
        private const val TAG = "PlatformInterfaceImpl"
    }

    class SimpleStringIterator(private val list: List<String>) : StringIterator {
        private var index = 0
        override fun len(): Int = list.size
        override fun hasNext(): Boolean = index < list.size
        override fun next(): String = list[index++]
    }

    class SimpleInterfaceIterator(private val list: List<NetworkInterface>) : NetworkInterfaceIterator {
        private var index = 0
        override fun hasNext(): Boolean = index < list.size
        override fun next(): NetworkInterface = list[index++]
    }

    override fun localDNSTransport(): LocalDNSTransport? = null

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true

    override fun autoDetectInterfaceControl(fd: Int) {
        // Will be overridden by VpnService.protect(fd)
    }

    override fun openTun(options: TunOptions): Int {
        // Will be overridden by VpnService.establish()
        return -1
    }

    override fun useProcFS(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): ConnectionOwner {
        val owner = ConnectionOwner()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val uid = connectivityManager.getConnectionOwnerUid(
                    ipProtocol,
                    InetSocketAddress(sourceAddress, sourcePort),
                    InetSocketAddress(destinationAddress, destinationPort)
                )
                if (uid != Process.INVALID_UID) {
                    owner.userId = uid
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "findConnectionOwner error: ${e.message}")
            }
        }
        return owner
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {}
    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {}

    override fun getInterfaces(): NetworkInterfaceIterator {
        val interfaces = mutableListOf<NetworkInterface>()
        try {
            val netInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (netInterfaces.hasMoreElements()) {
                val nif = netInterfaces.nextElement()
                val boxIf = NetworkInterface().apply {
                    name = nif.name
                    index = nif.index
                    mtu = try { nif.mtu } catch (e: Exception) { 1500 }
                }
                interfaces.add(boxIf)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Error enumerating interfaces: ${e.message}")
        }
        return SimpleInterfaceIterator(interfaces)
    }

    override fun underNetworkExtension(): Boolean = false
    override fun includeAllNetworks(): Boolean = false
    override fun readWIFIState(): WIFIState? = null
    override fun clearDNSCache() {}
    override fun sendNotification(notification: Notification) {}
    override fun cancelNotification(identifier: String, typeID: Int) {}
    override fun startNeighborMonitor(listener: NeighborUpdateListener) {}
    override fun closeNeighborMonitor(listener: NeighborUpdateListener) {}
    override fun registerMyInterface(name: String) {}
    override fun usePlatformShell(): Boolean = false
    override fun checkPlatformShell() {}
    override fun openShellSession(user: PlatformUser, command: String, environ: StringIterator, term: String, rows: Int, cols: Int): ShellSession? = null
    override fun lookupUser(username: String): PlatformUser? = null
    override fun lookupSFTPServer(): String = ""
    override fun readSystemSSHHostKey(): String = ""
    override fun tailscaleHostname(): String = "BellaBox"
    override fun usePlatformBridge(): Boolean = false
    override fun createBridge(options: BridgeOptions): BridgeSession? = null
    override fun usePlatformAutoRedirect(): Boolean = false
    override fun createAutoRedirect(options: ByteArray, handler: AutoRedirectHandler): AutoRedirectSession? = null
}
