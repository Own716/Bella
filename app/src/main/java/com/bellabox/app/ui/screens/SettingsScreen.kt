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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
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
import com.bellabox.app.ui.i18n.LocalAppStrings
import com.bellabox.app.ui.theme.BellaShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current

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
                imageVector = Icons.Rounded.Settings,
                contentDescription = s.settingsTitle,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = s.settingsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = s.settingsSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Appearance
        SectionTitle(title = s.settingsAppearance)
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
                        "light" -> s.settingsThemeLight
                        "dark" -> s.settingsThemeDark
                        else -> s.settingsThemeSystem
                    }
                    OutlinedTextField(
                        value = themeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.settingsThemeMode) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.settingsThemeSystem) },
                            onClick = {
                                viewModel.setThemeMode("system")
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(s.settingsThemeLight) },
                            onClick = {
                                viewModel.setThemeMode("light")
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(s.settingsThemeDark) },
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
                    val langLabel = if (language == "en") s.settingsLangEn else s.settingsLangZh
                    OutlinedTextField(
                        value = langLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.settingsLanguage) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.settingsLangZh) },
                            onClick = {
                                viewModel.setLanguage("zh")
                                langExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(s.settingsLangEn) },
                            onClick = {
                                viewModel.setLanguage("en")
                                langExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = s.settingsDynamicColor,
                    subtitle = s.settingsDynamicColorDesc,
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Network & Connection
        SectionTitle(title = s.settingsConnection)
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = s.settingsAutoConnectBoot,
                    subtitle = s.settingsAutoConnectBootDesc,
                    checked = autoConnectOnBoot,
                    onCheckedChange = { viewModel.setAutoConnectOnBoot(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = s.settingsAutoReconnectNet,
                    subtitle = s.settingsAutoReconnectNetDesc,
                    checked = autoReconnectNet,
                    onCheckedChange = { viewModel.setAutoReconnectNetworkChange(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Sing-box TUN
        SectionTitle(title = s.settingsTunStack)
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
                        Text(s.settingsTunStackTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            s.settingsTunStackDesc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tunMtu.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setTunMtu(v) } },
                    label = { Text(s.settingsTunMtu) },
                    supportingText = { Text(s.settingsTunMtuDesc) },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = s.settingsTunStrictRoute,
                    subtitle = s.settingsTunStrictRouteDesc,
                    checked = tunStrictRoute,
                    onCheckedChange = { viewModel.setTunStrictRoute(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    title = s.settingsBypassLan,
                    subtitle = s.settingsBypassLanDesc,
                    checked = bypassLan,
                    onCheckedChange = { viewModel.setBypassLan(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: DNS
        SectionTitle(title = s.settingsDns)
        Card(
            shape = BellaShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = s.settingsFakeIp,
                    subtitle = s.settingsFakeIpDesc,
                    checked = fakeIpEnabled,
                    onCheckedChange = { viewModel.setFakeIpEnabled(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dnsDirect,
                    onValueChange = { viewModel.setDnsDirect(it) },
                    label = { Text(s.settingsDnsDirect) },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dnsRemote,
                    onValueChange = { viewModel.setDnsRemote(it) },
                    label = { Text(s.settingsDnsRemote) },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Speed Test Settings
        SectionTitle(title = s.settingsSpeedTest)
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
                    label = { Text(s.settingsSpeedTestUrl) },
                    shape = BellaShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = speedTestTimeout.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setSpeedTestTimeoutSec(v) } },
                        label = { Text(s.settingsSpeedTestTimeout) },
                        shape = BellaShapes.small,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = speedTestConcurrency.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setSpeedTestConcurrency(v) } },
                        label = { Text(s.settingsSpeedTestConcurrency) },
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
                    contentDescription = s.settingsAboutKernel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(s.settingsAboutKernel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(s.settingsAboutKernelDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
