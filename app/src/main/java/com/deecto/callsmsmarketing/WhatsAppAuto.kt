package com.deecto.callsmsmarketing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.deecto.callSMSmarketing.database.DayWhatsappDao
import com.deecto.callsmsmarketing.database.DaySMSCounterDao
import com.deecto.callsmsmarketing.database.MessageDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.WhatsappDao
import com.deecto.callsmsmarketing.databinding.ActivityDashboardBinding
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.databinding.ActivityWhatsAppAutoBinding
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.model.DayWhatsappCounter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WhatsAppAuto : AppCompatActivity() {
    private lateinit var binding: ActivityWhatsAppAutoBinding

    private lateinit var database: MessageDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWhatsAppAutoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = MessageDatabase.getDatabase(this)


        CoroutineScope(Dispatchers.Default).launch {
            var messageDio: WhatsappDao = database.getWhatsappDao()
            var msg = messageDio.getDefaultMessage(true)
            try {
                binding.textViewCurrentMsg.text = msg.message
            } catch (e: Exception) {
                Log.e("set text error", e.toString())
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val formattedDate = current.format(formatter)
            var dayWhatsappCounterDao: DayWhatsappDao = database.getDayWhatsappCounterDao()
            try {
                val dayDetails = dayWhatsappCounterDao.getDayCount(formattedDate)
                val totalCount = dayWhatsappCounterDao.getTotalCount()

                binding.totalSmsCount.text = totalCount.toString()
                binding.dailySmsCount.text = dayDetails.counter.toString()

            } catch (e: Exception) {
                Log.e("Set Count Error", e.toString())
                binding.dailySmsCount.text = "0"
                var dayWhatsappCounter: DayWhatsappCounter = DayWhatsappCounter(null, formattedDate.toString(), 0)
                dayWhatsappCounterDao.insert(
                    dayWhatsappCounter
                )
            }
        }

    }
}