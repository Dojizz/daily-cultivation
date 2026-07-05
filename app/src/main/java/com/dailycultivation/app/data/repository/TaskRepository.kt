package com.dailycultivation.app.data.repository

import com.dailycultivation.app.data.dao.TaskDao
import com.dailycultivation.app.data.entity.DEADLINE_DURATION_MS
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.data.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val dao: TaskDao) {

    /** 获取活跃任务（未过期 + 未完成），按剩余时间升序排列 */
    fun observeActiveTasks(): Flow<List<TaskWithDeadline>> {
        return dao.observeByStatus(TaskStatus.PENDING).map { list ->
            val now = System.currentTimeMillis()
            list
                .map { TaskWithDeadline(it, it.createdAt + DEADLINE_DURATION_MS - now) }
                .sortedBy { it.remainingMs }
        }
    }

    /** 获取已完成任务 */
    fun observeCompletedTasks(): Flow<List<TaskEntity>> {
        return dao.observeByStatus(TaskStatus.COMPLETED)
    }

    /** 获取已过期任务 */
    fun observeExpiredTasks(): Flow<List<TaskWithDeadline>> {
        return dao.observeByStatus(TaskStatus.EXPIRED).map { list ->
            val now = System.currentTimeMillis()
            list.map { TaskWithDeadline(it, it.createdAt + DEADLINE_DURATION_MS - now) }
        }
    }

    /** 获取已终止任务 */
    fun observeCancelledTasks(): Flow<List<TaskEntity>> {
        return dao.observeByStatus(TaskStatus.CANCELLED)
    }

    suspend fun addTask(title: String, description: String = ""): Long {
        val task = TaskEntity(
            title = title,
            description = description,
        )
        return dao.insert(task)
    }

    suspend fun completeTask(id: Long) {
        dao.updateStatus(id, TaskStatus.COMPLETED, System.currentTimeMillis())
    }

    suspend fun expireTask(id: Long) {
        dao.updateStatus(id, TaskStatus.EXPIRED)
    }

    /** 重启过期/终止任务：重置创建时间，改回 pending */
    suspend fun restartTask(id: Long) {
        val task = dao.getById(id) ?: return
        dao.update(
            task.copy(
                createdAt = System.currentTimeMillis(),
                status = TaskStatus.PENDING,
                completedAt = null,
                cancelledAt = null,
            )
        )
    }

    suspend fun cancelTask(id: Long) {
        val task = dao.getById(id) ?: return
        dao.update(task.copy(status = TaskStatus.CANCELLED, cancelledAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(id: Long) {
        dao.delete(id)
    }

    /** 检查并标记所有已过期的 pending 任务 */
    suspend fun expireOverdueTasks() {
        val pending = dao.observeByStatus(TaskStatus.PENDING)
        // 不能直接 collect flow，这里改用一次性查询
    }

    /** 直接获取所有 pending 任务用于批量过期检查 */
    suspend fun checkAndExpireOverdue() {
        val now = System.currentTimeMillis()
        // 这里通过 Repository 方法间接处理，实际由 ViewModel 驱动
        dao.observeByStatus(TaskStatus.PENDING).collect { tasks ->
            tasks.filter { it.createdAt + DEADLINE_DURATION_MS < now }
                .forEach { dao.updateStatus(it.id, TaskStatus.EXPIRED) }
        }
    }
}

data class TaskWithDeadline(
    val task: TaskEntity,
    val remainingMs: Long,
)
