package com.bellabox.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PublicIpInfo(
    val ip: String,
    val country: String = "",
    val city: String = "",
    val isp: String = "",
    val asn: String = "",
    val isProxy: Boolean = false
)

class IpInfoProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    suspend fun getIpInfo(): Result<PublicIpInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://ipwho.is/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    if (json.optBoolean("success", true)) {
                        val ip = json.optString("ip", "")
                        val country = json.optString("country", "")
                        val city = json.optString("city", "")
                        val connection = json.optJSONObject("connection")
                        val isp = connection?.optString("isp", "") ?: ""
                        val asn = connection?.optString("asn", "") ?: ""
                        return@withContext Result.success(PublicIpInfo(ip, country, city, isp, asn))
                    }
                }
            }

            // Fallback to ipify if ipwho.is is unreachable
            val fallbackReq = Request.Builder().url("https://api.ipify.org?format=json").build()
            client.newCall(fallbackReq).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val ip = json.optString("ip", "")
                    return@withContext Result.success(PublicIpInfo(ip = ip))
                }
            }

            Result.failure(Exception("Failed to resolve public IP info"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
