package com.bellabox.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bellabox.app.BellaApplication
import com.bellabox.core.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: SettingsRepository = (application as BellaApplication).settingsRepository

    val themeMode: StateFlow<String> = repo.themeMode
    val language: StateFlow<String> = repo.language
    val dynamicColor: StateFlow<Boolean> = repo.dynamicColor

    val autoConnectOnBoot: StateFlow<Boolean> = repo.autoConnectOnBoot
    val autoReconnectNetworkChange: StateFlow<Boolean> = repo.autoReconnectNetworkChange

    val tunMtu: StateFlow<Int> = repo.tunMtu
    val tunStrictRoute: StateFlow<Boolean> = repo.tunStrictRoute

    val routingMode: StateFlow<String> = repo.routingMode
    val bypassLan: StateFlow<Boolean> = repo.bypassLan

    val fakeIpEnabled: StateFlow<Boolean> = repo.fakeIpEnabled
    val fakeIpRange: StateFlow<String> = repo.fakeIpRange
    val dnsDirect: StateFlow<String> = repo.dnsDirect
    val dnsRemote: StateFlow<String> = repo.dnsRemote

    val speedTestUrl: StateFlow<String> = repo.speedTestUrl
    val speedTestTimeoutSec: StateFlow<Int> = repo.speedTestTimeoutSec
    val speedTestConcurrency: StateFlow<Int> = repo.speedTestConcurrency

    fun setThemeMode(mode: String) = repo.setThemeMode(mode)
    fun setLanguage(lang: String) = repo.setLanguage(lang)
    fun setDynamicColor(enabled: Boolean) = repo.setDynamicColor(enabled)

    fun setAutoConnectOnBoot(enabled: Boolean) = repo.setAutoConnectOnBoot(enabled)
    fun setAutoReconnectNetworkChange(enabled: Boolean) = repo.setAutoReconnectNetworkChange(enabled)

    fun setTunMtu(mtu: Int) = repo.setTunMtu(mtu)
    fun setTunStrictRoute(enabled: Boolean) = repo.setTunStrictRoute(enabled)

    fun setRoutingMode(mode: String) = repo.setRoutingMode(mode)
    fun setBypassLan(enabled: Boolean) = repo.setBypassLan(enabled)

    fun setFakeIpEnabled(enabled: Boolean) = repo.setFakeIpEnabled(enabled)
    fun setFakeIpRange(range: String) = repo.setFakeIpRange(range)
    fun setDnsDirect(dns: String) = repo.setDnsDirect(dns)
    fun setDnsRemote(dns: String) = repo.setDnsRemote(dns)

    fun setSpeedTestUrl(url: String) = repo.setSpeedTestUrl(url)
    fun setSpeedTestTimeoutSec(sec: Int) = repo.setSpeedTestTimeoutSec(sec)
    fun setSpeedTestConcurrency(concurrency: Int) = repo.setSpeedTestConcurrency(concurrency)
}
