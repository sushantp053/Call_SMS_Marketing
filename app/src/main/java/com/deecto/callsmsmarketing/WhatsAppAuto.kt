package com.deecto.callsmsmarketing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.deecto.callsmsmarketing.database.DayWhatsappCounterDao
import com.deecto.callsmsmarketing.database.MessageDatabase
import com.deecto.callsmsmarketing.database.WhatsappDao
import com.deecto.callsmsmarketing.databinding.ActivityWhatsAppAutoBinding
import com.deecto.callsmsmarketing.model.DayWhatsappCounter
import com.google.firebase.auth.FirebaseAuth
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
        val webUrl: String = intent.getStringExtra("web_url").toString()
        if (!webUrl.isNullOrEmpty()) {
            binding.attachLinkCard.isVisible = true
            binding.webUrlTV.text = webUrl

        } else {
            binding.attachLinkCard.isVisible = false
        }

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
        val attachLink = sharedPref.getBoolean("whats_link", true)
        val attachment = sharedPref.getBoolean("whats_attachment", true)

        when(sharedPref.getInt("whats",R.id.btnOff)){
            R.id.btnOff -> binding.toggleButtonGroup.check(R.id.btnOff)
            R.id.btnAsk -> binding.toggleButtonGroup.check(R.id.btnAsk)
            R.id.btnAuto -> binding.toggleButtonGroup.check(R.id.btnAuto)
        }
        mediaFun(attachment)
        binding.dailyOneWhatsapp.isChecked = dailySms
        binding.incomingCallSwitch.isChecked = incoming
        binding.attachMediaFile.isChecked = attachment
        binding.attachLinkSwitch.isChecked = attachLink
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
        binding.attachMediaFile.setOnCheckedChangeListener { buttonView, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("whats_attachment", isChecked)
                apply()
                mediaFun(isChecked)
            }
        }
        binding.mediaFileImage.setOnClickListener{
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }

        binding.attachLinkSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("whats_link", isChecked)
                apply()
            }
            with(sharedPref.edit()){
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
    }

    private fun mediaFun(status : Boolean){
        if(status) {
            binding.mediaFileImage.isVisible = true
            binding.horizontalLine.isVisible = true
        }else{
            binding.mediaFileImage.isVisible = false
            binding.horizontalLine.isVisible = false
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data?.data // The URI with the location of the file
            binding.mediaFileImage.setImageURI(selectedFile)
        }
    }
    private fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()

    }
}