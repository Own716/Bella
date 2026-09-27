package com.bellabox.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import com.bellabox.app.crash.CrashHandler
import com.bellabox.core.common.AppLogger
import com.bellabox.core.database.BellaDatabase
import com.bellabox.core.repository.NodeRepository
import com.bellabox.core.repository.RuleRepository
import com.bellabox.core.repository.SettingsRepository
import com.bellabox.core.repository.StrategyGroupRepository
import com.bellabox.core.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BellaApplication : Application() {

    lateinit var database: BellaDatabase
        private set
    lateinit var nodeRepository: NodeRepository
        private set
    lateinit var subscriptionRepository: SubscriptionRepository
        private set
    lateinit var ruleRepository: RuleRepository
        private set
    lateinit var strategyGroupRepository: StrategyGroupRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Install CrashHandler immediately as the very first operation
        CrashHandler.install(this)

        // If running in the dedicated crash reporting process, skip normal app initialization
        if (isCrashProcess()) {
            return
        }

        try {
            settingsRepository = SettingsRepository(this)
            database = BellaDatabase.getInstance(this)
            nodeRepository = NodeRepository(database)
            subscriptionRepository = SubscriptionRepository(database)
            ruleRepository = RuleRepository(database)
            strategyGroupRepository = StrategyGroupRepository(database)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Initialize default route rules and strategy groups if database is fresh
                    if (ruleRepository.getAllRules().isEmpty()) {
                        ruleRepository.resetToDefaultRules()
                    }
                    strategyGroupRepository.initializeDefaultGroups()
                } catch (e: Exception) {
                    AppLogger.w("BellaApplication", "Initialization non-fatal warning: ${e.message}")
                }
            }
            AppLogger.i("BellaApplication", "BellaBox Application initialized successfully")
        } catch (e: Throwable) {
            AppLogger.e("BellaApplication", "Fatal error during BellaApplication onCreate: ${e.message}", e)
            throw e // CrashHandler will catch this and display CrashActivity!
        }
    }

    private fun isCrashProcess(): Boolean {
        val processName = getProcessNameCompat()
        return processName.endsWith(":crash")
    }

    private fun getProcessNameCompat(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getProcessName()
        }
        val pid = Process.myPid()
        val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return ""
        val runningApps = am.runningAppProcesses ?: return ""
        for (info in runningApps) {
            if (info.pid == pid) {
                return info.processName ?: ""
            }
        }
        return ""
    }

    companion object {
        lateinit var instance: BellaApplication
            private set
    }
}
