package com.bellabox.core.network

import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.SpeedTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class SpeedTestEngine(
    private val maxConcurrency: Int = 3
) {
    private val semaphore = Semaphore(maxConcurrency)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun testNode(
        node: ProxyNode,
        probeUrl: String = "https://www.gstatic.com/generate_204"
    ): SpeedTestResult = withContext(Dispatchers.IO) {
        semaphore.withPermit {
            var tcpMs = -1L
            var tlsMs = -1L
            var httpMs = -1L
            var downloadBps = 0L

            try {
                // 1. Phase 1: Real TCP Connect measurement
                val tcpStart = System.nanoTime()
                val socket = Socket()
                try {
                    socket.connect(InetSocketAddress(node.server, node.port), 4000)
                    tcpMs = (System.nanoTime() - tcpStart) / 1_000_000
                } finally {
                    try { socket.close() } catch (ignored: Exception) {}
                }

                // 2. Phase 2: Real TLS Handshake measurement (if applicable)
                if (node.security == "tls" || node.security == "reality" || node.port == 443) {
                    val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                    val sslSocket = sslFactory.createSocket() as SSLSocket
                    try {
                        val serverSni = node.sni.ifBlank { node.server }
                        sslSocket.sslParameters = SSLParameters().apply {
                            serverNames = listOf(SNIHostName(serverSni))
                        }
                        val tlsStart = System.nanoTime()
                        sslSocket.connect(InetSocketAddress(node.server, node.port), 4000)
                        sslSocket.startHandshake()
                        tlsMs = (System.nanoTime() - tlsStart) / 1_000_000
                    } finally {
                        try { sslSocket.close() } catch (ignored: Exception) {}
                    }
                }

                // 3. Phase 3: HTTP Delay (end-to-end latency)
                val httpStart = System.nanoTime()
                val request = Request.Builder().url(probeUrl).build()
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 204) {
                        httpMs = (System.nanoTime() - httpStart) / 1_000_000
                    }
                }

                // 4. Compute transparent, explainable quality score
                val effectiveHttp = if (httpMs > 0) httpMs else tcpMs
                val (score, breakdown) = SpeedTestResult.computeQuality(
                    tcpMs = tcpMs,
                    tlsMs = tlsMs,
                    httpMs = effectiveHttp,
                    successRate = 1.0f,
                    downloadBps = downloadBps
                )

                SpeedTestResult(
                    nodeId = node.id,
                    tcpHandshakeMs = tcpMs,
                    tlsHandshakeMs = tlsMs,
                    httpDelayMs = effectiveHttp,
                    downloadSpeedBytesPerSec = downloadBps,
                    isSuccess = true,
                    qualityScore = score,
                    scoreBreakdown = breakdown
                )
            } catch (e: Exception) {
                SpeedTestResult(
                    nodeId = node.id,
                    isSuccess = false,
                    errorMessage = e.message ?: "Connection timed out",
                    qualityScore = 0,
                    scoreBreakdown = listOf("Failed: ${e.message ?: "Timeout"}")
                )
            }
        }
    }
}
