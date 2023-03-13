package com.deecto.callsmsmarketing.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_sms")
data class DaySMSCounter(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "day") var title: String?,
    @ColumnInfo(name = "counter") var counter: Int?,
): java.io.Serializable

