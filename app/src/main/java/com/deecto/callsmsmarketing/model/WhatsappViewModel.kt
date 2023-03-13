package com.deecto.callsmsmarketing.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.WhatsappRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhatsappViewModel(application: Application) : AndroidViewModel(application) {
    private val repository : WhatsappRepository

    val allWhatsapp : LiveData<List<WhatsappMessage>>

    init {
        val dao = MessageDatabase.getDatabase(application).getWhatsappDao()
        repository = WhatsappRepository(dao)
        allWhatsapp = repository.allWhatsappMessage
    }

    fun deleteWhatsapp(message: WhatsappMessage) = viewModelScope.launch(Dispatchers.IO) {

        repository.delete(message)
    }
    fun updateStatus(message: WhatsappMessage) = viewModelScope.launch(Dispatchers.IO) {

        repository.updateStatus(message)
    }
    fun insertWhatsapp(message:WhatsappMessage?) = viewModelScope.launch(Dispatchers.IO) {

        message?.let { repository.insert(it) }
    }
    fun updateWhatsapp(message: WhatsappMessage) = viewModelScope.launch(Dispatchers.IO) {

        repository.update(message)
    }
}