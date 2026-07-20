package com.dailycultivation.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.entity.PracticeType
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    // ── 日课定义 ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPractice(practice: PracticeEntity): Long

    @Update
    suspend fun updatePractice(practice: PracticeEntity)

    @Query("DELETE FROM practices WHERE id = :id")
    suspend fun deletePractice(id: Long)

    @Query("SELECT * FROM practices ORDER BY sortOrder ASC")
    fun observeAllPractices(): Flow<List<PracticeEntity>>

    @Query("SELECT * FROM practices WHERE isActive = 1 AND type = :type ORDER BY sortOrder ASC")
    fun observeActiveByType(type: String): Flow<List<PracticeEntity>>

    @Query("SELECT * FROM practices WHERE id = :id")
    suspend fun getPracticeById(id: Long): PracticeEntity?

    @Query("SELECT COUNT(*) FROM practices WHERE isActive = 1")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM practices WHERE isActive = 1 AND type = :type")
    suspend fun getActiveCountByType(type: String): Int

    // ── 日课记录 ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PracticeRecordEntity): Long

    @Update
    suspend fun updateRecord(record: PracticeRecordEntity)

    @Query("SELECT * FROM practice_records WHERE practiceId = :practiceId AND date = :date LIMIT 1")
    suspend fun getRecord(practiceId: Long, date: Long): PracticeRecordEntity?

    @Query("SELECT * FROM practice_records WHERE date = :date")
    fun observeRecordsForDate(date: Long): Flow<List<PracticeRecordEntity>>

    @Query("SELECT * FROM practice_records WHERE date >= :startDate AND date < :endDate")
    fun observeRecordsInRange(startDate: Long, endDate: Long): Flow<List<PracticeRecordEntity>>
}
