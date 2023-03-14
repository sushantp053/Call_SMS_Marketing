package com.deecto.callsmsmarketing.services

import android.Manifest
import android.R.attr
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.deecto.callsmsmarketing.database.DayWhatsappCounterDao
import com.deecto.callsmsmarketing.R
import com.deecto.callsmsmarketing.database.*
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.model.DayWhatsappCounter
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
            val dailySms = sharedPref.getBoolean("daily", true)
            val incoming = sharedPref.getBoolean("incoming", true)
            val outgoing = sharedPref.getBoolean("outgoing", true)

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
                Toast.makeText(context?.applicationContext, "Idle State", Toast.LENGTH_SHORT).show()
                showPopUp(context)
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

                            var sharedPref =
                                context?.getSharedPreferences("Call", Context.MODE_PRIVATE)
                            val limit = sharedPref!!.getInt("limit", 100)
                            if (limit > dayDetails.counter!!) {
                                // on below line we are sending text message.
                                smsManager.sendTextMessage(
                                    number, null, msg.message, null, null
                                )
                                changeCounter(context)
                            } else {
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
                        } else {
                        }
                    }
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
    private fun changeWhatsAppCounter(context: Context?) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        CoroutineScope(Dispatchers.Default).launch {
            var dayWhatsappCounterDao: DayWhatsappCounterDao = database.getDayWhatsappCounterDao()
            try {
                val dayDetails = dayWhatsappCounterDao.getDayCount(formattedDate)
                dayWhatsappCounterDao.updateDayCount(formattedDate.toString())

            } catch (e: Exception) {

                var dayWhatsAppCounter: DayWhatsappCounter = DayWhatsappCounter(null, formattedDate.toString(), 1)
                dayWhatsappCounterDao.insert(
                    dayWhatsAppCounter
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

    private fun getDaysBetweenDates(
        firstDateValue: String, secondDateValue: String, format: String
    ): Int {
        val sdf = SimpleDateFormat(format, Locale.getDefault())

        val firstDate = sdf.parse(firstDateValue)
        val secondDate = sdf.parse(secondDateValue)

        if (firstDate == null || secondDate == null) return 0

        return (((secondDate.time - firstDate.time) / (1000 * 60 * 60 * 24)) + 1).toInt()
    }

    private fun showPopUp(context: Context?) {

        var database: MessageDatabase = MessageDatabase.getDatabase(context!!.applicationContext)
        var whatsMsg: String = "HI"

        val closeButton: ImageButton
        val callButton: Button
        val reminderButton: Button
        val blackListButton: Button
        val groupButton: Button
        val whatsAppButton: Button
        val saveContactButton: Button

        val textName: TextView
        val textNumber: TextView
        val textMsg: TextView


        var phoneNumber: String = auth.currentUser?.phoneNumber.toString()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.END


        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val floatingView = inflater.inflate(R.layout.floating_window_layout, null)

        floatingView.isFocusable = true

        callButton = floatingView.findViewById(R.id.makeACallBtn)
        reminderButton = floatingView.findViewById(R.id.reminderBtn)
        blackListButton = floatingView.findViewById(R.id.blackListButton)
        groupButton = floatingView.findViewById(R.id.addToGroupButton)
        whatsAppButton = floatingView.findViewById(R.id.sendWhatsappBtn)
        saveContactButton = floatingView.findViewById(R.id.addToContactButton)

        closeButton = floatingView.findViewById(R.id.closeBtn)
        textName = floatingView.findViewById(R.id.contactName)
        textNumber = floatingView.findViewById(R.id.contactMobile)
        textMsg = floatingView.findViewById(R.id.messageText)
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.addView(floatingView, params)

        CoroutineScope(Dispatchers.Default).launch {
            var messageDio: WhatsappDao = database.getWhatsappDao()
            var msg = messageDio.getDefaultWhatsappMessage(true)
            try {
                textMsg.text = msg.message.toString()
                whatsMsg = msg.message.toString()
            } catch (e: Exception) {
                Log.e("set text error", e.toString())
            }
        }

        closeButton.setOnClickListener {

            windowManager.removeView(floatingView);
        }
        floatingView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                windowManager.removeView(floatingView)
            }
        }
        val cr: ContentResolver = context!!.getContentResolver()
        val c = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null)
        var totalCall = 1
        if (c != null) {
            totalCall = 1 // intenger call log limit
            if (c.moveToLast()) { //starts pulling logs from last - you can use moveToFirst() for first logs
                for (j in 0 until totalCall) {
                    val phNumber = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val n = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    val callDate = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val callDuration = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val dateFormat = Date(java.lang.Long.valueOf(callDate))
                    val callDayTimes = java.lang.String.valueOf(dateFormat)
                    var direction: String? = null
                    when (c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)).toInt()) {
                        CallLog.Calls.OUTGOING_TYPE -> direction = "OUTGOING"
                        CallLog.Calls.INCOMING_TYPE -> direction = "INCOMING"
                        CallLog.Calls.MISSED_TYPE -> direction = "MISSED"
                        else -> {}
                    }
                    c.moveToPrevious() // if you used moveToFirst() for first logs, you should this line to moveToNext
                    phoneNumber = "$phNumber"
                    textNumber.text = "$phNumber"
                    if (n != null) {
                        textName.text = "Name : $n "
                    } else {
                        textName.text = "Unknown"
                        saveContactButton.visibility = View.VISIBLE
                    }
                }
            }
            c.close()
        }
        callButton.setOnClickListener {

            windowManager.removeView(floatingView)
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            dialIntent.data = Uri.parse("tel:" + phoneNumber)
            context.applicationContext.startActivity(dialIntent)
        }
        reminderButton.setOnClickListener { }
        blackListButton.setOnClickListener { }
        groupButton.setOnClickListener { }
        whatsAppButton.setOnClickListener {
            changeWhatsAppCounter(context)
            windowManager.removeView(floatingView)
            if (phoneNumber.subSequence(0, 3).equals("+91")) {

                phoneNumber.trim()
                phoneNumber = phoneNumber.removePrefix("+91")
                val u: String = "https://wa.me/91" + phoneNumber+"?text=$whatsMsg"
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            } else {
                val u: String = "https://wa.me/91" + phoneNumber.trim()+"?text=$whatsMsg"
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            }
        }
        saveContactButton.setOnClickListener {

            windowManager.removeView(floatingView)
            val intent = Intent(
                ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                Uri.parse("tel:" + phoneNumber)
            )
            intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}




