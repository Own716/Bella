package com.bellabox.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bellabox.core.common.AppLogger

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
        private const val TAG = "BellaDatabase"

        @Volatile
        private var instance: BellaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_proxy_nodes_subscription ON proxy_nodes(subscriptionId)")
                } catch (e: Throwable) {
                    AppLogger.w(TAG, "MIGRATION_1_2 idx_proxy_nodes_subscription skipped: ${e.message}")
                }
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_proxy_nodes_fingerprint ON proxy_nodes(fingerprintHash)")
                } catch (e: Throwable) {
                    AppLogger.w(TAG, "MIGRATION_1_2 idx_proxy_nodes_fingerprint skipped: ${e.message}")
                }
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_route_rules_priority ON route_rules(priority)")
                } catch (e: Throwable) {
                    AppLogger.w(TAG, "MIGRATION_1_2 idx_route_rules_priority skipped: ${e.message}")
                }
            }
        }

        private fun buildDatabase(context: Context): BellaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BellaDatabase::class.java,
                "bellabox.db"
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }

        fun getInstance(context: Context): BellaDatabase {
            return instance ?: synchronized(this) {
                instance ?: createSafeDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun createSafeDatabase(appContext: Context): BellaDatabase {
            return try {
                val db = buildDatabase(appContext)
                // Force SQLite open and schema verification immediately inside try-catch
                db.openHelper.writableDatabase
                db
            } catch (e: Throwable) {
                AppLogger.e(TAG, "Database initialization or migration error detected: ${e.message}. Rebuilding clean database.", e)
                try {
                    appContext.deleteDatabase("bellabox.db")
                } catch (delEx: Throwable) {
                    AppLogger.e(TAG, "Failed to delete corrupted database: ${delEx.message}")
                }
                try {
                    val freshDb = Room.databaseBuilder(
                        appContext,
                        BellaDatabase::class.java,
                        "bellabox.db"
                    )
                        .fallbackToDestructiveMigration()
                        .fallbackToDestructiveMigrationOnDowngrade()
                        .build()
                    freshDb.openHelper.writableDatabase
                    freshDb
                } catch (fatal: Throwable) {
                    AppLogger.e(TAG, "Fatal fallback database error: ${fatal.message}", fatal)
                    // In-memory fallback as absolute failsafe so app never crashes
                    Room.inMemoryDatabaseBuilder(appContext, BellaDatabase::class.java)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
    }
}
