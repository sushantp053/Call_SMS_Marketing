package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.deecto.callsmsmarketing.model.BlockedContacts
import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.model.PhoneCall

@Dao
interface BlockedContactsDao {
    @Query("SELECT * FROM blocked_contacts ORDER BY id DESC")
    fun getAllContacts() : LiveData<List<BlockedContacts>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedContacts: BlockedContacts)
    @Delete
    suspend fun delete(blockedContacts: BlockedContacts)

    @Query("SELECT EXISTS(SELECT * FROM blocked_contacts WHERE phone = :phone )")
    fun isBlocked(phone: String?) : Boolean
}