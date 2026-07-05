package com.dailycultivation.app.data.db

import android.util.Log
import androidx.room.TypeConverter
import com.dailycultivation.app.data.entity.PracticeType

class PracticeTypeConverter {
    @TypeConverter
    fun fromPracticeType(type: PracticeType): String = type.name

    @TypeConverter
    fun toPracticeType(value: String): PracticeType {
        Log.d("PracticeTypeConverter", "toPracticeType 输入: '$value'")
        return when (value) {
            "HABIT", "0" -> PracticeType.HABIT
            "VIRTUE", "1" -> PracticeType.VIRTUE
            else -> {
                Log.e("PracticeTypeConverter", "未知的 PracticeType 值: '$value'，回退为 VIRTUE")
                PracticeType.VIRTUE
            }
        }
    }
}
