package com.bellabox.core.model

enum class DnsProtocol {
    UDP,
    TCP,
    TLS, // DoT
    HTTPS, // DoH
    QUIC // DoQ
}

data class DnsServerConfig(
    val tag: String,
    val address: String,
    val protocol: DnsProtocol = DnsProtocol.HTTPS,
    val port: Int = 443,
    val detour: String? = null // null for direct, outbound tag for proxy detour
)

data class DnsConfiguration(
    val directDns: String = "https://223.5.5.5/dns-query",
    val proxyDns: String = "https://1.1.1.1/dns-query",
    val fallbackDns: String = "8.8.8.8",
    val fakeIpEnabled: Boolean = true,
    val fakeIpRange: String = "198.18.0.0/15",
    val dnsCacheEnabled: Boolean = true
)
