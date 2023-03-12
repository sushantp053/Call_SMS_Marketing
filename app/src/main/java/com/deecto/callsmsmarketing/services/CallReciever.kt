package com.deecto.callsmsmarketing.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.deecto.callsmsmarketing.R
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.PhoneCallDao
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.model.PhoneCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Long.parseLong
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class CallReciever : BroadcastReceiver() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase
    private var dayCount: Int = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context?, intent: Intent?) {
        auth = Firebase.auth
        if (auth.currentUser != null) {

            var sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
            val dailySms = sharedPref.getBoolean("daily", false)
            val incoming = sharedPref.getBoolean("incoming", false)
            val outgoing = sharedPref.getBoolean("outgoing", false)
            val limit = sharedPref.getInt("limit", 100)


            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_NUMBERS
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                val number: String =
                    intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).toString()

                if (outgoing) {
                    if (number != null) {
                        if (dailySms) {
                            lastCallCompare(context, number, false)
                        } else {
                            checkUserData(context, number)
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
//                            sendMsg(context, number)
                            checkUserData(context, number)
                        }
                    }
                }
            } else if ((intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE)) {
//                Toast.makeText(context?.applicationContext, "Idle State", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context?.applicationContext,
                "SMS Marketing Not Started Please Login",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showToastMsg(c: Context, msg: String) {
        val toast = Toast.makeText(c, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun sendMsg(context: Context?, number: String) {

        database = MessageDatabase.getDatabase(context!!.applicationContext)
        try {
            if (number.length >= 10) {
                if (number.length == 10) {
                    var a = Integer.parseInt(number.subSequence(0, 2).toString())
                    if (a > 55) {
                        CoroutineScope(Dispatchers.Default).launch {
                            var messageDio: MessageDao = database.getMessageDao()
                            var msg = messageDio.getDefaultMessage(true)
                            val smsManager: SmsManager = SmsManager.getDefault()

                            val current = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                            val formattedDate = current.format(formatter)
                            var daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
                            val dayDetails = daySMSCounterDao.getDayCount(formattedDate)

                            var sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE)
                            val limit = sharedPref!!.getInt("limit", 100)
                            if (limit > dayDetails.counter!!) {
                                // on below line we are sending text message.
                                smsManager.sendTextMessage(
                                    number, null, msg.message, null, null
                                )

                                changeCounter(context)
                            }
                            else{
                                Toast.makeText(
                                    context,
                                    "Your Limit has been over",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else if (number.subSequence(0, 3).equals("+91")) {
//
                    CoroutineScope(Dispatchers.Default).launch {
                        var messageDio: MessageDao = database.getMessageDao()
                        var msg = messageDio.getDefaultMessage(true)
                        val smsManager: SmsManager = SmsManager.getDefault()
                        // on below line we are sending text message.

                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val formattedDate = current.format(formatter)
                        var daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
                        val dayDetails = daySMSCounterDao.getDayCount(formattedDate)

                        var sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE)
                        val limit = sharedPref!!.getInt("limit", 100)
                        if (limit > dayDetails.counter!!) {
                            smsManager.sendTextMessage(
                                number, null, msg.message, null, null
                            )

                            changeCounter(context)
                        }else{
                        }
                    }
//                    showPopUp(context)
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
//                    sendMsg(context, number)
                    checkUserData(context, number)
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
//                sendMsg(context, number)
                checkUserData(context, number)
                var phoneCall: PhoneCall =
                    PhoneCall(null, number, formatted, formatted, "active", 1)
                phoneCallDao.insert(phoneCall)
            }

        }

    }

    private fun changeCounter(context: Context?) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        CoroutineScope(Dispatchers.Default).launch {
            var daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
            try {
                val dayDetails = daySMSCounterDao.getDayCount(formattedDate)
                daySMSCounterDao.updateDayCount(formattedDate.toString())


            } catch (e: Exception) {

                var daySMSCounter: DaySMSCounter = DaySMSCounter(null, formattedDate.toString(), 1)
                daySMSCounterDao.insert(
                    daySMSCounter
                )
            }
        }
    }

    private fun checkUserData(context: Context?, number: String) {

        val current = LocalDateTime.now()
        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd")
        val selectedDate = current.format(formatter2)

        val db = Firebase.firestore

        Log.e(ContentValues.TAG, "Checking User Data")
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get().addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.e(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")

                    val days: Int = getDaysBetweenDates(
                        selectedDate.toString(),
                        document.data!!.get("end_date").toString(),
                        "yyyyMMdd"
                    )
                    if (days <= 0) {
                        context?.let { showToastMsg(it.applicationContext, "SMS not able send.") }
                        context?.let {
                            showToastMsg(
                                it.applicationContext, "Your Account has been expired."
                            )
                        }
                    } else {
                        sendMsg(context, number)
                    }

                } else {
                    Log.e(ContentValues.TAG, "No such document")
//                                    RegisterUser
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    fun getDaysBetweenDates(
        firstDateValue: String, secondDateValue: String, format: String
    ): Int {
        val sdf = SimpleDateFormat(format, Locale.getDefault())

        val firstDate = sdf.parse(firstDateValue)
        val secondDate = sdf.parse(secondDateValue)

        if (firstDate == null || secondDate == null) return 0

        return (((secondDate.time - firstDate.time) / (1000 * 60 * 60 * 24)) + 1).toInt()
    }

    fun showPopUp(context: Context?) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0

        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val floatingView = inflater.inflate(R.layout.floating_window_layout, null)

        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)
    }

}
