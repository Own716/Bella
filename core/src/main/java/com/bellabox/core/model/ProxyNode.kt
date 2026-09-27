package com.bellabox.core.model

enum class ProtocolType(val label: String, val defaultPort: Int) {
    VLESS("VLESS", 443),
    VMESS("VMess", 443),
    TROJAN("Trojan", 443),
    SHADOWSOCKS("Shadowsocks", 8388),
    HYSTERIA2("Hysteria 2", 443),
    TUIC("TUIC", 443),
    WIREGUARD("WireGuard", 51820),
    SOCKS("SOCKS5", 1080),
    HTTP("HTTP", 8080);

    companion object {
        fun fromString(value: String): ProtocolType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: VLESS
        }
    }
}

enum class ConnectionState {
    IDLE,
    STARTING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    STOPPING,
    STOPPED,
    FAILED;

    val isTransitioning: Boolean
        get() = this == STARTING || this == CONNECTING || this == RECONNECTING || this == STOPPING

    val isConnected: Boolean
        get() = this == CONNECTED
}

data class ProxyNode(
    val id: Long = 0,
    val subscriptionId: Long? = null,
    val name: String,
    val server: String,
    val port: Int,
    val protocol: ProtocolType,
    val uuid: String = "",
    val password: String = "",
    val flow: String = "",
    val security: String = "tls", // "none", "tls", "reality"
    val sni: String = "",
    val alpn: List<String> = emptyList(),
    val fingerprint: String = "chrome",
    val publicKey: String = "",
    val shortId: String = "",
    val transport: String = "tcp", // "tcp", "ws", "grpc", "h2"
    val transportPath: String = "",
    val transportHost: String = "",
    val isFavorite: Boolean = false,
    val latencyMs: Long = -1,
    val lastTestTime: Long = 0,
    val qualityScore: Int = -1,
    val isAvailable: Boolean = true
) {
    /**
     * Compute a structural fingerprint for deduplication.
     * Prevents duplicate nodes from the same or different subscriptions.
     */
    fun computeFingerprint(): String {
        return "${protocol.name}:${server.lowercase()}:$port:${uuid.ifEmpty { password }}:$sni:$transport:$transportPath:$publicKey:$shortId"
    }
}
