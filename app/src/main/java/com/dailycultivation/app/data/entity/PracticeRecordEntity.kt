package com.dailycultivation.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 日课每日记录 */
@Entity(tableName = "practice_records")
data class PracticeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val practiceId: Long,
    val date: Long,  // 当天零点的时间戳
    val note: String = "",
)
