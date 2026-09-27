package com.bellabox.core.model

enum class RuleActionType(val label: String) {
    DIRECT("Direct"),
    PROXY("Proxy"),
    BLOCK("Block")
}

enum class RuleType(val label: String) {
    DOMAIN("Domain"),
    DOMAIN_SUFFIX("Domain Suffix"),
    DOMAIN_KEYWORD("Domain Keyword"),
    IP_CIDR("IP CIDR"),
    GEOIP("GeoIP"),
    GEOSITE("Geosite"),
    PACKAGE_NAME("App Package"),
    PORT("Port"),
    PROTOCOL("Protocol")
}

data class RouteRule(
    val id: Long = 0,
    val name: String,
    val ruleType: RuleType,
    val values: List<String>,
    val action: RuleActionType,
    val targetOutboundTag: String = "",
    val isEnabled: Boolean = true,
    val priority: Int = 0
)
