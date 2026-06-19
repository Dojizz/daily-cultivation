package com.dailycultivation.app.data.repository

import com.dailycultivation.app.data.dao.PracticeDao
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** 轮转基准日期：2026-06-19（日课功能上线日） */
private val ROTATION_EPOCH_MS: Long = run {
    Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.JUNE)
        set(Calendar.DAY_OF_MONTH, 19)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

class PracticeRepository(private val dao: PracticeDao) {

    // ── 日课定义 ──

    fun observeAllPractices(): Flow<List<PracticeEntity>> = dao.observeAllPractices()

    suspend fun addPractice(name: String, description: String = ""): Long {
        val maxOrder = dao.observeAllPractices().first().maxOfOrNull { it.sortOrder } ?: -1
        return dao.insertPractice(
            PracticeEntity(
                name = name,
                description = description,
                sortOrder = maxOrder + 1,
            )
        )
    }

    suspend fun updatePractice(practice: PracticeEntity) {
        dao.updatePractice(practice)
    }

    suspend fun deletePractice(id: Long) {
        dao.deletePractice(id)
    }

    // ── 每日轮转 ──

    /** 计算今天的日课索引（在活跃日课列表中的位置） */
    suspend fun getTodayPractice(): PracticeEntity? {
        val activeList = dao.observeAllPractices().first()
            .filter { it.isActive }
            .sortedBy { it.sortOrder }
        if (activeList.isEmpty()) return null

        val todayStart = getTodayStartMs()
        val daysSinceEpoch = TimeUnit.MILLISECONDS.toDays(todayStart - ROTATION_EPOCH_MS).toInt()
        val index = daysSinceEpoch % activeList.size
        return activeList[index]
    }

    // ── 日课记录 ──

    suspend fun getTodayRecord(practiceId: Long): PracticeRecordEntity? {
        return dao.getRecord(practiceId, getTodayStartMs())
    }

    suspend fun checkIn(practiceId: Long, note: String = "") {
        val today = getTodayStartMs()
        val existing = dao.getRecord(practiceId, today)
        if (existing != null) {
            dao.updateRecord(existing.copy(note = note))
        } else {
            dao.insertRecord(
                PracticeRecordEntity(
                    practiceId = practiceId,
                    date = today,
                    note = note,
                )
            )
        }
    }

    fun observeTodayRecords(): Flow<List<PracticeRecordEntity>> {
        return dao.observeRecordsForDate(getTodayStartMs())
    }

    companion object {
        fun getTodayStartMs(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
