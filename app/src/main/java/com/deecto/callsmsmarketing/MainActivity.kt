package com.deecto.callsmsmarketing

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.services.ManagePermissions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: MessageDatabase
    private val permissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }else{
            checkUserData()
        }

        database = MessageDatabase.getDatabase(this)


        CoroutineScope(Dispatchers.Default).launch {
            var messageDio: MessageDao = database.getMessageDao()
            var msg = messageDio.getDefaultMessage(true)
            try {
                textViewCurrentMsg.text = msg.message
            } catch (e: Exception) {
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

                    totalSmsCount.text = totalCount.toString()
                    dailySmsCount.text = dayDetails.counter.toString()

                } catch (e: Exception) {
                    Log.e("Set Count Error", e.toString())
                    dailySmsCount.text = "0"
                    var daySMSCounter: DaySMSCounter = DaySMSCounter(null, formattedDate.toString(), 0)
                    daySMSCounterDao.insert(
                        daySMSCounter
                    )
                }
        }

        val sharedPref = this.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        val dailySms = sharedPref.getBoolean("daily", false)
        val incoming = sharedPref.getBoolean("incoming", false)
        val outgoing = sharedPref.getBoolean("outgoing", false)
        dailyOne.isChecked = dailySms
        incomingCallSwitch.isChecked = incoming
        outGoingCallSwitch.isChecked = outgoing
        dailyOne.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("daily", isChecked)
                apply()
            }
        }
        incomingCallSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("incoming", isChecked)
                apply()
            }
        }
        outGoingCallSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("outgoing", isChecked)
                apply()
            }
        }
        val list = listOf<String>(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS
        )
        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this,list,permissionsRequestCode)

        managePermissions.checkPermissions()

        changeMessageCard.setOnClickListener {
            val i = Intent(
                this,
                MessageActivity::class.java
            )
            startActivity(i)
        }
        logoutCard.setOnClickListener {
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

    private fun checkUserData(){

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
}

