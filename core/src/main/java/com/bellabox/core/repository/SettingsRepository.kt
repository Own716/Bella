package com.bellabox.core.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("bellabox_settings", Context.MODE_PRIVATE)

    // Appearance
    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "zh") ?: "zh") // Default Chinese
    val language: StateFlow<String> = _language.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    // Connection
    private val _autoConnectOnBoot = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CONNECT_BOOT, false))
    val autoConnectOnBoot: StateFlow<Boolean> = _autoConnectOnBoot.asStateFlow()

    private val _autoReconnectNetworkChange = MutableStateFlow(prefs.getBoolean(KEY_AUTO_RECONNECT_NET, true))
    val autoReconnectNetworkChange: StateFlow<Boolean> = _autoReconnectNetworkChange.asStateFlow()

    private val _reconnectIntervalSeconds = MutableStateFlow(prefs.getInt(KEY_RECONNECT_INTERVAL, 3))
    val reconnectIntervalSeconds: StateFlow<Int> = _reconnectIntervalSeconds.asStateFlow()

    // TUN Settings (1.15 native stack)
    private val _tunEnabled = MutableStateFlow(prefs.getBoolean(KEY_TUN_ENABLED, true))
    val tunEnabled: StateFlow<Boolean> = _tunEnabled.asStateFlow()

    private val _tunMtu = MutableStateFlow(prefs.getInt(KEY_TUN_MTU, 9000))
    val tunMtu: StateFlow<Int> = _tunMtu.asStateFlow()

    private val _tunStrictRoute = MutableStateFlow(prefs.getBoolean(KEY_TUN_STRICT_ROUTE, true))
    val tunStrictRoute: StateFlow<Boolean> = _tunStrictRoute.asStateFlow()

    private val _tunAutoRoute = MutableStateFlow(prefs.getBoolean(KEY_TUN_AUTO_ROUTE, true))
    val tunAutoRoute: StateFlow<Boolean> = _tunAutoRoute.asStateFlow()

    private val _tunDnsHijack = MutableStateFlow(prefs.getBoolean(KEY_TUN_DNS_HIJACK, true))
    val tunDnsHijack: StateFlow<Boolean> = _tunDnsHijack.asStateFlow()

    private val _tunIpv4Address = MutableStateFlow(prefs.getString(KEY_TUN_IPV4, "172.19.0.1/30") ?: "172.19.0.1/30")
    val tunIpv4Address: StateFlow<String> = _tunIpv4Address.asStateFlow()

    private val _tunIpv6Address = MutableStateFlow(prefs.getString(KEY_TUN_IPV6, "fdfe:dcba:9876::1/126") ?: "fdfe:dcba:9876::1/126")
    val tunIpv6Address: StateFlow<String> = _tunIpv6Address.asStateFlow()

    // DNS Settings
    private val _dnsMode = MutableStateFlow(prefs.getString(KEY_DNS_MODE, "fakeip") ?: "fakeip")
    val dnsMode: StateFlow<String> = _dnsMode.asStateFlow()

    private val _fakeIpEnabled = MutableStateFlow(prefs.getBoolean(KEY_FAKE_IP_ENABLED, true))
    val fakeIpEnabled: StateFlow<Boolean> = _fakeIpEnabled.asStateFlow()

    private val _fakeIpRange = MutableStateFlow(prefs.getString(KEY_FAKE_IP_RANGE, "198.18.0.0/15") ?: "198.18.0.0/15")
    val fakeIpRange: StateFlow<String> = _fakeIpRange.asStateFlow()

    private val _directDns = MutableStateFlow(prefs.getString(KEY_DIRECT_DNS, "223.5.5.5") ?: "223.5.5.5")
    val directDns: StateFlow<String> = _directDns.asStateFlow()
    val dnsDirect: StateFlow<String> = _directDns.asStateFlow()

    private val _remoteDns = MutableStateFlow(prefs.getString(KEY_REMOTE_DNS, "https://1.1.1.1/dns-query") ?: "https://1.1.1.1/dns-query")
    val remoteDns: StateFlow<String> = _remoteDns.asStateFlow()
    val dnsRemote: StateFlow<String> = _remoteDns.asStateFlow()

    private val _optimisticDns = MutableStateFlow(prefs.getBoolean(KEY_OPTIMISTIC_DNS, true))
    val optimisticDns: StateFlow<Boolean> = _optimisticDns.asStateFlow()

    // Routing
    private val _defaultRouteMode = MutableStateFlow(prefs.getString(KEY_DEFAULT_ROUTE_MODE, "rule") ?: "rule")
    val defaultRouteMode: StateFlow<String> = _defaultRouteMode.asStateFlow()
    val routingMode: StateFlow<String> = _defaultRouteMode.asStateFlow()

    private val _bypassLan = MutableStateFlow(prefs.getBoolean(KEY_BYPASS_LAN, true))
    val bypassLan: StateFlow<Boolean> = _bypassLan.asStateFlow()

    private val _bypassChina = MutableStateFlow(prefs.getBoolean(KEY_BYPASS_CHINA, true))
    val bypassChina: StateFlow<Boolean> = _bypassChina.asStateFlow()

    // Speed Test
    private val _speedTestUrl = MutableStateFlow(prefs.getString(KEY_SPEED_TEST_URL, "https://www.gstatic.com/generate_204") ?: "https://www.gstatic.com/generate_204")
    val speedTestUrl: StateFlow<String> = _speedTestUrl.asStateFlow()

    private val _speedTestTimeoutMs = MutableStateFlow(prefs.getInt(KEY_SPEED_TEST_TIMEOUT, 3000))
    val speedTestTimeoutMs: StateFlow<Int> = _speedTestTimeoutMs.asStateFlow()
    val speedTestTimeoutSec = MutableStateFlow(prefs.getInt(KEY_SPEED_TEST_TIMEOUT, 3000) / 1000)

    private val _speedTestConcurrency = MutableStateFlow(prefs.getInt(KEY_SPEED_TEST_CONCURRENCY, 4))
    val speedTestConcurrency: StateFlow<Int> = _speedTestConcurrency.asStateFlow()

    // Core
    private val _coreLogLevel = MutableStateFlow(prefs.getString(KEY_CORE_LOG_LEVEL, "info") ?: "info")
    val coreLogLevel: StateFlow<String> = _coreLogLevel.asStateFlow()

    // Selected Active Node
    private val _selectedNodeId = MutableStateFlow(prefs.getLong(KEY_SELECTED_NODE_ID, -1L))
    val selectedNodeId: StateFlow<Long> = _selectedNodeId.asStateFlow()

    // Setters
    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    fun setAutoConnectOnBoot(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT_BOOT, enabled).apply()
        _autoConnectOnBoot.value = enabled
    }

    fun setAutoReconnectNetworkChange(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT_NET, enabled).apply()
        _autoReconnectNetworkChange.value = enabled
    }

    fun setTunEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TUN_ENABLED, enabled).apply()
        _tunEnabled.value = enabled
    }

    fun setTunMtu(mtu: Int) {
        prefs.edit().putInt(KEY_TUN_MTU, mtu).apply()
        _tunMtu.value = mtu
    }

    fun setTunStrictRoute(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TUN_STRICT_ROUTE, enabled).apply()
        _tunStrictRoute.value = enabled
    }

    fun setTunDnsHijack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TUN_DNS_HIJACK, enabled).apply()
        _tunDnsHijack.value = enabled
    }

    fun setDnsMode(mode: String) {
        prefs.edit().putString(KEY_DNS_MODE, mode).apply()
        _dnsMode.value = mode
    }

    fun setFakeIpEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FAKE_IP_ENABLED, enabled).apply()
        _fakeIpEnabled.value = enabled
    }

    fun setFakeIpRange(range: String) {
        prefs.edit().putString(KEY_FAKE_IP_RANGE, range).apply()
        _fakeIpRange.value = range
    }

    fun setDirectDns(dns: String) {
        prefs.edit().putString(KEY_DIRECT_DNS, dns).apply()
        _directDns.value = dns
    }
    fun setDnsDirect(dns: String) = setDirectDns(dns)

    fun setRemoteDns(dns: String) {
        prefs.edit().putString(KEY_REMOTE_DNS, dns).apply()
        _remoteDns.value = dns
    }
    fun setDnsRemote(dns: String) = setRemoteDns(dns)

    fun setDefaultRouteMode(mode: String) {
        prefs.edit().putString(KEY_DEFAULT_ROUTE_MODE, mode).apply()
        _defaultRouteMode.value = mode
    }
    fun setRoutingMode(mode: String) = setDefaultRouteMode(mode)

    fun setBypassLan(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BYPASS_LAN, enabled).apply()
        _bypassLan.value = enabled
    }

    fun setBypassChina(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BYPASS_CHINA, enabled).apply()
        _bypassChina.value = enabled
    }

    fun setSpeedTestUrl(url: String) {
        prefs.edit().putString(KEY_SPEED_TEST_URL, url).apply()
        _speedTestUrl.value = url
    }

    fun setSpeedTestTimeoutMs(timeout: Int) {
        prefs.edit().putInt(KEY_SPEED_TEST_TIMEOUT, timeout).apply()
        _speedTestTimeoutMs.value = timeout
        speedTestTimeoutSec.value = timeout / 1000
    }
    fun setSpeedTestTimeoutSec(sec: Int) = setSpeedTestTimeoutMs(sec * 1000)

    fun setSpeedTestConcurrency(concurrency: Int) {
        prefs.edit().putInt(KEY_SPEED_TEST_CONCURRENCY, concurrency).apply()
        _speedTestConcurrency.value = concurrency
    }

    fun setCoreLogLevel(level: String) {
        prefs.edit().putString(KEY_CORE_LOG_LEVEL, level).apply()
        _coreLogLevel.value = level
    }

    fun setSelectedNodeId(nodeId: Long) {
        prefs.edit().putLong(KEY_SELECTED_NODE_ID, nodeId).apply()
        _selectedNodeId.value = nodeId
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _themeMode.value = "system"
        _language.value = "zh"
        _dynamicColor.value = true
        _autoConnectOnBoot.value = false
        _autoReconnectNetworkChange.value = true
        _reconnectIntervalSeconds.value = 3
        _tunEnabled.value = true
        _tunMtu.value = 9000
        _tunStrictRoute.value = true
        _tunAutoRoute.value = true
        _tunDnsHijack.value = true
        _tunIpv4Address.value = "172.19.0.1/30"
        _tunIpv6Address.value = "fdfe:dcba:9876::1/126"
        _dnsMode.value = "fakeip"
        _fakeIpEnabled.value = true
        _fakeIpRange.value = "198.18.0.0/15"
        _directDns.value = "223.5.5.5"
        _remoteDns.value = "https://1.1.1.1/dns-query"
        _optimisticDns.value = true
        _defaultRouteMode.value = "rule"
        _bypassLan.value = true
        _bypassChina.value = true
        _speedTestUrl.value = "https://www.gstatic.com/generate_204"
        _speedTestTimeoutMs.value = 3000
        speedTestTimeoutSec.value = 3
        _speedTestConcurrency.value = 4
        _coreLogLevel.value = "info"
        _selectedNodeId.value = -1L
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_AUTO_CONNECT_BOOT = "pref_auto_connect_boot"
        private const val KEY_AUTO_RECONNECT_NET = "pref_auto_reconnect_net"
        private const val KEY_RECONNECT_INTERVAL = "pref_reconnect_interval"
        private const val KEY_TUN_ENABLED = "pref_tun_enabled"
        private const val KEY_TUN_MTU = "pref_tun_mtu"
        private const val KEY_TUN_STRICT_ROUTE = "pref_tun_strict_route"
        private const val KEY_TUN_AUTO_ROUTE = "pref_tun_auto_route"
        private const val KEY_TUN_DNS_HIJACK = "pref_tun_dns_hijack"
        private const val KEY_TUN_IPV4 = "pref_tun_ipv4"
        private const val KEY_TUN_IPV6 = "pref_tun_ipv6"
        private const val KEY_DNS_MODE = "pref_dns_mode"
        private const val KEY_FAKE_IP_ENABLED = "pref_fake_ip_enabled"
        private const val KEY_FAKE_IP_RANGE = "pref_fake_ip_range"
        private const val KEY_DIRECT_DNS = "pref_direct_dns"
        private const val KEY_REMOTE_DNS = "pref_remote_dns"
        private const val KEY_OPTIMISTIC_DNS = "pref_optimistic_dns"
        private const val KEY_DEFAULT_ROUTE_MODE = "pref_default_route_mode"
        private const val KEY_BYPASS_LAN = "pref_bypass_lan"
        private const val KEY_BYPASS_CHINA = "pref_bypass_china"
        private const val KEY_SPEED_TEST_URL = "pref_speed_test_url"
        private const val KEY_SPEED_TEST_TIMEOUT = "pref_speed_test_timeout"
        private const val KEY_SPEED_TEST_CONCURRENCY = "pref_speed_test_concurrency"
        private const val KEY_CORE_LOG_LEVEL = "pref_core_log_level"
        private const val KEY_SELECTED_NODE_ID = "pref_selected_node_id"
    }
}
