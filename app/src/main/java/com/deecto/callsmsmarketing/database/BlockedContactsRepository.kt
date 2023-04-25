package com.deecto.callsmsmarketing.database

import androidx.lifecycle.LiveData
import com.deecto.callsmsmarketing.model.BlockedContacts
import com.deecto.callsmsmarketing.model.Message

class BlockedContactsRepository (private val blockedContactsDao: BlockedContactsDao){

    val allContactsList : LiveData<List<BlockedContacts>> = blockedContactsDao.getAllContacts()

    suspend fun insert(blockedContacts: BlockedContacts){
        blockedContactsDao.insert(blockedContacts)
    }
    suspend fun remove(blockedContacts: BlockedContacts){
        blockedContactsDao.delete(blockedContacts)
    }
}