package com.deecto.callsmsmarketing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        dailyOne.setOnCheckedChangeListener { buttonView, isChecked ->
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

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                963
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                111
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                977
            )
        }
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
}

