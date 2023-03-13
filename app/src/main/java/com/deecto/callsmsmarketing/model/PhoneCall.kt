package com.deecto.callsmsmarketing.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_calls")
data class PhoneCall(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "phone") var phone: String,
    @ColumnInfo(name = "outgoing_time") var outgoing_time: String,
    @ColumnInfo(name = "incoming_time") var incoming_time: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "counter") var counter: Int,
): java.io.Serializable
