package com.deecto.callsmsmarketing

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
       if( auth.currentUser != null)
       {
           startActivity(Intent(this, MainActivity::class.java))
           finish()
       }
        sendSMSCard.setOnClickListener { 
             if(editTextMobile.text.isEmpty()){
                 Toast.makeText(this, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT).show()
             }else{
                 var intent = Intent(this, VerifyOTP::class.java)
                 intent.putExtra("number", editTextMobile.text!!.toString())
                 startActivity(intent)
                 finish()
             }
        }
    }
}