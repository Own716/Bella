package com.bellabox.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class CoreVersionInfo(
    val currentVersion: String = SingboxEngineAdapter.CORE_VERSION,
    val currentCommit: String = SingboxEngineAdapter.CORE_COMMIT,
    val currentChannel: String = SingboxEngineAdapter.CORE_CHANNEL,
    val latestAlphaTag: String? = null,
    val hasNewerAlpha: Boolean = false,
    val updateNotes: String? = null
)

class CoreVersionChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    suspend fun checkOfficialReleases(): Result<CoreVersionInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/SagerNet/sing-box/releases?per_page=5")
                .header("User-Agent", "BellaBox-CoreChecker")
                .build()

            val info = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    CoreVersionInfo()
                } else {
                    val body = response.body?.string() ?: "[]"
                    val releases = JSONArray(body)
                    var latestTag: String? = null
                    var notes: String? = null

                    for (i in 0 until releases.length()) {
                        val item = releases.getJSONObject(i)
                        val tag = item.optString("tag_name", "")
                        val isPrerelease = item.optBoolean("prerelease", false)
                        if (isPrerelease && tag.contains("alpha", ignoreCase = true)) {
                            latestTag = tag.removePrefix("v")
                            notes = item.optString("body", "")
                            break
                        }
                    }

                    val hasNewer = latestTag != null && latestTag != SingboxEngineAdapter.CORE_VERSION
                    CoreVersionInfo(
                        latestAlphaTag = latestTag,
                        hasNewerAlpha = hasNewer,
                        updateNotes = notes
                    )
                }
            }
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
