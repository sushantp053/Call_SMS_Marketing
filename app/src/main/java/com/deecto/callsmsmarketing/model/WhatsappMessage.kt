package com.deecto.callsmsmarketing.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp" )
data class WhatsappMessage(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "message") var message: String?,
    @ColumnInfo(name = "created_at") var created_at: String?,
    @ColumnInfo(name = "status") var status: Boolean,
) : java.io.Serializable
