package com.dailycultivation.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailycultivation.app.data.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: JournalEntity): Long

    @Update
    suspend fun update(journal: JournalEntity)

    @Query("SELECT * FROM journals WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: Long): JournalEntity?

    @Query("SELECT * FROM journals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): JournalEntity?

    @Query("SELECT * FROM journals WHERE date = :date LIMIT 1")
    fun observeByDate(date: Long): Flow<JournalEntity?>

    @Query("SELECT * FROM journals ORDER BY date DESC")
    fun observeAll(): Flow<List<JournalEntity>>

    @Query("DELETE FROM journals WHERE id = :id")
    suspend fun delete(id: Long)
}
