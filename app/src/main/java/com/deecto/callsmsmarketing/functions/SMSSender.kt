package com.deecto.callsmsmarketing.functions

import android.content.Context
import android.telephony.SmsManager

class SMSSender {


    fun sendSms(context: Context, number: String, msg : String){
        val smsManager: SmsManager = SmsManager.getDefault()

    }
    fun sendWhatsapp(context: Context, number: String, msg : String){

    }
}