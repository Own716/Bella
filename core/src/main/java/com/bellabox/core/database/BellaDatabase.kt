package com.bellabox.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NodeEntity::class,
        SubscriptionEntity::class,
        StrategyGroupEntity::class,
        RouteRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BellaDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun strategyGroupDao(): StrategyGroupDao
    abstract fun routeRuleDao(): RouteRuleDao

    companion object {
        @Volatile
        private var instance: BellaDatabase? = null

        fun getInstance(context: Context): BellaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BellaDatabase::class.java,
                    "bellabox.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
