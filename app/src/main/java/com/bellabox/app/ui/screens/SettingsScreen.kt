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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VpnLock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bellabox.app.ui.theme.BellaShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()

    val autoConnectOnBoot by viewModel.autoConnectOnBoot.collectAsState()
    val autoReconnectNet by viewModel.autoReconnectNetworkChange.collectAsState()

    val tunMtu by viewModel.tunMtu.collectAsState()
    val tunStrictRoute by viewModel.tunStrictRoute.collectAsState()
    val bypassLan by viewModel.bypassLan.collectAsState()

    val fakeIpEnabled by viewModel.fakeIpEnabled.collectAsState()
    val dnsDirect by viewModel.dnsDirect.collectAsState()
    val dnsRemote by viewModel.dnsRemote.collectAsState()

    val speedTestUrl by viewModel.speedTestUrl.collectAsState()
    val speedTestTimeout by viewModel.speedTestTimeoutSec.collectAsState()
    val speedTestConcurrency by viewModel.speedTestConcurrency.collectAsState()

    var themeExpanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
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
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "系统与个性化",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Sing-box 1.15 内核参数、网络策略与界面偏好",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Appearance
        SectionTitle(title = "界面与外观")
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Theme Mode Selector
                ExposedDropdownMenuBox(
                    expanded = themeExpanded,
                    onExpandedChange = { themeExpanded = it }
                ) {
                    val themeLabel = when (themeMode) {
                        "light" -> "浅色模式"
                        "dark" -> "深色模式"
                        else -> "跟随系统"
                    }
                    OutlinedTextField(
                        value = themeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("主题外观") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("跟随系统") },
                            onClick = {
                                viewModel.setThemeMode("system")
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("浅色模式") },
                            onClick = {
                                viewModel.setThemeMode("light")
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("深色模式") },
                            onClick = {
                                viewModel.setThemeMode("dark")
                                themeExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language Selector
                ExposedDropdownMenuBox(
                    expanded = langExpanded,
                    onExpandedChange = { langExpanded = it }
                ) {
                    val langLabel = when (language) {
                        "en" -> "English"
                        "ja" -> "日本語"
                        "zh-TW" -> "繁體中文"
                        else -> "简体中文 (默认)"
                    }
                    OutlinedTextField(
                        value = langLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("界面语言") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("简体中文 (默认)") },
                            onClick = {
                                viewModel.setLanguage("zh")
                                langExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                viewModel.setLanguage("en")
                                langExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("繁體中文") },
                            onClick = {
                                viewModel.setLanguage("zh-TW")
                                langExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("日本語") },
                            onClick = {
                                viewModel.setLanguage("ja")
                                langExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = "动态主题色 (Material You)",
                    subtitle = "提取系统壁纸主题色，自适应全局控件",
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Network & Connection
        SectionTitle(title = "连接与自动化")
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = "开机自启连接",
                    subtitle = "设备启动完毕后自动拉起安全隧道",
                    checked = autoConnectOnBoot,
                    onCheckedChange = { viewModel.setAutoConnectOnBoot(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = "网络切换自动重连",
                    subtitle = "在 Wi-Fi 与移动网络漫游时自动重连",
                    checked = autoReconnectNet,
                    onCheckedChange = { viewModel.setAutoReconnectNetworkChange(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Sing-box TUN
        SectionTitle(title = "TUN 虚拟网卡")
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TCP/IP 网络协议栈", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "Sing-box 1.15 原生 TCP/IP 栈 (无系统栈开销)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tunMtu.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setTunMtu(v) } },
                    label = { Text("TUN MTU 大小") },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = "严格路由 (Strict Route)",
                    subtitle = "拦截并丢弃非目标网卡的直连流量以防泄露",
                    checked = tunStrictRoute,
                    onCheckedChange = { viewModel.setTunStrictRoute(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = "绕过局域网 (Bypass LAN)",
                    subtitle = "私有 IP (192.168.x / 10.x / 172.16.x) 保持直连",
                    checked = bypassLan,
                    onCheckedChange = { viewModel.setBypassLan(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: DNS
        SectionTitle(title = "DNS 与域名解析")
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = "FakeIP 模式",
                    subtitle = "分配保留虚拟 IP，极大减少远端 DNS 解析往返耗时",
                    checked = fakeIpEnabled,
                    onCheckedChange = { viewModel.setFakeIpEnabled(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dnsDirect,
                    onValueChange = { viewModel.setDnsDirect(it) },
                    label = { Text("国内直连 DNS") },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dnsRemote,
                    onValueChange = { viewModel.setDnsRemote(it) },
                    label = { Text("远端代理 DNS") },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Speed Test Settings
        SectionTitle(title = "测速与并发评估")
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = speedTestUrl,
                    onValueChange = { viewModel.setSpeedTestUrl(it) },
                    label = { Text("测速目标 URL") },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = speedTestTimeout.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setSpeedTestTimeoutSec(v) } },
                        label = { Text("超时时间(秒)") },
                        shape = BellaShapes.small,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = speedTestConcurrency.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setSpeedTestConcurrency(v) } },
                        label = { Text("最大并发数") },
                        shape = BellaShapes.small,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Card Entry
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onNavigateToAbout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "About",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("关于 BellaBox", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("查看版本、内核状态、实时运行日志与开源许可", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
