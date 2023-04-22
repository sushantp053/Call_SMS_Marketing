package com.deecto.callsmsmarketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.database.DayWhatsappCounterDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.WhatsappDao
import com.deecto.callsmsmarketing.databinding.ActivityWhatsAppAutoBinding
import com.deecto.callsmsmarketing.model.DayWhatsappCounter
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
            try {
                var msg = messageDio.getDefaultWhatsappMessage(true)
                binding.textViewCurrentMsg.text = msg.message
            } catch (e: Exception) {
                Log.e("set text error", e.toString())
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val formattedDate = current.format(formatter)
            var dayWhatsappCounterDao: DayWhatsappCounterDao = database.getDayWhatsappCounterDao()
            try {
                val dayDetails = dayWhatsappCounterDao.getDayCount(formattedDate)
                val totalCount = dayWhatsappCounterDao.getTotalCount()

                binding.totalSmsCount.text = totalCount.toString()
                binding.dailySmsCount.text = dayDetails.counter.toString()

            } catch (e: Exception) {
                Log.e("Set Count Error", e.toString())
                binding.dailySmsCount.text = "0"
                var dayWhatsappCounter: DayWhatsappCounter =
                    DayWhatsappCounter(null, formattedDate.toString(), 0)
                dayWhatsappCounterDao.insert(
                    dayWhatsappCounter
                )
            }
        }

        binding.changeMessageCard.setOnClickListener {
            var intent = Intent(this, WhatsappActivity::class.java)
            startActivity(intent)
        }
        val sharedPref = this.getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        val dailySms = sharedPref.getBoolean("whats_daily", true)
        val incoming = sharedPref.getBoolean("whats_incoming", true)
        val outgoing = sharedPref.getBoolean("whats_outgoing", true)
        val attachment = sharedPref.getBoolean("whats_attachment", true)

        when(sharedPref.getInt("whats",R.id.btnOff)){
            R.id.btnOff -> binding.toggleButtonGroup.check(R.id.btnOff)
            R.id.btnAsk -> binding.toggleButtonGroup.check(R.id.btnAsk)
            R.id.btnAuto -> binding.toggleButtonGroup.check(R.id.btnAuto)
        }

        binding.dailyOne.isChecked = dailySms
        binding.incomingCallSwitch.isChecked = incoming
        binding.outGoingCallSwitch.isChecked = outgoing
//        binding.limitText.text = "$limit SMS"


        binding.toggleButtonGroup.addOnButtonCheckedListener { toggleButtonGroup, checkedId, isChecked ->

            if (isChecked) {
                when (checkedId) {
                    R.id.btnOff -> sharedPref.edit().putInt("whats", R.id.btnOff).apply()
                    R.id.btnAsk -> sharedPref.edit().putInt("whats", R.id.btnAsk).apply()
                    R.id.btnAuto -> sharedPref.edit().putInt("whats", R.id.btnAuto).apply()
                }
            }
        }
    }

    private fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()

    }
}