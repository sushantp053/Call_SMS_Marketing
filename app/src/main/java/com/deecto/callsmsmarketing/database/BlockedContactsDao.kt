package com.deecto.callsmsmarketing.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deecto.callsmsmarketing.model.BlockedContacts
import com.deecto.callsmsmarketing.model.PhoneCall

@Dao
interface BlockedContactsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedContacts: BlockedContacts)

    @Query("SELECT * FROM blocked_contacts WHERE phone = :phone")
    fun isBlocked(phone: String?) : BlockedContacts
}