package com.bellabox.core.network

import android.util.Base64
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class SubscriptionFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    data class FetchResult(
        val subscription: Subscription,
        val nodes: List<ProxyNode>
    )

    suspend fun fetch(subscription: Subscription): Result<FetchResult> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(subscription.url)
                .header("User-Agent", "BellaBox/1.0.0 (Android; Sing-box)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error: ${response.code} ${response.message}"))
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank()) {
                return@withContext Result.failure(Exception("Empty subscription response"))
            }

            // Parse User-Info header if present (e.g. upload=123; download=456; total=789; expire=123456)
            var upload = subscription.uploadBytes
            var download = subscription.downloadBytes
            var total = subscription.totalBytes
            var expire = subscription.expireTime

            val userInfoHeader = response.header("subscription-userinfo")
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

            // Decode body: could be base64 or plaintext urls
            val decodedContent = tryDecodeBase64(body)
            val lines = decodedContent.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val rawNodes = mutableListOf<ProxyNode>()
            for (line in lines) {
                val node = NodeUriParser.parse(line)
                if (node != null) {
                    rawNodes.add(node.copy(subscriptionId = subscription.id))
                }
            }

            // Deduplicate nodes based on structural fingerprint
            val seenFingerprints = HashSet<String>()
            val distinctNodes = mutableListOf<ProxyNode>()
            for (node in rawNodes) {
                val fp = node.computeFingerprint()
                if (seenFingerprints.add(fp)) {
                    distinctNodes.add(node)
                }
            }

            val updatedSub = subscription.copy(
                nodeCount = distinctNodes.size,
                lastUpdate = System.currentTimeMillis(),
                uploadBytes = upload,
                downloadBytes = download,
                totalBytes = total,
                expireTime = expire,
                lastStatusMessage = "Successfully updated with ${distinctNodes.size} nodes"
            )

            Result.success(FetchResult(updatedSub, distinctNodes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tryDecodeBase64(input: String): String {
        val trimmed = input.trim().replace("\r", "").replace("\n", "")
        return try {
            val bytes = Base64.decode(trimmed, Base64.DEFAULT)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            input
        }
    }
}
