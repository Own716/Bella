package com.bellabox.app

import android.app.Application
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
        database = BellaDatabase.getInstance(this)
        nodeRepository = NodeRepository(database)
        subscriptionRepository = SubscriptionRepository(database)
        ruleRepository = RuleRepository(database)
        strategyGroupRepository = StrategyGroupRepository(database)
        settingsRepository = SettingsRepository(this)

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
    }

    companion object {
        lateinit var instance: BellaApplication
            private set
    }
}
