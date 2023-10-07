package com.deecto.callsmsmarketing

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase
    private val db = Firebase.firestore
    private var limit: Int = 100

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            val telephonyManager =
                this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val numberOfActiveSubscriptions = telephonyManager.phoneCount
//            val subscriptionManager = SubscriptionManager.from(this@MainActivity)
//            val subscriptions = subscriptionManager.activeSubscriptionInfoList
            if (numberOfActiveSubscriptions >= 2) {
//                Log.e("Number of Sim", "Two in Numbers")
//                val operatorName = telephonyManager.simOperatorName
//                Log.e("Operator 1" , operatorName.toString() )
//                Log.e("Operator 2" , operatorName.toString())
//                val phoneNumber = telephonyManager.getLine1Number()
//                Log.e("Phone 1" , phoneNumber.toString() )

                val subscriptionManager = getSystemService(SubscriptionManager::class.java)
                val telephonyManager = getSystemService(TelephonyManager::class.java)

                subscriptionManager?.let {
                    val activeSubscriptionInfoList = it.activeSubscriptionInfoList

                    activeSubscriptionInfoList?.let { activeList ->
                        for (subscriptionInfo in activeList) {
                            val subId = subscriptionInfo.subscriptionId
//                            val operatorName = telephonyManager?.getSimOperatorName()
//                            val phoneNumber = telephonyManager?.getMeid(1)
//                            Log.e("Error", subId.toString() + phoneNumber)
                            println("Operator Name: ${subscriptionInfo.carrierName}")
                            println("Phone Number: ${subscriptionInfo}")
                            if (subscriptionInfo.simSlotIndex == 0){
                                binding.sim1Text.text = subscriptionInfo.carrierName
                            }else{
                                binding.sim2Text.text = subscriptionInfo.carrierName
                            }
                        }
                    }
                }
            } else if (numberOfActiveSubscriptions == 1) {
                // There is only one active SIM card
                Log.e("Number of Sim", "1 Sim available")
            } else {
                // No active SIM cards are found
                Log.e("Number of Sim", "No SIM available")
            }
        }
        val webUrl: String = intent.getStringExtra("web_url").toString()
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
//            checkUserData()
        }

        database = MessageDatabase.getDatabase(this)
        if (webUrl.isNotEmpty() && webUrl.length > 5) {
            binding.attachLinkCard.isVisible = true
            binding.webUrlTV.text = webUrl
        } else {
            binding.attachLinkCard.isVisible = false
        }

        CoroutineScope(Dispatchers.Default).launch {
            var messageDio: MessageDao = database.getMessageDao()
            try {
                var msg = messageDio.getDefaultMessage(true)
                binding.textViewCurrentMsg.text = msg.message
            } catch (e: Exception) {
                binding.textViewCurrentMsg.text = "No Message Added"
                Log.e("set text error", e.toString())
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val formattedDate = current.format(formatter)
            var daySMSCounterDao: DaySMSCounterDao = database.getDaySMSCounterDao()
            try {
                val dayDetails = daySMSCounterDao.getDayCount(formattedDate)
                val totalCount = daySMSCounterDao.getTotalCount()

                binding.totalSmsCount.text = totalCount.toString()
                binding.dailySmsCount.text = dayDetails.counter.toString()

            } catch (e: Exception) {
                Log.e("Set Count Error", e.toString())
                binding.dailySmsCount.text = "0"
                var daySMSCounter: DaySMSCounter = DaySMSCounter(null, formattedDate.toString(), 0)
                daySMSCounterDao.insert(
                    daySMSCounter
                )
            }
        }

        val sharedPref = this.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        val dailySms = sharedPref.getBoolean("daily", true)
        val incoming = sharedPref.getBoolean("incoming", true)
        val outgoing = sharedPref.getBoolean("outgoing", true)
        val attachLink = sharedPref.getBoolean("sms_link", true)
        limit = sharedPref.getInt("limit", 100)

        binding.dailyOne.isChecked = dailySms
        binding.incomingCallSwitch.isChecked = incoming
        binding.outGoingCallSwitch.isChecked = outgoing
        binding.attachLinkSwitch.isChecked = attachLink
        binding.limitText.text = "$limit SMS"

        binding.smsLimitLayout.setOnClickListener {
            withEditText()
        }
        binding.dailyOne.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("daily", isChecked)
                apply()
            }
        }
        binding.incomingCallSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("incoming", isChecked)
                apply()
            }
        }
        binding.outGoingCallSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("outgoing", isChecked)
                apply()
            }
        }
        binding.attachLinkSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("sms_link", isChecked)
                apply()
            }
            with(sharedPref.edit()) {
                putString("webUrl", webUrl)
                apply()
            }
        }

        binding.shareWebSite.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Website")
                var shareMessage = "\nVisit my website\n\n$webUrl"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
        binding.changeMessageCard.setOnClickListener {
            val i = Intent(
                this,
                MessageActivity::class.java
            )
            startActivity(i)
        }
        binding.logoutCard.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sign Out")
            builder.setMessage("Do you really want to Sign Out. To Use app you need to Login.")
            builder.setPositiveButton("Sign Out") { dialog, which ->
                auth.signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                dialog.dismiss()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            builder.show()

        }
    }

    private fun checkUserData() {

        Log.e(ContentValues.TAG, "Checking User Data")
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.e(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")


                } else {
                    Log.e(ContentValues.TAG, "No such document")
//                                    RegisterUser
                    startActivity(Intent(this, RegisterUser::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    private fun withEditText() {
        val sharedPref = this.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Set Limit")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setText("$limit")
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { dialogInterface, i ->
            Toast.makeText(
                applicationContext,
                "New Limit set to " + editText.text.toString(),
                Toast.LENGTH_SHORT
            ).show()
            sharedPref.edit().putInt("limit", editText.text.toString().toInt())
                .apply()
            binding.limitText.setText("${editText.text.toString().toInt()} SMS")
        }
        builder.show()
    }
}

