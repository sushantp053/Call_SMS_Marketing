package com.deecto.callsmsmarketing.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deecto.callsmsmarketing.database.BlockedContactsRepository
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockedContactViewModel (application: Application) : AndroidViewModel(application) {
    private val repository : BlockedContactsRepository

    val allContacts : LiveData<List<BlockedContacts>>

    init {
        val dao = MessageDatabase.getDatabase(application).getBlockedContactsDao()
        repository = BlockedContactsRepository(dao)
        allContacts = repository.allContactsList
    }

    fun insertContact(blockedContacts:BlockedContacts?) = viewModelScope.launch(Dispatchers.IO) {
        blockedContacts?.let { repository.insert(it) }
    }

    fun removeContact(blockedContacts:BlockedContacts?) = viewModelScope.launch(Dispatchers.IO) {
        blockedContacts?.let { repository.remove(it) }
    }

}
