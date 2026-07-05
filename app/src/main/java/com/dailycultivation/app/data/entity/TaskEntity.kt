package com.dailycultivation.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: TaskStatus = TaskStatus.PENDING,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
)

enum class TaskStatus {
    PENDING,
    COMPLETED,
    EXPIRED,
    CANCELLED,
}

/** 72 小时的毫秒数 */
const val DEADLINE_DURATION_MS: Long = 72 * 60 * 60 * 1000L
