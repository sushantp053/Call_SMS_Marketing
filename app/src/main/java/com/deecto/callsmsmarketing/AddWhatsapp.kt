package com.deecto.callsmsmarketing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.model.WhatsappMessage
import java.text.SimpleDateFormat
import java.util.*

class AddWhatsapp : AppCompatActivity() {


    private lateinit var message: WhatsappMessage
    private lateinit var old_message: WhatsappMessage
    var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_whatsapp)
        val etTitle: EditText = findViewById(R.id.editTextTextTitle)
        val etWhatsapp: EditText = findViewById(R.id.editTextTextMessage)
        val saveBtn: Button = findViewById(R.id.btnSave)
        try {
            old_message = intent.getSerializableExtra("current_message") as WhatsappMessage
            etTitle.setText(old_message.title)
            etWhatsapp.setText(old_message.message)
            isUpdate = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        saveBtn.setOnClickListener {
            val title = etTitle.text.toString()
            val msg = etWhatsapp.text.toString()
            Log.e("Whatsapp", title)
            if (title.isNotEmpty() || msg.isNotEmpty()) {
                val formater = SimpleDateFormat("yyyymmdd")
                if (isUpdate) {
                    message = WhatsappMessage(
                        old_message.id, title, msg, formater.format(Date()), old_message.status
                    )
                } else {
                    message = WhatsappMessage(null, title, msg, formater.format(Date()), true)
                }
                val intent = Intent()

                intent.putExtra("message", message)
                setResult(Activity.RESULT_OK, intent)
                finish()

            } else {
                Toast.makeText(this, "Please enter some message", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }
    }
}