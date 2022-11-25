package com.deecto.callsmsmarketing.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast

class CallReciever : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            showToastMsg(context!!, "Deecto Phone Call Started" + intent?.dataString)
        } else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE) {
            showToastMsg(context!!, "Deecto Phone Call Ended")
        }
        else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
            val number: String =
                intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).toString()
            showToastMsg(context!!, "Deecto Incoming Call $number")
            try {

                val smsManager: SmsManager = SmsManager.getDefault()
                // on below line we are sending text message.
                smsManager.sendTextMessage(
                    number,
                    null,
                    "Thank You for calling Sushant we will get back to you soon.",
                    null,
                    null
                )
                Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {

                // on catch block we are displaying toast message for error.
                Toast.makeText(
                    context, "Please enter all the data.." + e.message.toString(), Toast.LENGTH_LONG
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