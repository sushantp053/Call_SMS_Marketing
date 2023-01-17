package com.deecto.callsmsmarketing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        auth = Firebase.auth

        Handler().postDelayed({
            val currentUser = auth.currentUser
            if(currentUser != null){
                val i = Intent(
                    this@SplashScreen,
                    MainActivity::class.java
                )
                startActivity(i)
                finish()
            }else{
                val i = Intent(
                    this@SplashScreen,
                    Login::class.java
                )
                startActivity(i)
                finish()
            }

        }, 2000)
    }
}