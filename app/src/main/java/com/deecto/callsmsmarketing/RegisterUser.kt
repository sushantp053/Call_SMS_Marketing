package com.deecto.callsmsmarketing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.databinding.ActivityRegisterUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RegisterUser : AppCompatActivity() {
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+"
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var dialog: AlertDialog
    private lateinit var binding: ActivityRegisterUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        binding.saveBtn.setOnClickListener {
            val businessName = binding.etBusinessName.text.toString()
            val address = binding.etAddress.text.toString()
            val email = binding.etEmail.text.toString()
            if (businessName.isNotEmpty()) {
                if (address.isNotEmpty()) {
                    if (email.isNotEmpty()) {
                        if (email.matches(emailPattern.toRegex())) {

                            val current = LocalDateTime.now()
                            val formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd")
                            val selectedDate = current.format(formatter2)
                            val s = (selectedDate.toInt() + 3).toString()

                            val builder = AlertDialog.Builder(this)
                            builder.setMessage("Please wait.....")
                            builder.setTitle("Loading")
                            builder.setCancelable(false)

                            dialog = builder.create()
                            dialog.show()
                            val user = db.collection("users")
                            val userData = hashMapOf(
                                "business_name" to businessName,
                                "email" to email,
                                "address" to address,
                                "mobile" to auth.currentUser!!.phoneNumber,
                                "active" to true,
                                "plan" to 7,
                                "plan_name" to "trial",
                                "start_date" to selectedDate,
                                "end_date" to s,
                                "plan_id" to 1,
                            )
                            user.document(auth.uid.toString()).set(userData).addOnSuccessListener {

                                dialog.dismiss()
                                startActivity(Intent(this@RegisterUser, Dashboard::class.java))
                                finish()
                            }.addOnFailureListener {
                                dialog.dismiss()
                                Toast.makeText(
                                    this@RegisterUser,
                                    "Try again. Data saving error $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext, "Invalid email address",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "Please Enter Your Email Address", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Please Enter Your Address", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Enter Your Business Name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}