package com.bellabox.app

import android.app.Application
import com.bellabox.core.common.AppLogger
import com.bellabox.core.database.BellaDatabase

class BellaApplication : Application() {

    lateinit var database: BellaDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = BellaDatabase.getInstance(this)
        AppLogger.i("BellaApplication", "BellaBox Application initialized successfully")
    }

    companion object {
        lateinit var instance: BellaApplication
            private set
    }
}
