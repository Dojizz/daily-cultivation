package com.dailycultivation.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Calendar

@Entity(
    tableName = "journals",
    indices = [Index(value = ["date"], unique = true)],
)
data class JournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,        // 日记所属日期的零点时间戳
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) : Serializable {
    companion object {
        /**
         * 计算"今天"对应的日记日期。
         * 凌晨 3 点之前算作前一天——因为睡前写日记可能跨过午夜。
         */
        fun todayDate(): Long {
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
