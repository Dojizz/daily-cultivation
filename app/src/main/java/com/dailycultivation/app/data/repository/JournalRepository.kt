package com.dailycultivation.app.data.repository

import com.dailycultivation.app.data.dao.JournalDao
import com.dailycultivation.app.data.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val dao: JournalDao) {

    /** 获取"今天"对应的日记词条（3am 前算昨天） */
    fun observeToday(): Flow<JournalEntity?> {
        return dao.observeByDate(JournalEntity.todayDate())
    }

    fun observeByDate(date: Long): Flow<JournalEntity?> {
        return dao.observeByDate(date)
    }

    /** 获取所有日记（按日期倒序） */
    fun observeAll(): Flow<List<JournalEntity>> = dao.observeAll()

    /** 保存或更新今天的日记。不存在则创建，存在则更新内容和 updatedAt。 */
    suspend fun saveToday(content: String) {
        val date = JournalEntity.todayDate()
        val existing = dao.getByDate(date)
        if (existing != null) {
            dao.update(
                existing.copy(
                    content = content,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        } else {
            dao.insert(
                JournalEntity(
                    date = date,
                    content = content,
                )
            )
        }
    }

    suspend fun getByDate(date: Long): JournalEntity? = dao.getByDate(date)

    suspend fun updateContent(id: Long, content: String) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                content = content,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
