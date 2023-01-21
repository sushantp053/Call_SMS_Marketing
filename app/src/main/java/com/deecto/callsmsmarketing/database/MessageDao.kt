package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deecto.callsmsmarketing.model.Message

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("UPDATE message_table SET status = :status")
    suspend fun updateAllStatus(status: Boolean?)

    @Query("UPDATE message_table SET status = :status WHERE id = :id")
    suspend fun updateCurrentStatus(status: Boolean?, id: Int?)

    @Query("SELECT * FROM message_table ORDER BY id ASC")
    fun getAllMessage() : LiveData<List<Message>>

    @Query("SELECT * FROM message_table WHERE id= :id ORDER BY id ASC")
    fun getActiveMessage(id: Int?) : LiveData<List<Message>>

    @Query("SELECT * FROM message_table WHERE status = :status ORDER BY id ASC")
    fun getDefaultMessage(status: Boolean?) : Message

    @Query("UPDATE message_table SET title= :title, message= :message, status = :status WHERE id = :id")
    suspend fun update(id: Int?, title: String?, message: String?, status: Boolean?)
}