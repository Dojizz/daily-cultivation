package com.dailycultivation.app.data.repository

import com.dailycultivation.app.data.dao.TaskDao
import com.dailycultivation.app.data.entity.DEADLINE_DURATION_MS
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.data.entity.TaskStatus
import com.dailycultivation.app.data.entity.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val dao: TaskDao) {

    /** 短期活跃任务（72h 截止），按剩余时间升序 */
    fun observeActiveShortTermTasks(): Flow<List<TaskWithDeadline>> {
        return dao.observeByStatusAndType(TaskStatus.PENDING, TaskType.SHORT_TERM).map { list ->
            val now = System.currentTimeMillis()
            list
                .map { TaskWithDeadline(it, it.createdAt + DEADLINE_DURATION_MS - now) }
                .sortedBy { it.remainingMs }
        }
    }

    /** 长期活跃任务（无截止时间），按创建时间倒序 */
    fun observeActiveLongTermTasks(): Flow<List<TaskWithDeadline>> {
        return dao.observeByStatusAndType(TaskStatus.PENDING, TaskType.LONG_TERM).map { list ->
            list.map { TaskWithDeadline(it, Long.MAX_VALUE) }
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

    suspend fun addTask(
        title: String,
        description: String = "",
        taskType: TaskType = TaskType.SHORT_TERM,
    ): Long {
        val task = TaskEntity(
            title = title,
            description = description,
            taskType = taskType,
        )
        return dao.insert(task)
    }

    suspend fun completeTask(id: Long) {
        dao.updateStatus(id, TaskStatus.COMPLETED, System.currentTimeMillis())
    }

    suspend fun expireTask(id: Long) {
        dao.updateStatus(id, TaskStatus.EXPIRED)
    }

    /** 重启过期/终止任务 */
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
}

data class TaskWithDeadline(
    val task: TaskEntity,
    val remainingMs: Long,
)
