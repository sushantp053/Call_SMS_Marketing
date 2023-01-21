package com.deecto.callsmsmarketing.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository : MessageRepository

    val allMessage : LiveData<List<Message>>

    init {
        val dao = MessageDatabase.getDatabase(application).getMessageDao()
        repository = MessageRepository(dao)
        allMessage = repository.allMessage
    }

    fun deleteMessage(message: Message) = viewModelScope.launch(Dispatchers.IO) {

        repository.delete(message)
    }
    fun updateStatus(message: Message) = viewModelScope.launch(Dispatchers.IO) {

        repository.updateStatus(message)
    }
    fun insertMessage(message:Message?) = viewModelScope.launch(Dispatchers.IO) {

        message?.let { repository.insert(it) }
    }
    fun updateMessage(message: Message) = viewModelScope.launch(Dispatchers.IO) {

        repository.update(message)
    }
}