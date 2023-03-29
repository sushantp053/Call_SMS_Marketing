package com.deecto.callsmsmarketing.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "blocked_contacts")
data class BlockedContacts(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "phone") var phone: String,
    @ColumnInfo(name = "name") var name: String,
): java.io.Serializable
