package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import com.deecto.callsmsmarketing.model.Message

class MessageRepository(private val messageDao: MessageDao) {
    val allMessage : LiveData<List<Message>> = messageDao.getAllMessage()

    suspend fun insert(message: Message){
        messageDao.insert(message)
    }
    suspend fun delete(message: Message){
        messageDao.delete(message)
    }
    suspend fun update( message: Message){
        messageDao.update(message.id,message.title, message.message, message.status)
    }

    suspend fun updateStatus( message: Message){
        messageDao.updateAllStatus(false)
        messageDao.updateCurrentStatus(true, message.id)
    }
}