package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deecto.callsmsmarketing.model.Message

@Dao
interface WhatsappDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("UPDATE whatsapp SET status = :status")
    suspend fun updateAllStatus(status: Boolean?)

    @Query("UPDATE whatsapp SET status = :status WHERE id = :id")
    suspend fun updateCurrentStatus(status: Boolean?, id: Int?)

    @Query("SELECT * FROM whatsapp ORDER BY id ASC")
    fun getAllMessage() : LiveData<List<Message>>

    @Query("SELECT * FROM whatsapp WHERE id= :id ORDER BY id ASC")
    fun getActiveMessage(id: Int?) : LiveData<List<Message>>

    @Query("SELECT * FROM whatsapp WHERE status = :status ORDER BY id ASC")
    fun getDefaultMessage(status: Boolean?) : Message

    @Query("UPDATE whatsapp SET title= :title, message= :message, status = :status WHERE id = :id")
    suspend fun update(id: Int?, title: String?, message: String?, status: Boolean?)
}