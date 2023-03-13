package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import com.deecto.callsmsmarketing.model.WhatsappMessage

class WhatsappRepository(private val whatsappDao: WhatsappDao) {
    val allWhatsappMessage : LiveData<List<WhatsappMessage>> = whatsappDao.getAllWhatsappMessage()

    suspend fun insert(message: WhatsappMessage){
        whatsappDao.insert(message)
    }
    suspend fun delete(message: WhatsappMessage){
        whatsappDao.delete(message)
    }
    suspend fun update( message: WhatsappMessage){
        whatsappDao.update(message.id,message.title, message.message, message.status)
    }

    suspend fun updateStatus( message: WhatsappMessage){
        whatsappDao.updateAllStatus(false)
        whatsappDao.updateCurrentStatus(true, message.id)
    }
}