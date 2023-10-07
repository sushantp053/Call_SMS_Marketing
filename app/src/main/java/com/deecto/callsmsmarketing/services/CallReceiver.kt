package com.deecto.callsmsmarketing.services

import android.Manifest
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.deecto.callsmsmarketing.R
import com.deecto.callsmsmarketing.database.*
import com.deecto.callsmsmarketing.model.BlockedContacts
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


class CallReceiver : BroadcastReceiver() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase
    private var dayCount: Int = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context?, intent: Intent?) {
        auth = Firebase.auth
        if (auth.currentUser != null) {

            val sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
            val dailySms = sharedPref.getBoolean("daily", true)
            val incoming = sharedPref.getBoolean("incoming", true)
            val outgoing = sharedPref.getBoolean("outgoing", true)
            val popup = sharedPref.getBoolean("popup", true)


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
                    if (number.isNotEmpty()) {
                        if (number.length >= 10) {
                            if (dailySms) {
                                lastCallCompare(context, number, false)
                            } else {
                                isBlocked(context, number, 1)
                            }

                        }
                    }
                }
            } else if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {

                val number: String =
                    intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        .toString()

                if (incoming) {
                    if (number.isNotEmpty()) {
                        if (number.length >= 10) {
                            if (dailySms) {
                                lastCallCompare(context, number, true)
                            } else {
//                            sendMsg(context, number)
                                isBlocked(context, number, 1)
                            }

                        }
                    }
                }
            } else if ((intent?.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE)) {
                if (popup) {
                    CoroutineScope(Dispatchers.Default).launch {
                        checkContactUserData(context, "0", 2)
                    }
                }
            }
        } else {
            Toast.makeText(
                context?.applicationContext,
                "SMS Marketing Not Started Please Login",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isBlocked(context: Context, number: String, type: Int) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        var isIAmBlocked = false
        CoroutineScope(Dispatchers.Default).launch {
            val blockedContactsDao: BlockedContactsDao = database.getBlockedContactsDao()
            try {
                isIAmBlocked = blockedContactsDao.isBlocked(number)
                Log.e("Returning Try Value", "Try $isIAmBlocked")
                if (!isIAmBlocked) {
                    checkContactUserData(context, number, type)
                } else {
                    Log.e("Blocked Contact", number)
                }
            } catch (e: NullPointerException) {
                Log.e("BLOCKED ERROR 132", e.toString())
                isIAmBlocked = false
            }
        }
    }

    private fun showToastMsg(c: Context, msg: String) {
        val toast = Toast.makeText(c, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun sendMsg(context: Context?, number: String) {

        Log.e("Call Ended ", number)
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        try {
            if (number.length >= 10) {
                if (number.length == 10) {
                    val a = Integer.parseInt(number.subSequence(0, 2).toString())
                    if (a > 55) {
                        CoroutineScope(Dispatchers.Default).launch {
                            val messageDio: MessageDao = database.getMessageDao()
                            val msg = messageDio.getDefaultMessage(true)
                            val smsManager: SmsManager = SmsManager.getDefault()

                            val current = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                            val formattedDate = current.format(formatter)
                            val daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
                            val dayDetails = daySMSCounterDao.getDayCount(formattedDate)

                            val sharedPref =
                                context.getSharedPreferences("Call", Context.MODE_PRIVATE)
                            val limit = sharedPref!!.getInt("limit", 100)
                            try {
                                if (dayDetails.counter == null) {
                                    if (limit > 0) {
//                                        smsManager.sendTextMessage(
//                                            number, null, msg.message, null, null
//                                        )
//                                        changeCounter(context)

                                        sendMultipartSMS(msg.message, number, context)
                                    } else {
                                        Toast.makeText(
                                            context.applicationContext,
                                            "Your SMS sending limit has reached. ",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                } else {
                                    if (limit > dayDetails.counter!!) {
//                                        smsManager.sendTextMessage(
//                                            number, null, msg.message, null, null
//                                        )
//                                        changeCounter(context)

                                        sendMultipartSMS(msg.message, number, context)
                                    } else {
                                        Toast.makeText(
                                            context.applicationContext,
                                            "Your SMS sending limit has reached. ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            } catch (e: NullPointerException) {
                                Log.e("Exception 198", e.toString())
                                if (limit > 0) {
//                                    smsManager.sendTextMessage(
//                                        number, null, msg.message, null, null
//                                    )
//                                    changeCounter(context)

                                    sendMultipartSMS(msg.message, number, context)
                                } else {
                                    Toast.makeText(
                                        context.applicationContext,
                                        "Your SMS sending limit has reached. ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                } else if (number.subSequence(0, 3) == "+91") {
//
                    CoroutineScope(Dispatchers.Default).launch {
                        val messageDio: MessageDao = database.getMessageDao()
                        val msg = messageDio.getDefaultMessage(true)

                        // on below line we are sending text message.

                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val formattedDate = current.format(formatter)
                        val daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
                        val dayDetails = daySMSCounterDao.getDayCount(formattedDate)
                        val sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE)
                        val limit = sharedPref!!.getInt("limit", 100)
                        try {
                            if (dayDetails.counter == null) {
                                if (limit > 0) {
//                                    smsManager.sendTextMessage(
//                                        number, null, msg.message, null, null
//                                    )
//                                    changeCounter(context)
                                    Log.e("Day Counter", "Not Found")
                                    sendMultipartSMS(msg.message, number, context)
                                } else {
                                    Toast.makeText(
                                        context.applicationContext,
                                        "Your SMS sending limit has reached. ",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            } else {
                                if (limit > dayDetails.counter!!) {
//                                    smsManager.sendTextMessage(
//                                        number, null, msg.message, null, null
//                                    )
//                                    changeCounter(context)

                                    Log.e("Day Limit", "Your are in Limit")
                                    sendMultipartSMS(msg.message, number, context)
                                } else {
                                    Toast.makeText(
                                        context.applicationContext,
                                        "Your SMS sending limit has reached. ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: NullPointerException) {
                            Log.e("Exception 264", e.toString())
                            if (limit > 0) {
//                                smsManager.sendTextMessage(
//                                    number, null, msg.message, null, null
//                                )
//                                changeCounter(context)

                                Log.e("Day Exception", "Found an exception")
                                sendMultipartSMS(msg.message, number, context)
                            } else {
                                Toast.makeText(
                                    context.applicationContext,
                                    "Your SMS sending limit has reached. ",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }


                    }
                }
            }

        } catch (e: Exception) {
            Log.e("SMS Sending Error 285", e.message.toString())
        }
    }

    private fun lastCallCompare(context: Context?, number: String, incoming: Boolean) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)

        CoroutineScope(Dispatchers.Default).launch {
            val phoneCallDao: PhoneCallDao = database.getPhoneCallDao()
            val phoneCallDetails = phoneCallDao.getLastCall(number)
            val current = LocalDateTime.now()

            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val formatted = current.format(formatter)
            try {
                val incomingDifferance =
                    parseLong(formatted.toString()) - parseLong(phoneCallDetails.incoming_time)
                val outgoingDifferance =
                    parseLong(formatted.toString()) - parseLong(phoneCallDetails.outgoing_time)
                if (incomingDifferance > (24 * 60 * 60) && outgoingDifferance > (24 * 60 * 60)) {
                    isBlocked(context, number, 1)
                } else {
                    Log.e("Hours are Lessor", "Last Call done in 24 hours")
                }
                if (incoming) {
                    phoneCallDao.updateIncomingTime(formatted.toString(), number)
                } else {
                    phoneCallDao.updateOutgoingTime(formatted.toString(), number)
                }
            } catch (e: Exception) {
                Log.e("Time comparing error 315", e.message.toString())
//                sendMsg(context, number)
                isBlocked(context, number, 1)
                val phoneCall: PhoneCall =
                    PhoneCall(null, number, formatted, formatted, "active", 1)
                phoneCallDao.insert(phoneCall)
            }

        }

    }

    private fun sendMultipartSMS(message: String?, number: String, context: Context?) {
        Log.e("Multipart Working", "In Function")
        val smsManager: SmsManager = SmsManager.getDefault()
        val messageParts = smsManager.divideMessage(message)
        val numParts = messageParts.size

        val sentIntents = mutableListOf<PendingIntent>()
        val deliveryIntents = mutableListOf<PendingIntent>()

        for (i in 0 until numParts) {
            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )
            val deliveryIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_DELIVERED"),
                PendingIntent.FLAG_IMMUTABLE
            )

            sentIntents.add(sentIntent)
            deliveryIntents.add(deliveryIntent)

            smsManager.sendTextMessage(number, null, messageParts[i], sentIntent, deliveryIntent)
            changeCounter(context)

            Log.e("Multipart Working", "Message Send")
        }
    }

    private fun changeCounter(context: Context?) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        CoroutineScope(Dispatchers.Default).launch {
            val daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
            val id = daySMSCounterDao.getDayId(formattedDate.toString())
            if (id == null) {
                val daySMSCounter = DaySMSCounter(null, formattedDate.toString(), 1)
                daySMSCounterDao.insert(daySMSCounter)
            } else {
                try {
                    daySMSCounterDao.updateDayCount(formattedDate.toString())
                } catch (e: NullPointerException) {
                    Log.e("Exception 339", e.toString())
                    val daySMSCounter = DaySMSCounter(null, formattedDate.toString(), 1)
                    daySMSCounterDao.insert(daySMSCounter)
                }
            }
        }
    }

    private fun changeWhatsAppCounter(context: Context?) {
        database = MessageDatabase.getDatabase(context!!.applicationContext)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        CoroutineScope(Dispatchers.Default).launch {
            val dayWhatsappCounterDao: DayWhatsappCounterDao = database.getDayWhatsappCounterDao()
            val id = dayWhatsappCounterDao.getDayId(formattedDate.toString())
            if (id == null) {
                val dayWhatsAppCounter =
                    DayWhatsappCounter(null, formattedDate.toString(), 1)
                dayWhatsappCounterDao.insert(dayWhatsAppCounter)
            } else {
                try {
                    dayWhatsappCounterDao.updateDayCount(formattedDate.toString())

                } catch (e: NullPointerException) {
                    Log.e("Whatsapp Exception 373", e.toString())
                    val dayWhatsAppCounter =
                        DayWhatsappCounter(null, formattedDate.toString(), 1)
                    dayWhatsappCounterDao.insert(dayWhatsAppCounter)
                }
            }
        }
    }

    private fun checkContactUserData(context: Context?, number: String, type: Int) {

        val current = LocalDateTime.now()
        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd")
        val selectedDate = current.format(formatter2)

        val sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        val whats = sharedPref.getInt("whats", R.id.btnOff)

        val db = Firebase.firestore


        Log.e(ContentValues.TAG, "Checking User Data")
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get().addOnSuccessListener { document ->
            if (document.data != null) {
//                Log.e(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")

                val days: Int = getDaysBetweenDates(
                    selectedDate.toString(),
                    document.data!!["end_date"].toString(),
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
                    if (type == 1) {
                        sendMsg(context, number)
                    } else {

                        when (whats) {
                            R.id.btnAsk -> {
                                showPopUp(context, number)
                            }
                            R.id.btnAuto -> {
                                CoroutineScope(Dispatchers.Default).launch {
                                    autoSend(context, number)
                                }
                            }
                            else -> {

                            }
                        }

                    }
                }

            } else {
                Log.e(ContentValues.TAG, "No such document")
//                                    RegisterUser
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }
    }

    private fun autoSend(context: Context?, number1: String) {

        val sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        sharedPref.edit().putBoolean("popup", false)
            .apply()

        database = MessageDatabase.getDatabase(context!!.applicationContext)
        var phoneNumber = number1
        val cr: ContentResolver = context!!.contentResolver
        val c = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " ASC")
        var totalCall = 1
        if (c != null) {
            totalCall = 1 // integer call log limit
            //starts pulling logs from last - you can use moveToFirst() for first logs
            if (c.moveToLast()) {
                for (j in 0 until totalCall) {
                    val phNumber = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    var direction: String? = null
                    when (c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)).toInt()) {
                        CallLog.Calls.OUTGOING_TYPE -> direction = "OUTGOING"
                        CallLog.Calls.INCOMING_TYPE -> direction = "INCOMING"
                        CallLog.Calls.MISSED_TYPE -> direction = "MISSED"
                        else -> {}
                    }
                    c.moveToPrevious() // if you used moveToFirst() for first logs, you should this line to moveToNext
                    phoneNumber = phNumber
                    if (phNumber != null) {
                        CoroutineScope(Dispatchers.Default).launch {
                            val messageDio: WhatsappDao = database.getWhatsappDao()
                            val blockedContactsDao: BlockedContactsDao =
                                database.getBlockedContactsDao()
                            try {
                                val msg = messageDio.getDefaultWhatsappMessage(true)
                                if (phoneNumber.subSequence(0, 3).equals("+91")) {
                                    phoneNumber.trim()
                                    phoneNumber = phoneNumber.removePrefix("+91")
                                    try {
                                        if (!blockedContactsDao.isBlocked(phoneNumber)) {
                                            val smsNumber = "91$phoneNumber"
                                            val sendIntent = Intent(Intent.ACTION_SEND)
                                            sendIntent.type = "text/plain"
                                            sendIntent.putExtra(
                                                Intent.EXTRA_TEXT, "${msg.message} " +
                                                        "\nMarketingwala CJ"
                                            )
                                            sendIntent.putExtra("jid", "$smsNumber@s.whatsapp.net")
                                            sendIntent.setPackage("com.whatsapp")
                                            sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            context?.applicationContext?.startActivity(sendIntent)

                                        } else {
                                            Log.e("AutoSend Blocked", "Blocked Contact")
                                        }
                                    } catch (e: NullPointerException) {
                                        Log.e("BLOCKED ERROR 482", e.toString())
                                    }

                                } else {
                                    try {
                                        if (!blockedContactsDao.isBlocked(phoneNumber)) {
                                            val smsNumber = "91$phoneNumber"
                                            val sendIntent = Intent(Intent.ACTION_SEND)
                                            sendIntent.type = "text/plain"
                                            sendIntent.putExtra(Intent.EXTRA_TEXT, "${msg.message}")
                                            sendIntent.putExtra("jid", "$smsNumber@s.whatsapp.net")
                                            sendIntent.setPackage("com.whatsapp")
                                            sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            context?.applicationContext?.startActivity(sendIntent)

                                        } else {
                                            Log.e("AutoSend Blocked", "Blocked Contact")
                                        }
                                    } catch (e: NullPointerException) {
                                        Log.e("BLOCKED ERROR 502", e.toString())
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("set text error 506", e.toString())
                            }
                            sharedPref.edit().putBoolean("popup", true)
                                .apply()
                        }

                    }
                }
            }

            c.close()
        }
        changeWhatsAppCounter(context)
    }

    fun LastCall(context: Context?): String? {
        val sb = StringBuffer()
        val cur: Cursor? = context!!.applicationContext.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " ASC"
        )
        val number: Int = cur?.getColumnIndex(CallLog.Calls.NUMBER)!!
        if (cur != null) {
            while (cur.moveToNext()) {
                val phNumber: String = cur.getString(number)
                sb.append("\nPhone Number:$phNumber")
                break
            }
        }
        cur.close()
        return sb.toString()
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

    private fun showPopUp(context: Context?, number: String) {

        val sharedPref = context?.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        sharedPref.edit().putBoolean("popup", false)
            .apply()

        val database: MessageDatabase = MessageDatabase.getDatabase(context!!.applicationContext)
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

//        floatingView.isFocusable = true

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

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.addView(floatingView, params)

        floatingView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                sharedPref.edit().putBoolean("popup", true)
                    .apply()
                windowManager.removeView(floatingView)
            }
        }
        floatingView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                Log.e("Error", "Home Key pressed")
                sharedPref.edit().putBoolean("popup", true)
                    .apply()
                windowManager.removeView(floatingView)
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e("Error", "Back Key pressed")
                sharedPref.edit().putBoolean("popup", true)
                    .apply()
                windowManager.removeView(floatingView)
            }
            Log.e("Key code", keyCode.toString())
            return@setOnKeyListener true

        }
        CoroutineScope(Dispatchers.Default).launch {
            val messageDio: WhatsappDao = database.getWhatsappDao()
            try {
                val msg = messageDio.getDefaultWhatsappMessage(true)
                textMsg.text = msg.message.toString()
                whatsMsg = msg.message.toString()
            } catch (e: Exception) {
                Log.e("set text error 639", e.toString())
            }
        }

        closeButton.setOnClickListener {
            sharedPref.edit().putBoolean("popup", true)
                .apply()
            windowManager.removeView(floatingView)
        }

        val cr: ContentResolver = context.contentResolver
        val c = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " ASC")
        var totalCall = 1
        if (c != null) {
            totalCall = 1 // integer call log limit
            //starts pulling logs from last - you can use moveToFirst() for first logs
            if (c.moveToLast()) {
                for (j in 0 until totalCall) {
                    val phNumber = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val n = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    val callDate = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    var direction: String? = null
                    when (c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)).toInt()) {
                        CallLog.Calls.OUTGOING_TYPE -> direction = "OUTGOING"
                        CallLog.Calls.INCOMING_TYPE -> direction = "INCOMING"
                        CallLog.Calls.MISSED_TYPE -> direction = "MISSED"
                        else -> {}
                    }
                    c.moveToPrevious() // if you used moveToFirst() for first logs, you should this line to moveToNext
                    phoneNumber = phNumber
                    textNumber.text = phNumber
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
            sharedPref.edit().putBoolean("popup", true)
                .apply()
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            dialIntent.data = Uri.parse("tel:$phoneNumber")
            context.applicationContext.startActivity(dialIntent)

            windowManager.removeView(floatingView)
        }
        reminderButton.setOnClickListener { }
        blackListButton.setOnClickListener {
            sharedPref.edit().putBoolean("popup", true)
                .apply()

            CoroutineScope(Dispatchers.Default).launch {
                val blockedContactsDao: BlockedContactsDao = database.getBlockedContactsDao()
                try {
                    val blockedContacts = BlockedContacts(
                        null,
                        textNumber.text.toString(),
                        textName.text.toString()
                    )
                    blockedContactsDao.insert(blockedContacts)

                } catch (e: NullPointerException) {
                }
            }
            Toast.makeText(
                context.applicationContext,
                "Added to blocklist",
                Toast.LENGTH_SHORT
            ).show()
            windowManager.removeView(floatingView)
        }
        groupButton.setOnClickListener { }
        whatsAppButton.setOnClickListener {

            sharedPref.edit().putBoolean("popup", true)
                .apply()
            changeWhatsAppCounter(context)
            if (phoneNumber.subSequence(0, 3).equals("+91")) {

                phoneNumber.trim()
                phoneNumber = phoneNumber.removePrefix("+91")
                val u = "https://wa.me/91$phoneNumber?text=$whatsMsg"
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            } else {
                val u: String = "https://wa.me/91" + phoneNumber.trim() + "?text=$whatsMsg"
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            }
            windowManager.removeView(floatingView)

        }
        saveContactButton.setOnClickListener {

            sharedPref.edit().putBoolean("popup", true)
                .apply()

            val intent = Intent(
                ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                Uri.parse("tel:" + phoneNumber)
            )
            intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            windowManager.removeView(floatingView)
        }
    }
}



