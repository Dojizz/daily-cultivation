package com.dailycultivation.app.data.repository

import com.dailycultivation.app.data.dao.PracticeDao
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.entity.PracticeType
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

    /** 获取所有活跃的习惯（HABIT），每天都会出现 */
    fun observeAllHabits(): Flow<List<PracticeEntity>> =
        dao.observeActiveByType(PracticeType.HABIT.name)

    /** 获取所有活跃的特质（VIRTUE），用于轮转 */
    fun observeAllVirtues(): Flow<List<PracticeEntity>> =
        dao.observeActiveByType(PracticeType.VIRTUE.name)

    suspend fun addPractice(
        name: String,
        description: String = "",
        type: PracticeType = PracticeType.VIRTUE,
    ): Long {
        val maxOrder = dao.observeAllPractices().first().maxOfOrNull { it.sortOrder } ?: -1
        return dao.insertPractice(
            PracticeEntity(
                name = name,
                description = description,
                sortOrder = maxOrder + 1,
                type = type,
            )
        )
    }

    suspend fun updatePractice(practice: PracticeEntity) {
        dao.updatePractice(practice)
    }

    suspend fun deletePractice(id: Long) {
        dao.deletePractice(id)
    }

    // ── 特制轮转 ──

    /** 计算今天轮转到的特质（仅 VIRTUE 类型） */
    suspend fun getTodayPractice(): PracticeEntity? {
        val activeList = dao.observeActiveByType(PracticeType.VIRTUE.name).first()
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

    /** 打卡：特制用 note，习惯用 durationMinutes */
    suspend fun checkIn(
        practiceId: Long,
        note: String = "",
        durationMinutes: Int = 0,
    ) {
        val today = getTodayStartMs()
        val existing = dao.getRecord(practiceId, today)
        if (existing != null) {
            dao.updateRecord(
                existing.copy(
                    note = note,
                    durationMinutes = durationMinutes,
                )
            )
        } else {
            dao.insertRecord(
                PracticeRecordEntity(
                    practiceId = practiceId,
                    date = today,
                    note = note,
                    durationMinutes = durationMinutes,
                )
            )
        }
    }

    fun observeTodayRecords(): Flow<List<PracticeRecordEntity>> {
        return dao.observeRecordsForDate(getTodayStartMs())
    }

    fun observeRecordsForDate(date: Long): Flow<List<PracticeRecordEntity>> {
        return dao.observeRecordsForDate(date)
    }

    fun observeRecordsInRange(startDate: Long, endDate: Long): Flow<List<PracticeRecordEntity>> {
        return dao.observeRecordsInRange(startDate, endDate)
    }

    companion object {
        /** 今天零点时间戳。凌晨 3 点前算前一天，与日记规则一致。 */
        fun getTodayStartMs(): Long {
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.HOUR_OF_DAY) < 3) {
                cal.add(Calendar.DAY_OF_MONTH, -1)
            }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
