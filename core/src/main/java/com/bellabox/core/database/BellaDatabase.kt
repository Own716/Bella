package com.bellabox.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        NodeEntity::class,
        SubscriptionEntity::class,
        StrategyGroupEntity::class,
        RouteRuleEntity::class
    ],
    version = 2,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_proxy_nodes_subscription ON proxy_nodes(subscriptionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_proxy_nodes_fingerprint ON proxy_nodes(fingerprintHash)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_route_rules_priority ON route_rules(priority)")
            }
        }

        fun getInstance(context: Context): BellaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BellaDatabase::class.java,
                    "bellabox.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
