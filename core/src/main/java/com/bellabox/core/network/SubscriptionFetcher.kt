package com.bellabox.core.network

import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class SubscriptionFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    data class FetchResult(
        val subscription: Subscription,
        val nodes: List<ProxyNode>,
        val totalRawCount: Int,
        val successCount: Int,
        val duplicateCount: Int,
        val failureCount: Int,
        val failureReasons: List<String>
    )

    suspend fun fetch(subscription: Subscription): Result<FetchResult> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(subscription.url.trim())
                .header("User-Agent", "BellaBox/1.0.1-preview (Android; Sing-box 1.15.0-alpha.9)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP 请求失败: ${response.code} ${response.message}"))
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank()) {
                return@withContext Result.failure(Exception("订阅返回内容为空"))
            }

            // Parse User-Info header if present
            var upload = subscription.uploadBytes
            var download = subscription.downloadBytes
            var total = subscription.totalBytes
            var expire = subscription.expireTime

            val userInfoHeader = response.header("subscription-userinfo") ?: response.header("Subscription-Userinfo")
            if (userInfoHeader != null) {
                val parts = userInfoHeader.split(";")
                for (part in parts) {
                    val kv = part.trim().split("=")
                    if (kv.size == 2) {
                        val key = kv[0].trim().lowercase()
                        val value = kv[1].trim().toLongOrNull() ?: continue
                        when (key) {
                            "upload" -> upload = value
                            "download" -> download = value
                            "total" -> total = value
                            "expire" -> expire = value
                        }
                    }
                }
            }

            // Clean BOM and CRLF
            val cleanBody = body.removePrefix("\uFEFF").replace("\r\n", "\n").trim()
            val parsedNodes = mutableListOf<ProxyNode>()
            val failureReasons = mutableListOf<String>()
            var totalRawCount = 0

            // 1. Try detecting Sing-box JSON format
            if (cleanBody.startsWith("{") && cleanBody.endsWith("}")) {
                try {
                    val json = JSONObject(cleanBody)
                    if (json.has("outbounds")) {
                        val outbounds = json.getJSONArray("outbounds")
                        for (i in 0 until outbounds.length()) {
                            totalRawCount++
                            val out = outbounds.getJSONObject(i)
                            val node = parseSingboxOutbound(out, subscription.id)
                            if (node != null) {
                                parsedNodes.add(node)
                            } else {
                                failureReasons.add("跳过非代理出站: ${out.optString("type")}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Not valid singbox json, fall through
                }
            }

            // 2. Try detecting Clash YAML format
            if (parsedNodes.isEmpty() && (cleanBody.contains("proxies:") || cleanBody.contains("Proxy:"))) {
                parseClashYaml(cleanBody, subscription.id, parsedNodes, failureReasons)
                totalRawCount = parsedNodes.size + failureReasons.size
            }

            // 3. Fallback to standard base64 decoding or line-by-line URI list
            if (parsedNodes.isEmpty()) {
                val decoded = tryDecodeBase64(cleanBody)
                val lines = decoded.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") }
                totalRawCount = lines.size

                for (line in lines) {
                    val node = NodeUriParser.parse(line)
                    if (node != null) {
                        parsedNodes.add(node.copy(subscriptionId = subscription.id))
                    } else {
                        failureReasons.add("解析失败: ${line.take(30)}...")
                    }
                }
            }

            if (parsedNodes.isEmpty()) {
                return@withContext Result.failure(
                    Exception("未能解析出任何有效节点 (共检测 ${totalRawCount} 行/项，错误数: ${failureReasons.size})")
                )
            }

            // Deduplicate nodes based on structural fingerprint
            val seenFingerprints = HashSet<String>()
            val distinctNodes = mutableListOf<ProxyNode>()
            var duplicateCount = 0

            for (node in parsedNodes) {
                val fp = node.computeFingerprint()
                if (seenFingerprints.add(fp)) {
                    distinctNodes.add(node)
                } else {
                    duplicateCount++
                }
            }

            val updatedSub = subscription.copy(
                nodeCount = distinctNodes.size,
                lastUpdate = System.currentTimeMillis(),
                uploadBytes = upload,
                downloadBytes = download,
                totalBytes = total,
                expireTime = expire,
                lastStatusMessage = "成功更新: ${distinctNodes.size} 个节点 (去重 ${duplicateCount} 个，失败 ${failureReasons.size} 个)"
            )

            Result.success(
                FetchResult(
                    subscription = updatedSub,
                    nodes = distinctNodes,
                    totalRawCount = totalRawCount,
                    successCount = distinctNodes.size,
                    duplicateCount = duplicateCount,
                    failureCount = failureReasons.size,
                    failureReasons = failureReasons
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("订阅更新异常: ${e.message ?: e.toString()}"))
        }
    }

    private fun tryDecodeBase64(input: String): String {
        val sanitized = input.trim().replace("\n", "").replace("\r", "")
        return try {
            val normalized = sanitized.replace('-', '+').replace('_', '/')
            val padLength = (4 - (normalized.length % 4)) % 4
            val padded = normalized + "=".repeat(padLength)
            val bytes = java.util.Base64.getDecoder().decode(padded)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            input
        }
    }

    private fun parseSingboxOutbound(out: JSONObject, subId: Long): ProxyNode? {
        val type = out.optString("type", "").lowercase()
        val tag = out.optString("tag", "")
        val server = out.optString("server", "")
        val port = out.optInt("server_port", out.optInt("port", 443))

        val protocol = when (type) {
            "vless" -> ProtocolType.VLESS
            "vmess" -> ProtocolType.VMESS
            "trojan" -> ProtocolType.TROJAN
            "shadowsocks" -> ProtocolType.SHADOWSOCKS
            "hysteria2" -> ProtocolType.HYSTERIA2
            "tuic" -> ProtocolType.TUIC
            "wireguard" -> ProtocolType.WIREGUARD
            "socks" -> ProtocolType.SOCKS
            "http" -> ProtocolType.HTTP
            else -> return null
        }

        val tlsObj = out.optJSONObject("tls")
        val isTls = tlsObj != null && tlsObj.optBoolean("enabled", false)
        val realityObj = tlsObj?.optJSONObject("reality")
        val isReality = realityObj != null && realityObj.optBoolean("enabled", false)

        val transportObj = out.optJSONObject("transport")
        val transportType = transportObj?.optString("type", "tcp") ?: "tcp"
        val transportPath = transportObj?.optString("path", "") ?: ""

        return ProxyNode(
            subscriptionId = subId,
            name = tag.ifBlank { "$server:$port" },
            server = server,
            port = port,
            protocol = protocol,
            uuid = out.optString("uuid", ""),
            password = out.optString("password", ""),
            flow = out.optString("flow", ""),
            security = if (isReality) "reality" else if (isTls) "tls" else "none",
            sni = tlsObj?.optString("server_name", "") ?: "",
            publicKey = realityObj?.optString("public_key", "") ?: "",
            shortId = realityObj?.optString("short_id", "") ?: "",
            transport = transportType,
            transportPath = transportPath
        )
    }

    private fun parseClashYaml(yaml: String, subId: Long, outNodes: MutableList<ProxyNode>, outFailures: MutableList<String>) {
        val lines = yaml.lines()
        var inProxiesSection = false
        var currentMap = mutableMapOf<String, String>()

        fun commitCurrent() {
            if (currentMap.isNotEmpty()) {
                val node = createNodeFromClashMap(currentMap, subId)
                if (node != null) outNodes.add(node) else outFailures.add("无法识别 Clash 代理节点: ${currentMap["name"]}")
                currentMap.clear()
            }
        }

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.startsWith("proxies:") || line.startsWith("Proxy:")) {
                inProxiesSection = true
                continue
            }
            if (inProxiesSection) {
                if (line.isNotEmpty() && !line.startsWith("-") && !line.startsWith(" ") && !line.startsWith("#") && line.endsWith(":")) {
                    // Left proxies section
                    commitCurrent()
                    break
                }
                if (line.startsWith("-")) {
                    commitCurrent()
                    val content = line.removePrefix("-").trim()
                    if (content.contains(":")) {
                        val key = content.substringBefore(":").trim()
                        val value = content.substringAfter(":").trim().removeSurrounding("\"").removeSurrounding("'")
                        currentMap[key] = value
                    }
                } else if (line.contains(":")) {
                    val key = line.substringBefore(":").trim()
                    val value = line.substringAfter(":").trim().removeSurrounding("\"").removeSurrounding("'")
                    currentMap[key] = value
                }
            }
        }
        commitCurrent()
    }

    private fun createNodeFromClashMap(map: Map<String, String>, subId: Long): ProxyNode? {
        val name = map["name"] ?: return null
        val server = map["server"] ?: return null
        val port = map["port"]?.toIntOrNull() ?: 443
        val type = map["type"]?.lowercase() ?: return null

        val protocol = when (type) {
            "ss" -> ProtocolType.SHADOWSOCKS
            "vmess" -> ProtocolType.VMESS
            "trojan" -> ProtocolType.TROJAN
            "vless" -> ProtocolType.VLESS
            "hysteria2", "hy2" -> ProtocolType.HYSTERIA2
            "tuic" -> ProtocolType.TUIC
            "wireguard" -> ProtocolType.WIREGUARD
            "socks5" -> ProtocolType.SOCKS
            "http" -> ProtocolType.HTTP
            else -> return null
        }

        return ProxyNode(
            subscriptionId = subId,
            name = name,
            server = server,
            port = port,
            protocol = protocol,
            uuid = map["uuid"] ?: "",
            password = map["password"] ?: "",
            security = if (map["tls"] == "true") "tls" else "none",
            sni = map["sni"] ?: map["servername"] ?: "",
            flow = map["flow"] ?: "",
            transport = map["network"] ?: "tcp",
            transportPath = map["ws-path"] ?: map["path"] ?: ""
        )
    }
}
