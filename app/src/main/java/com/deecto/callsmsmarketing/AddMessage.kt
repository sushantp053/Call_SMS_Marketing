package com.deecto.callsmsmarketing

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.model.Message
import com.google.android.material.datepicker.SingleDateSelector
import java.text.SimpleDateFormat
import java.util.*

class AddMessage : AppCompatActivity() {

    private lateinit var message: Message
    private lateinit var old_message : Message
    var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_message)
        val etTitle: EditText = findViewById(R.id.editTextTextTitle)
        val etMessage: EditText = findViewById(R.id.editTextTextMessage)
        val saveBtn: Button = findViewById(R.id.btnSave)
        try {
            old_message = intent.getSerializableExtra("current_message", ) as Message
            etTitle.setText(old_message.title)
            etMessage.setText(old_message.message)
            isUpdate = true
        }catch (e : Exception){
            e.printStackTrace()
        }

        saveBtn.setOnClickListener {
            val title = etTitle.text.toString()
            val msg = etMessage.text.toString()
            Log.e("Message", title)
            if(title.isNotEmpty() || msg.isNotEmpty()){
                val formater = SimpleDateFormat("yyyymmdd")
                if(isUpdate){
                   message = Message(
                       old_message.id,title, msg,formater.format(Date()),old_message.status
                   )
                }else{
                    message = Message(null, title,msg, formater.format(Date()), true)
                }
                val intent = Intent()

                intent.putExtra("message", message)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }else{
                Toast.makeText(this,"Please enter some message", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

    }
}