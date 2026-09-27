package com.bellabox.core.network

import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ParsedUri(
    val scheme: String,
    val userInfo: String?,
    val host: String,
    val port: Int,
    val path: String,
    val queryParams: Map<String, String>,
    val fragment: String?
) {
    fun getQueryParameter(key: String): String? = queryParams[key]

    companion object {
        fun parse(uriStr: String): ParsedUri? {
            return try {
                var s = uriStr.trim()
                val schemeIdx = s.indexOf("://")
                if (schemeIdx == -1) return null
                val scheme = s.substring(0, schemeIdx).lowercase()
                s = s.substring(schemeIdx + 3)

                var fragment: String? = null
                val hashIdx = s.indexOf('#')
                if (hashIdx != -1) {
                    fragment = s.substring(hashIdx + 1)
                    s = s.substring(0, hashIdx)
                }

                val queryParams = mutableMapOf<String, String>()
                val qIdx = s.indexOf('?')
                if (qIdx != -1) {
                    val qStr = s.substring(qIdx + 1)
                    s = s.substring(0, qIdx)
                    qStr.split('&').forEach { param ->
                        val parts = param.split('=', limit = 2)
                        if (parts.size == 2) {
                            try {
                                queryParams[parts[0]] = URLDecoder.decode(parts[1], "UTF-8")
                            } catch (_: Exception) {
                                queryParams[parts[0]] = parts[1]
                            }
                        } else if (parts.isNotEmpty()) {
                            queryParams[parts[0]] = ""
                        }
                    }
                }

                var path = ""
                val slashIdx = s.indexOf('/')
                if (slashIdx != -1) {
                    path = s.substring(slashIdx)
                    s = s.substring(0, slashIdx)
                }

                var userInfo: String? = null
                val atIdx = s.lastIndexOf('@')
                if (atIdx != -1) {
                    userInfo = s.substring(0, atIdx)
                    s = s.substring(atIdx + 1)
                }

                var host = s
                var port = -1
                if (host.startsWith("[")) {
                    val closeBracket = host.indexOf(']')
                    if (closeBracket != -1) {
                        val hostPart = host.substring(1, closeBracket)
                        val portPart = host.substring(closeBracket + 1)
                        if (portPart.startsWith(":")) {
                            port = portPart.substring(1).toIntOrNull() ?: -1
                        }
                        host = hostPart
                    }
                } else if (host.contains(':')) {
                    val colonIdx = host.lastIndexOf(':')
                    val portPart = host.substring(colonIdx + 1)
                    port = portPart.toIntOrNull() ?: -1
                    host = host.substring(0, colonIdx)
                }

                ParsedUri(
                    scheme = scheme,
                    userInfo = userInfo,
                    host = host,
                    port = port,
                    path = path,
                    queryParams = queryParams,
                    fragment = fragment
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

object NodeUriParser {

    fun parse(rawUri: String): ProxyNode? {
        val trimmed = rawUri.trim()
        return try {
            when {
                trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed)
                trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed)
                trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed)
                trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed)
                trimmed.startsWith("hysteria2://", ignoreCase = true) || trimmed.startsWith("hy2://", ignoreCase = true) -> parseHysteria2(trimmed)
                trimmed.startsWith("tuic://", ignoreCase = true) -> parseTuic(trimmed)
                trimmed.startsWith("wireguard://", ignoreCase = true) || trimmed.startsWith("wg://", ignoreCase = true) -> parseWireguard(trimmed)
                trimmed.startsWith("socks5://", ignoreCase = true) || trimmed.startsWith("socks://", ignoreCase = true) -> parseSocks(trimmed)
                trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> parseHttp(trimmed)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun safeBase64Decode(encoded: String): ByteArray {
        val sanitized = encoded.trim().replace('-', '+').replace('_', '/')
        val padLength = (4 - (sanitized.length % 4)) % 4
        val padded = sanitized + "=".repeat(padLength)
        return java.util.Base64.getDecoder().decode(padded)
    }

    private fun parseVless(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val uuid = uri.userInfo ?: ""
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 443
        val name = decodeFragment(uri.fragment, "$host:$port")

        val security = uri.getQueryParameter("security") ?: "none"
        val flow = uri.getQueryParameter("flow") ?: ""
        val sni = uri.getQueryParameter("sni") ?: ""
        val fp = uri.getQueryParameter("fp") ?: "chrome"
        val pbk = uri.getQueryParameter("pbk") ?: ""
        val sid = uri.getQueryParameter("sid") ?: ""
        val type = uri.getQueryParameter("type") ?: "tcp"
        val path = uri.getQueryParameter("path") ?: ""
        val hostHeader = uri.getQueryParameter("host") ?: ""

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.VLESS,
            uuid = uuid,
            flow = flow,
            security = security,
            sni = sni,
            fingerprint = fp,
            publicKey = pbk,
            shortId = sid,
            transport = type,
            transportPath = path,
            transportHost = hostHeader
        )
    }

    private fun parseVmess(uriStr: String): ProxyNode? {
        val base64Data = uriStr.substringAfter("vmess://").trim()
        val jsonStr = try {
            String(safeBase64Decode(base64Data), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return null
        }
        val json = JSONObject(jsonStr)
        val host = json.optString("add", "")
        val port = json.optInt("port", 443)
        val id = json.optString("id", "")
        val name = json.optString("ps", "$host:$port")
        val net = json.optString("net", "tcp")
        val path = json.optString("path", "")
        val hostHeader = json.optString("host", "")
        val tls = json.optString("tls", "")
        val sni = json.optString("sni", hostHeader)

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.VMESS,
            uuid = id,
            security = if (tls.equals("tls", ignoreCase = true)) "tls" else "none",
            sni = sni,
            transport = net,
            transportPath = path,
            transportHost = hostHeader
        )
    }

    private fun parseTrojan(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val password = uri.userInfo ?: ""
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 443
        val name = decodeFragment(uri.fragment, "$host:$port")
        val sni = uri.getQueryParameter("sni") ?: host
        val type = uri.getQueryParameter("type") ?: "tcp"
        val path = uri.getQueryParameter("path") ?: ""
        val hostHeader = uri.getQueryParameter("host") ?: ""

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.TROJAN,
            password = password,
            security = "tls",
            sni = sni,
            transport = type,
            transportPath = path,
            transportHost = hostHeader
        )
    }

    private fun parseShadowsocks(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val userInfo = uri.userInfo
        val name = decodeFragment(uri.fragment, "Shadowsocks")
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 8388

        var method = "aes-128-gcm"
        var password = ""

        if (userInfo != null) {
            val decoded = try {
                String(safeBase64Decode(userInfo), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                userInfo
            }
            if (decoded.contains(":")) {
                method = decoded.substringBefore(":")
                password = decoded.substringAfter(":")
            }
        }

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.SHADOWSOCKS,
            password = password,
            flow = method
        )
    }

    private fun parseHysteria2(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val password = uri.userInfo ?: ""
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 443
        val name = decodeFragment(uri.fragment, "$host:$port")
        val sni = uri.getQueryParameter("sni") ?: host

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.HYSTERIA2,
            password = password,
            security = "tls",
            sni = sni
        )
    }

    private fun parseTuic(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val userInfo = uri.userInfo ?: ""
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 443
        val name = decodeFragment(uri.fragment, "$host:$port")
        val sni = uri.getQueryParameter("sni") ?: host

        val uuid = if (userInfo.contains(":")) userInfo.substringBefore(":") else userInfo
        val password = if (userInfo.contains(":")) userInfo.substringAfter(":") else ""

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.TUIC,
            uuid = uuid,
            password = password,
            security = "tls",
            sni = sni
        )
    }

    private fun parseWireguard(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 51820
        val name = decodeFragment(uri.fragment, "WireGuard-$host")
        val privateKey = uri.userInfo ?: uri.getQueryParameter("privatekey") ?: ""
        val publicKey = uri.getQueryParameter("publickey") ?: ""
        val presharedKey = uri.getQueryParameter("presharedkey") ?: ""

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.WIREGUARD,
            password = privateKey,
            publicKey = publicKey,
            shortId = presharedKey
        )
    }

    private fun parseSocks(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 1080
        val name = decodeFragment(uri.fragment, "SOCKS5-$host:$port")
        val userInfo = uri.userInfo ?: ""
        val user = if (userInfo.contains(":")) userInfo.substringBefore(":") else userInfo
        val pass = if (userInfo.contains(":")) userInfo.substringAfter(":") else ""

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.SOCKS,
            uuid = user,
            password = pass
        )
    }

    private fun parseHttp(uriStr: String): ProxyNode? {
        val uri = ParsedUri.parse(uriStr) ?: return null
        val host = uri.host
        val port = uri.port.takeIf { it != -1 } ?: 8080
        val name = decodeFragment(uri.fragment, "HTTP-$host:$port")
        val userInfo = uri.userInfo ?: ""
        val user = if (userInfo.contains(":")) userInfo.substringBefore(":") else userInfo
        val pass = if (userInfo.contains(":")) userInfo.substringAfter(":") else ""
        val isTls = uri.scheme.equals("https", ignoreCase = true)

        return ProxyNode(
            name = name,
            server = host,
            port = port,
            protocol = ProtocolType.HTTP,
            uuid = user,
            password = pass,
            security = if (isTls) "tls" else "none",
            sni = uri.getQueryParameter("sni") ?: host
        )
    }

    private fun decodeFragment(fragment: String?, default: String): String {
        if (fragment.isNullOrBlank()) return default
        return try {
            URLDecoder.decode(fragment, "UTF-8")
        } catch (e: Exception) {
            fragment
        }
    }
}
