package com.bellabox.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bellabox.app.ui.i18n.LocalAppStrings
import com.bellabox.app.ui.theme.BellaShapes
import com.bellabox.app.ui.theme.LatencyLowColor
import com.bellabox.core.network.IpInfoProvider
import com.bellabox.core.network.PublicIpInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    val s = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()
    val ipInfoProvider = remember { IpInfoProvider() }

    // Public IP state
    var publicIpInfo by remember { mutableStateOf<PublicIpInfo?>(null) }
    var isCheckingIp by remember { mutableStateOf(false) }

    // TCP Ping state
    var pingHost by remember { mutableStateOf("1.1.1.1") }
    var pingPort by remember { mutableStateOf("443") }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    // DNS Lookup state
    var dnsHost by remember { mutableStateOf("google.com") }
    var dnsResult by remember { mutableStateOf<String?>(null) }
    var isResolvingDns by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Build,
                contentDescription = s.toolsTitle,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = s.toolsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = s.toolsSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Public Outbound IP & Geo Card
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Public, contentDescription = "IP", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(s.toolsPublicIpTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isCheckingIp = true
                            coroutineScope.launch {
                                val result = ipInfoProvider.getIpInfo()
                                publicIpInfo = result.getOrNull()
                                isCheckingIp = false
                            }
                        },
                        shape = BellaShapes.small,
                        enabled = !isCheckingIp
                    ) {
                        if (isCheckingIp) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(s.toolsCheckIp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                if (publicIpInfo != null) {
                    Text(
                        text = publicIpInfo!!.ip,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${s.toolsLocation}: ${publicIpInfo!!.city}, ${publicIpInfo!!.country}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${s.toolsIsp}: ${publicIpInfo!!.isp} (${publicIpInfo!!.asn})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = s.toolsPublicIpDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Real TCP Ping Card
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.NetworkCheck, contentDescription = "TCP Ping", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s.toolsTcpPingTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pingHost,
                        onValueChange = { pingHost = it },
                        label = { Text(s.toolsHost) },
                        shape = BellaShapes.small,
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pingPort,
                        onValueChange = { pingPort = it },
                        label = { Text(s.toolsPort) },
                        shape = BellaShapes.small,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        isPinging = true
                        pingResult = null
                        coroutineScope.launch(Dispatchers.IO) {
                            val start = System.nanoTime()
                            val socket = Socket()
                            try {
                                val p = pingPort.toIntOrNull() ?: 443
                                socket.connect(InetSocketAddress(pingHost, p), 3000)
                                val duration = (System.nanoTime() - start) / 1_000_000
                                pingResult = s.toolsPingSuccess(duration)
                            } catch (e: Exception) {
                                pingResult = "${s.toolsPingFailed}: ${e.message}"
                            } finally {
                                try { socket.close() } catch (ignored: Exception) {}
                                isPinging = false
                            }
                        }
                    },
                    shape = BellaShapes.small,
                    enabled = !isPinging,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(s.toolsStartPing)
                    }
                }

                if (pingResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pingResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pingResult!!.contains("ms")) LatencyLowColor else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. DNS Lookup Card
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Dns, contentDescription = "DNS", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s.toolsDnsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = dnsHost,
                    onValueChange = { dnsHost = it },
                    label = { Text(s.toolsQueryDomain) },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        isResolvingDns = true
                        dnsResult = null
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val ips = InetAddress.getAllByName(dnsHost)
                                dnsResult = ips.joinToString("\n") { "${it.hostAddress} (${if (it.address.size == 4) "IPv4" else "IPv6"})" }
                            } catch (e: Exception) {
                                dnsResult = "${s.toolsQueryFailed}: ${e.message}"
                            } finally {
                                isResolvingDns = false
                            }
                        }
                    },
                    shape = BellaShapes.small,
                    enabled = !isResolvingDns,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isResolvingDns) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(s.toolsStartLookup)
                    }
                }

                if (dnsResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dnsResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
