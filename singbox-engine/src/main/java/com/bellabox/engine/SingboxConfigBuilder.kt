package com.bellabox.engine

import com.bellabox.core.model.DnsConfiguration
import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.RouteRule
import com.bellabox.core.model.RuleActionType
import com.bellabox.core.model.RuleType
import com.bellabox.core.model.StrategyGroup
import com.bellabox.core.model.StrategyType
import org.json.JSONArray
import org.json.JSONObject

class SingboxConfigBuilder {

    fun build(
        activeNode: ProxyNode,
        dnsConfig: DnsConfiguration = DnsConfiguration(),
        customRules: List<RouteRule> = emptyList(),
        excludedPackages: List<String> = emptyList(),
        includedPackages: List<String> = emptyList()
    ): String {
        return buildComplete(
            activeNode = activeNode,
            allNodes = listOf(activeNode),
            strategyGroups = emptyList(),
            dnsConfig = dnsConfig,
            customRules = customRules,
            excludedPackages = excludedPackages,
            includedPackages = includedPackages
        )
    }

    fun buildComplete(
        activeNode: ProxyNode?,
        allNodes: List<ProxyNode> = emptyList(),
        strategyGroups: List<StrategyGroup> = emptyList(),
        dnsConfig: DnsConfiguration = DnsConfiguration(),
        customRules: List<RouteRule> = emptyList(),
        excludedPackages: List<String> = emptyList(),
        includedPackages: List<String> = emptyList()
    ): String {
        val root = JSONObject()

        // 1. Log configuration
        root.put("log", JSONObject().apply {
            put("disabled", false)
            put("level", "info")
            put("timestamp", true)
        })

        // 2. DNS configuration (1.15.0 official modern format)
        root.put("dns", buildDnsBlock(dnsConfig))

        // 3. Inbound configuration (TUN with Sing-box 1.15 native sing-tun stack)
        root.put("inbounds", JSONArray().apply {
            put(buildTunInbound(excludedPackages, includedPackages))
        })

        // 4. Outbound configuration
        root.put("outbounds", buildOutbounds(activeNode, allNodes, strategyGroups))

        // 5. Route configuration
        root.put("route", buildRouteBlock(customRules))

        // 6. Experimental features
        root.put("experimental", JSONObject().apply {
            put("cache_file", JSONObject().apply {
                put("enabled", true)
                put("store_fakeip", true)
            })
        })

        return root.toString(2)
    }

    private fun buildDnsBlock(dnsConfig: DnsConfiguration): JSONObject {
        val dns = JSONObject()
        val servers = JSONArray()

        // Direct DNS (Domestic)
        servers.put(JSONObject().apply {
            put("tag", "dns-direct")
            put("address", dnsConfig.directDns)
            put("detour", "direct")
        })

        // Proxy DNS (Remote)
        servers.put(JSONObject().apply {
            put("tag", "dns-remote")
            put("address", dnsConfig.proxyDns)
            put("detour", "proxy")
        })

        // FakeIP DNS server
        if (dnsConfig.fakeIpEnabled) {
            servers.put(JSONObject().apply {
                put("tag", "dns-fakeip")
                put("address", "fakeip")
            })
            dns.put("fakeip", JSONObject().apply {
                put("enabled", true)
                put("inet4_range", dnsConfig.fakeIpRange)
            })
        }

        dns.put("servers", servers)

        // DNS rules
        val rules = JSONArray()
        rules.put(JSONObject().apply {
            put("outbound", JSONArray().apply { put("any") })
            put("server", if (dnsConfig.fakeIpEnabled) "dns-fakeip" else "dns-remote")
        })
        rules.put(JSONObject().apply {
            put("clash_mode", "Direct")
            put("server", "dns-direct")
        })
        rules.put(JSONObject().apply {
            put("clash_mode", "Global")
            put("server", if (dnsConfig.fakeIpEnabled) "dns-fakeip" else "dns-remote")
        })

        dns.put("rules", rules)
        dns.put("strategy", "prefer_ipv4")
        return dns
    }

    private fun buildTunInbound(
        excludedPackages: List<String>,
        includedPackages: List<String>
    ): JSONObject {
        return JSONObject().apply {
            put("type", "tun")
            put("tag", "tun-in")
            put("interface_name", "tun0")
            put("inet4_address", JSONArray().apply { put("172.19.0.1/30") })
            put("inet6_address", JSONArray().apply { put("fdfe:dcba:9876::1/126") })
            put("mtu", 9000)
            put("auto_route", true)
            put("strict_route", true)
            // stack is deliberately omitted in sing-box 1.15 to utilize native sing-tun TCP/IP stack
            put("sniff", true)
            put("sniff_override_destination", false)

            if (includedPackages.isNotEmpty()) {
                put("include_package", JSONArray(includedPackages))
            } else if (excludedPackages.isNotEmpty()) {
                put("exclude_package", JSONArray(excludedPackages))
            }
        }
    }

    private fun buildOutbounds(
        activeNode: ProxyNode?,
        allNodes: List<ProxyNode>,
        strategyGroups: List<StrategyGroup>
    ): JSONArray {
        val outbounds = JSONArray()
        val generatedNodeTags = mutableSetOf<String>()

        // 1. If active node is specified, ensure it has a primary "proxy" outbound
        if (activeNode != null) {
            val primaryOutbound = buildSingleNodeOutbound(activeNode, tag = "proxy")
            outbounds.put(primaryOutbound)
            generatedNodeTags.add("proxy")
        }

        // 2. Add individual node outbounds
        val nodesToProcess = if (allNodes.isNotEmpty()) allNodes else (if (activeNode != null) listOf(activeNode) else emptyList())
        for (node in nodesToProcess) {
            val nodeTag = "node_${node.id}"
            if (!generatedNodeTags.contains(nodeTag)) {
                outbounds.put(buildSingleNodeOutbound(node, tag = nodeTag))
                generatedNodeTags.add(nodeTag)
            }
        }

        // 3. Add strategy groups if configured
        for (group in strategyGroups) {
            val groupJson = JSONObject().apply {
                put("tag", group.tag)
                when (group.type) {
                    StrategyType.URLTEST -> {
                        put("type", "urltest")
                        put("url", group.urlTestUrl)
                        put("interval", "${group.urlTestIntervalMinutes}m")
                        put("tolerance", group.urlTestToleranceMs)
                    }
                    else -> {
                        put("type", "selector")
                        if (group.selectedNodeId != null) {
                            put("default", "node_${group.selectedNodeId}")
                        }
                    }
                }
                val memberOutbounds = JSONArray()
                if (group.nodeIds.isNotEmpty()) {
                    for (nid in group.nodeIds) {
                        memberOutbounds.put("node_$nid")
                    }
                } else {
                    for (node in nodesToProcess) {
                        memberOutbounds.put("node_${node.id}")
                    }
                }
                if (memberOutbounds.length() == 0 && activeNode != null) {
                    memberOutbounds.put("proxy")
                }
                put("outbounds", memberOutbounds)
            }
            outbounds.put(groupJson)
        }

        // 4. If neither activeNode nor individual nodes were provided, fallback to dummy direct to avoid empty outbounds
        if (outbounds.length() == 0) {
            outbounds.put(JSONObject().apply {
                put("tag", "proxy")
                put("type", "direct")
            })
        }

        // 5. System direct outbound
        outbounds.put(JSONObject().apply {
            put("tag", "direct")
            put("type", "direct")
        })

        // 6. Block outbound
        outbounds.put(JSONObject().apply {
            put("tag", "block")
            put("type", "block")
        })

        // 7. DNS-out outbound
        outbounds.put(JSONObject().apply {
            put("tag", "dns-out")
            put("type", "dns")
        })

        return outbounds
    }

    private fun buildSingleNodeOutbound(node: ProxyNode, tag: String): JSONObject {
        return JSONObject().apply {
            put("tag", tag)
            put("type", node.protocol.name.lowercase())
            put("server", node.server)
            put("port", node.port)

            when (node.protocol) {
                ProtocolType.VLESS -> {
                    put("uuid", node.uuid)
                    if (node.flow.isNotBlank()) put("flow", node.flow)
                    if (node.security == "tls" || node.security == "reality") {
                        put("tls", buildTlsBlock(node))
                    }
                    if (node.transport != "tcp") {
                        put("transport", buildTransportBlock(node))
                    }
                }
                ProtocolType.VMESS -> {
                    put("uuid", node.uuid)
                    put("security", "auto")
                    if (node.security == "tls") {
                        put("tls", buildTlsBlock(node))
                    }
                    if (node.transport != "tcp") {
                        put("transport", buildTransportBlock(node))
                    }
                }
                ProtocolType.TROJAN -> {
                    put("password", node.password)
                    put("tls", buildTlsBlock(node))
                    if (node.transport != "tcp") {
                        put("transport", buildTransportBlock(node))
                    }
                }
                ProtocolType.SHADOWSOCKS -> {
                    put("method", if (node.flow.isNotBlank()) node.flow else "aes-128-gcm")
                    put("password", node.password)
                }
                ProtocolType.HYSTERIA2 -> {
                    put("password", node.password)
                    put("tls", buildTlsBlock(node))
                }
                ProtocolType.TUIC -> {
                    if (node.uuid.isNotBlank()) put("uuid", node.uuid)
                    put("password", node.password)
                    put("congestion_control", "bbr")
                    put("tls", buildTlsBlock(node))
                }
                ProtocolType.WIREGUARD -> {
                    put("system_interface", false)
                    put("interface_name", "wg0")
                    put("local_address", JSONArray().apply { put("10.0.0.2/32") })
                    put("private_key", node.password)
                    put("peer_public_key", node.publicKey)
                }
                ProtocolType.SOCKS -> {
                    put("version", "5")
                    if (node.uuid.isNotBlank()) put("username", node.uuid)
                    if (node.password.isNotBlank()) put("password", node.password)
                }
                ProtocolType.HTTP -> {
                    if (node.uuid.isNotBlank()) put("username", node.uuid)
                    if (node.password.isNotBlank()) put("password", node.password)
                    if (node.security == "tls") {
                        put("tls", buildTlsBlock(node))
                    }
                }
            }
        }
    }

    private fun buildTlsBlock(node: ProxyNode): JSONObject {
        return JSONObject().apply {
            put("enabled", true)
            put("server_name", if (node.sni.isNotBlank()) node.sni else node.server)
            if (node.alpn.isNotEmpty()) {
                put("alpn", JSONArray(node.alpn))
            }
            if (node.security == "reality") {
                put("reality", JSONObject().apply {
                    put("enabled", true)
                    put("public_key", node.publicKey)
                    put("short_id", node.shortId)
                })
                put("utls", JSONObject().apply {
                    put("enabled", true)
                    put("fingerprint", node.fingerprint.ifBlank { "chrome" })
                })
            }
        }
    }

    private fun buildTransportBlock(node: ProxyNode): JSONObject {
        return JSONObject().apply {
            put("type", node.transport)
            if (node.transportPath.isNotBlank()) put("path", node.transportPath)
            if (node.transportHost.isNotBlank()) {
                put("headers", JSONObject().apply {
                    put("Host", node.transportHost)
                })
            }
        }
    }

    private fun buildRouteBlock(customRules: List<RouteRule>): JSONObject {
        val route = JSONObject()
        val rules = JSONArray()

        // DNS hijack rule
        rules.put(JSONObject().apply {
            put("protocol", "dns")
            put("outbound", "dns-out")
        })

        // China domains & IPs direct rule (default sensible policy)
        rules.put(JSONObject().apply {
            put("geosite", JSONArray().apply { put("cn") })
            put("outbound", "direct")
        })
        rules.put(JSONObject().apply {
            put("geoip", JSONArray().apply { put("cn") })
            put("outbound", "direct")
        })
        rules.put(JSONObject().apply {
            put("ip_is_private", true)
            put("outbound", "direct")
        })

        // Add custom rules from user settings ordered by priority
        for (custom in customRules.filter { it.isEnabled }.sortedBy { it.priority }) {
            val ruleJson = JSONObject()
            val valsArray = JSONArray(custom.values)
            when (custom.ruleType) {
                RuleType.DOMAIN -> ruleJson.put("domain", valsArray)
                RuleType.DOMAIN_SUFFIX -> ruleJson.put("domain_suffix", valsArray)
                RuleType.DOMAIN_KEYWORD -> ruleJson.put("domain_keyword", valsArray)
                RuleType.IP_CIDR -> ruleJson.put("ip_cidr", valsArray)
                RuleType.GEOIP -> ruleJson.put("geoip", valsArray)
                RuleType.GEOSITE -> ruleJson.put("geosite", valsArray)
                RuleType.PACKAGE_NAME -> ruleJson.put("package_name", valsArray)
                RuleType.PORT -> ruleJson.put("port", valsArray)
                RuleType.PROTOCOL -> ruleJson.put("protocol", valsArray)
            }
            val targetOutbound = when (custom.action) {
                RuleActionType.DIRECT -> "direct"
                RuleActionType.BLOCK -> "block"
                RuleActionType.PROXY -> custom.targetOutboundTag.ifBlank { "proxy" }
            }
            ruleJson.put("outbound", targetOutbound)
            rules.put(ruleJson)
        }

        route.put("rules", rules)
        route.put("auto_detect_interface", true)
        return route
    }
}
