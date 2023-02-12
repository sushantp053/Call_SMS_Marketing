package com.deecto.callsmsmarketing

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_verify_otp.*
import java.util.concurrent.TimeUnit

class VerifyOTP : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var dialog: AlertDialog
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)
        auth = Firebase.auth

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please wait.....")
        builder.setTitle("Loading")
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()

        val phoneNumber = "+91" + intent.getStringExtra("number")

        otpMobile.text = phoneNumber

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(this@VerifyOTP, "Please Try Again", Toast.LENGTH_SHORT).show()

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    dialog.dismiss()
                    verificationId = p0
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        verifyOTPCard.setOnClickListener {
            if (editTextOtp.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter OTP....", Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                val credential =
                    PhoneAuthProvider.getCredential(verificationId, editTextOtp.text!!.toString())
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val docRef = db.collection("users").document(auth.uid.toString())
                        docRef.get()
                            .addOnSuccessListener { document ->
                                if (document.data != null) {
                                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Log.d(TAG, "No such document")
//                                    RegisterUser
                                    startActivity(Intent(this, RegisterUser::class.java))
                                    finish()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "get failed with ", exception)
                            }
                        dialog.dismiss()

                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@VerifyOTP, "Error ${it.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        changeMobile.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}