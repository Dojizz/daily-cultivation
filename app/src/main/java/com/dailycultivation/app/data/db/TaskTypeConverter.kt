package com.dailycultivation.app.data.db

import androidx.room.TypeConverter
import com.dailycultivation.app.data.entity.TaskType

class TaskTypeConverter {
    @TypeConverter
    fun fromTaskType(type: TaskType): String = type.name

    @TypeConverter
    fun toTaskType(value: String): TaskType =
        when (value) {
            "SHORT_TERM" -> TaskType.SHORT_TERM
            "LONG_TERM" -> TaskType.LONG_TERM
            else -> TaskType.SHORT_TERM  // 兼容旧数据
        }
}
