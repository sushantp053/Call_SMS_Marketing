package com.deecto.callsmsmarketing.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.model.PhoneCall

@Dao
interface PhoneCallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phoneCall: PhoneCall)

    @Query("SELECT * FROM phone_calls WHERE phone = :phone")
    fun getLastCall(phone: String?) : PhoneCall

    @Query("Update phone_calls SET incoming_time = :time, counter = counter+1 Where phone = :phone")
    suspend fun updateIncomingTime(time: String?, phone: String?)

    @Query("Update phone_calls SET outgoing_time = :time, counter = counter+1  Where phone = :phone ")
    suspend fun updateOutgoingTime(time: String?, phone: String?)

}
