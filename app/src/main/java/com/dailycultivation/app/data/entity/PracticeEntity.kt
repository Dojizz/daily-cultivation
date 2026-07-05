package com.dailycultivation.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 日课定义 */
@Entity(tableName = "practices")
data class PracticeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val type: PracticeType = PracticeType.VIRTUE,
    val createdAt: Long = System.currentTimeMillis(),
)
