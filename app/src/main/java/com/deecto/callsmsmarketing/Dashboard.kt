package com.deecto.callsmsmarketing

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_login.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
            checkUserData()
        }
        sendWhatsappDirectCard.setOnClickListener {
            if(etMobile.text.isEmpty() && etMobile.text.length <10){
                Toast.makeText(this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show()
            }else{
              val u : String =  "https://wa.me/91"+etMobile.text!!.toString()
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                startActivity(webIntent)
            }
        }
    }
    fun getDateFormatyyyyMMddToyyyyMMdd(string: String?): String? {
        val inputSDF = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val outputSDF = SimpleDateFormat("yyyy-MMM-dd", Locale.getDefault())
        var date: Date? = null
        date = try {
            //here you get Date object from string
            inputSDF.parse(string)
        } catch (e: ParseException) {
            return string
        }
        return outputSDF.format(date)
    }
    private fun checkUserData() {

        Log.e(ContentValues.TAG, "Checking User Data")
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.e(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                    runOnUiThread() {
                        customUserName.text = "Hi, ${document.data!!.get("business_name")}"
                        accountValidity.text = getDateFormatyyyyMMddToyyyyMMdd(document.data!!.get("end_date").toString())
                        accountStatus.text = document.data!!.get("active").toString()
                        accountPlan.text = document.data!!.get("plan_name").toString()
                    }

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