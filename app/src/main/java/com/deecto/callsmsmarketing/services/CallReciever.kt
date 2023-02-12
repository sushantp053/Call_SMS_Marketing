package com.deecto.callsmsmarketing.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.PhoneCallDao
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.model.PhoneCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Long.parseLong
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CallReciever : BroadcastReceiver() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context?, intent: Intent?) {
        auth = Firebase.auth
        if (auth.currentUser != null) {
            context?.let { showToastMsg(it.applicationContext, "Your Auth has been Authentic") }
            Log.e("Authentic", "Yes I am Authentic")

            var sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
            val dailySms = sharedPref.getBoolean("daily", false)
            val incoming = sharedPref.getBoolean("incoming", false)
            val outgoing = sharedPref.getBoolean("outgoing", false)

            var tm: TelephonyManager =
                context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                val number: String =
                    intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).toString()

                if (outgoing) {
                    if (number != null) {
                        if (dailySms) {
                            lastCallCompare(context, number, false)
                        } else {
                            sendMsg(context, number)
                        }
                    }
                }
            } else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
                val number: String =
                    intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).toString()
                if (incoming) {
                    if (number != null) {
                        if (dailySms) {
                            lastCallCompare(context, number, true)
                        } else {
                            sendMsg(context, number)
                        }
                    }
                }
            }
        }else{
            Toast.makeText(context?.applicationContext, "SMS Marketing Not Started Please Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastMsg(c: Context, msg: String) {
        val toast = Toast.makeText(c, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun sendMsg(context: Context?, number: String) {

        database = MessageDatabase.getDatabase(context!!.applicationContext)
        Log.e("Send SMS Method", "Method Called")
        try {
            if (number.length >= 10) {
                Log.e("Length Matched", "Greater than 10 digits")
                if (number.length == 10) {
                    var a = Integer.parseInt(number.subSequence(0, 2).toString())
                    if (a > 55) {
                        CoroutineScope(Dispatchers.Default).launch {
                            var messageDio: MessageDao = database.getMessageDao()
                            var msg = messageDio.getDefaultMessage(true)
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
                        changeCounter(context)
                    }

                } else if (number.subSequence(0, 3).equals("+91")) {

                    Log.e(
                        "Error Code",
                        "Code Matched ${number.subSequence(3, 5)} $number"
                    )
                    CoroutineScope(Dispatchers.Default).launch {
                        var messageDio: MessageDao = database.getMessageDao()
                        var msg = messageDio.getDefaultMessage(true)
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
                    changeCounter(context)
                }
            }

        } catch (e: Exception) {
            Log.e("SMS Sending Error", e.message.toString())
        }
    }

    private fun lastCallCompare(context: Context?, number: String, incoming: Boolean) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)

        CoroutineScope(Dispatchers.Default).launch {
            var phoneCallDao: PhoneCallDao = database.getPhoneCallDao()
            var phoneCallDetails = phoneCallDao.getLastCall(number)
            val current = LocalDateTime.now()

            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val formatted = current.format(formatter)
            try {
                val incomingDifferance =
                    parseLong(formatted.toString()) - parseLong(phoneCallDetails.incoming_time)
                val outgoingDifferance =
                    parseLong(formatted.toString()) - parseLong(phoneCallDetails.outgoing_time)
                if (incomingDifferance > (24 * 60 * 60) && outgoingDifferance > (24 * 60 * 60)) {
                    sendMsg(context, number)
                    Log.e(
                        "Compare Worked",
                        "Compared $outgoingDifferance and incoming $incomingDifferance"
                    )
                } else {
                    Log.e("Hours are Lessor", "Last Call done in 24 hours")
                }
                if (incoming) {
                    phoneCallDao.updateIncomingTime(formatted.toString(), number)
                } else {
                    phoneCallDao.updateOutgoingTime(formatted.toString(), number)
                }
            } catch (e: Exception) {
                Log.e("Time comparing error", e.message.toString())
                sendMsg(context, number)
                var phoneCall: PhoneCall =
                    PhoneCall(null, number, formatted, formatted, "active", 1)
                phoneCallDao.insert(phoneCall)
            }

        }

    }

    private fun changeCounter(context: Context?) {
        Log.e("Counter", "Day Change Counter Called")
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        CoroutineScope(Dispatchers.Default).launch {
            var daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
            try {
                val dayDetails = daySMSCounterDao.getDayCount(formattedDate)
                daySMSCounterDao.updateDayCount(formattedDate.toString())

                Log.e("Counter", "Day Change Counter Called")

            } catch (e: Exception) {

                Log.e("Counter Error", e.toString())
                var daySMSCounter: DaySMSCounter = DaySMSCounter(null, formattedDate.toString(), 1)
                daySMSCounterDao.insert(
                    daySMSCounter
                )
            }
        }
    }
}
