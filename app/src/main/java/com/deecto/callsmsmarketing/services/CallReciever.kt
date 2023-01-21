package com.deecto.callsmsmarketing.services

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReciever : BroadcastReceiver() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        auth = Firebase.auth
        if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            showToastMsg(context!!, "Deecto Phone Call Started" + intent?.dataString)
        } else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE) {
            showToastMsg(context!!, "Deecto Phone Call Ended")
        } else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
            val number: String =
                intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).toString()
            val scAdd: String? =
                intent!!.getStringExtra((TelephonyManager.EXTRA_SPECIFIC_CARRIER_ID))
            showToastMsg(context!!, "Deecto Incoming Call $number $scAdd")
            try {
                if (number.length >= 10) {
                    if (number.subSequence(0, 3).equals("+91")) {
                        database = MessageDatabase.getDatabase(context)

                        CoroutineScope(Dispatchers.Default).launch {
                            var messageDio: MessageDao = database.getMessageDao()
                            var msg = messageDio.getDefaultMessage(true)

                            Log.e(
                                "Error Code",
                                "Code Matched ${number.subSequence(3, 5)} $number"
                            )
                            val smsManager: SmsManager = SmsManager.getDefault()
                            // on below line we are sending text message.
                            smsManager.sendTextMessage(
                                number,
                                null,
                                msg.message,
                                null,
                                null
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(
                    context, "SMS Sending Error" + e.message.toString(), Toast.LENGTH_LONG
                ).show()
                Log.e("SMS Sending Error", e.message.toString())
            }
        }
    }

    fun showToastMsg(c: Context, msg: String) {
        val toast = Toast.makeText(c, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}