package com.dailycultivation.app.data.db

import android.content.Context
import android.util.Log
import com.dailycultivation.app.BuildConfig
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 备份元数据，存在备份目录下的 backup_info.json 中。
 */
data class BackupMetadata(
    val dbVersion: Int?,     // null 表示 Room 打开前备份的，版本未知
    val appVersion: String,  // e.g. "0.1.1"
)

data class BackupInfo(
    val folderName: String,
    val timestamp: Date,
    val displayName: String,
    val dbSize: Long,
    val metadata: BackupMetadata?,
)

object DatabaseBackupManager {

    private const val TAG = "DatabaseBackup"
    private const val BACKUP_DIR = "database_backups"
    private const val MAX_BACKUPS = 5
    private const val DB_NAME = "daily_cultivation.db"
    private const val META_FILE = "backup_info.json"

    /**
     * 自动备份：在 Room 打开数据库之前调用。
     * 复制当前数据库文件，写入元数据（dbVersion 未知）。
     */
    fun autoBackup(context: Context) {
        try {
            val dbPath = context.getDatabasePath(DB_NAME)
            if (!dbPath.exists()) return  // 首次启动，尚无数据库

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val backupFolder = File(
                context.getDir(BACKUP_DIR, Context.MODE_PRIVATE),
                dateFormat.format(Date()),
            )
            backupFolder.mkdirs()

            val dbFiles = listOfNotNull(
                dbPath,
                dbPath.resolveSibling("$DB_NAME-shm").takeIf { it.exists() },
                dbPath.resolveSibling("$DB_NAME-wal").takeIf { it.exists() },
            )

            for (src in dbFiles) {
                src.copyTo(File(backupFolder, src.name), overwrite = true)
            }

            writeMetadata(backupFolder, BackupMetadata(
                dbVersion = null,
                appVersion = BuildConfig.VERSION_NAME,
            ))

            Log.i(TAG, "自动备份完成: ${backupFolder.name} (app=${BuildConfig.VERSION_NAME})")
            pruneOldBackups(context)
        } catch (e: Exception) {
            Log.e(TAG, "自动备份失败", e)
        }
    }

    /**
     * Room 成功打开数据库后调用，补全最新备份的 dbVersion。
     */
    fun updateLatestBackupMetadata(context: Context, dbVersion: Int) {
        try {
            val backupDir = context.getDir(BACKUP_DIR, Context.MODE_PRIVATE)
            val latest = backupDir.listFiles()
                ?.filter { it.isDirectory }
                ?.maxByOrNull { it.name }
                ?: return

            val meta = readMetadata(latest)
            if (meta != null && meta.dbVersion == null) {
                writeMetadata(latest, meta.copy(dbVersion = dbVersion))
                Log.d(TAG, "补全备份元数据: ${latest.name} dbVersion=$dbVersion")
            }
        } catch (e: Exception) {
            Log.e(TAG, "更新备份元数据失败", e)
        }
    }

    /**
     * 列出所有可用备份，按时间倒序。
     */
    fun listBackups(context: Context): List<BackupInfo> {
        val backupDir = context.getDir(BACKUP_DIR, Context.MODE_PRIVATE)
        if (!backupDir.exists()) return emptyList()

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val displayFormat = SimpleDateFormat("yyyy年M月d日 HH:mm:ss", Locale.CHINA)

        return backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.name }
            ?.mapNotNull { dir ->
                val timestamp = try {
                    dateFormat.parse(dir.name)
                } catch (_: Exception) {
                    null
                }
                if (timestamp == null) return@mapNotNull null

                val dbFile = File(dir, DB_NAME)
                BackupInfo(
                    folderName = dir.name,
                    timestamp = timestamp,
                    displayName = displayFormat.format(timestamp),
                    dbSize = if (dbFile.exists()) dbFile.length() else 0,
                    metadata = readMetadata(dir),
                )
            }
            ?: emptyList()
    }

    /**
     * 从备份恢复。会覆盖当前数据库。成功后需要重启 App。
     */
    fun restoreFromBackup(context: Context, folderName: String): Boolean {
        return try {
            val backupDir = context.getDir(BACKUP_DIR, Context.MODE_PRIVATE)
            val sourceFolder = File(backupDir, folderName)
            if (!sourceFolder.exists()) return false

            // 先关闭数据库
            AppDatabase.closeInstance()

            val dbPath = context.getDatabasePath(DB_NAME)

            // 覆盖主数据库文件
            val sourceDb = File(sourceFolder, DB_NAME)
            if (!sourceDb.exists()) return false
            sourceDb.copyTo(dbPath, overwrite = true)

            // 覆盖 shm/wal 文件（如果备份中有）
            for (suffix in listOf("$DB_NAME-shm", "$DB_NAME-wal")) {
                val sourceFile = File(sourceFolder, suffix)
                val targetFile = dbPath.resolveSibling(suffix)
                if (sourceFile.exists()) {
                    sourceFile.copyTo(targetFile, overwrite = true)
                } else {
                    targetFile.delete()
                }
            }

            Log.i(TAG, "从备份恢复成功: $folderName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "从备份恢复失败", e)
            false
        }
    }

    /**
     * 删除指定备份。
     */
    fun deleteBackup(context: Context, folderName: String): Boolean {
        val backupDir = context.getDir(BACKUP_DIR, Context.MODE_PRIVATE)
        val folder = File(backupDir, folderName)
        return folder.deleteRecursively()
    }

    // ── 元数据读写 ──────────────────────────────────────────────

    private fun writeMetadata(folder: File, metadata: BackupMetadata) {
        val json = JSONObject().apply {
            put("dbVersion", metadata.dbVersion ?: JSONObject.NULL)
            put("appVersion", metadata.appVersion)
        }
        File(folder, META_FILE).writeText(json.toString())
    }

    private fun readMetadata(folder: File): BackupMetadata? {
        val metaFile = File(folder, META_FILE)
        if (!metaFile.exists()) return null
        return try {
            val json = JSONObject(metaFile.readText())
            BackupMetadata(
                dbVersion = if (json.isNull("dbVersion")) null else json.getInt("dbVersion"),
                appVersion = json.getString("appVersion"),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun pruneOldBackups(context: Context) {
        val backupDir = context.getDir(BACKUP_DIR, Context.MODE_PRIVATE)
        val backups = backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.name }
            ?: return

        backups.drop(MAX_BACKUPS).forEach { dir ->
            dir.deleteRecursively()
            Log.i(TAG, "清理旧备份: ${dir.name}")
        }
    }
}
