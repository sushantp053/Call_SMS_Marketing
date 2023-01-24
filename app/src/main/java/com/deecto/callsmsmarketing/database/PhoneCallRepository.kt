package com.deecto.callsmsmarketing.database

import com.deecto.callsmsmarketing.model.PhoneCall

class PhoneCallRepository (private val phoneCallDao: PhoneCallDao) {

    suspend fun insert(phoneCall: PhoneCall){
        phoneCallDao.insert(phoneCall)
    }

}