package com.dailycultivation.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailycultivation.app.data.dao.JournalDao
import com.dailycultivation.app.data.dao.PracticeDao
import com.dailycultivation.app.data.dao.TaskDao
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        PracticeEntity::class,
        PracticeRecordEntity::class,
        JournalEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(PracticeTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun practiceDao(): PracticeDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val appContext = context.applicationContext

                    // 在打开数据库之前自动备份，防止 migration 失败导致数据丢失
                    DatabaseBackupManager.autoBackup(appContext)

                    Room.databaseBuilder(
                        appContext,
                        AppDatabase::class.java,
                        "daily_cultivation.db"
                    )
                        .addMigrations(MIGRATION_3_4)
                        // 禁止自动删库：缺少 Migration 时直接崩溃，强迫开发者写 Migration
                        .addCallback(object : Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                // Room 成功打开后，补全备份元数据中的 dbVersion
                                DatabaseBackupManager.updateLatestBackupMetadata(
                                    appContext, db.version
                                )
                            }
                        })
                        .build().also { INSTANCE = it }
                }
            }
        }

        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        private val MIGRATION_3_4 = Migration(3, 4) { db ->
            db.execSQL("ALTER TABLE practices ADD COLUMN type TEXT NOT NULL DEFAULT 'VIRTUE'")
            db.execSQL("ALTER TABLE practice_records ADD COLUMN durationMinutes INTEGER NOT NULL DEFAULT 0")
        }
    }
}
