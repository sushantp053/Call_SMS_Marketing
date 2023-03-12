package com.deecto.callsmsmarketing

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.databinding.ActivityDashboardBinding
import com.deecto.callsmsmarketing.services.ManagePermissions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.judemanutd.autostarter.AutoStartPermissionHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private val permissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions

    private lateinit var binding: ActivityDashboardBinding


    val list = listOf<String>(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_NUMBERS,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
            checkUserData()
//            startPowerSaverIntent(this)
        }

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this, list, permissionsRequestCode)
        managePermissions.checkPermissions()

        if (AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Autostart Permission")
            builder.setMessage("To send automatic massage we need Autostart Permission Kindly Grant the permission")
            builder.setPositiveButton("Give Permission") { dialog, which ->

                AutoStartPermissionHelper.getInstance().getAutoStartPermission(this)
                dialog.dismiss()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            builder.show()
        }
        binding.sendWhatsappDirectCard.setOnClickListener {
            if (binding.etMobile.text.isEmpty() && binding.etMobile.text.length < 10) {
                Toast.makeText(this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show()
            } else {
                val u: String = "https://wa.me/91" + binding.etMobile.text!!.toString()
                val webIntent: Intent = Uri.parse(u).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                startActivity(webIntent)
            }
        }

        binding.autoSMSCard.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.cardAutoWhatsapp.setOnClickListener {
            var intent = Intent(this, WhatsAppAuto::class.java)
            startActivity(intent)
        }
        binding.settingCard.setOnClickListener {
            var intent = Intent(this, PermissionActivity::class.java)
            startActivity(intent)
        }
        binding.cardWebsite.setOnClickListener {
            var intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardAutoDialer.setOnClickListener {
            var intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardContactBlocker.setOnClickListener {
            var intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardTutorial.setOnClickListener {
            var intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardReminder.setOnClickListener {
            var intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
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

    fun getDaysBetweenDates(
        firstDateValue: String,
        secondDateValue: String,
        format: String
    ): Int {
        val sdf = SimpleDateFormat(format, Locale.getDefault())

        val firstDate = sdf.parse(firstDateValue)
        val secondDate = sdf.parse(secondDateValue)

        if (firstDate == null || secondDate == null)
            return 0

        return (((secondDate.time - firstDate.time) / (1000 * 60 * 60 * 24)) + 1).toInt()
    }

    private fun checkUserData() {

        val current = LocalDateTime.now()
        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd")
        val selectedDate = current.format(formatter2)


        Log.e(ContentValues.TAG, "Checking User Data")
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.e(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                    runOnUiThread() {
                        val days: Int =
                            getDaysBetweenDates(
                                selectedDate.toString(),
                                document.data!!.get("end_date").toString(),
                                "yyyyMMdd"
                            )
                        binding.customUserName.text = "Hi, ${document.data!!.get("business_name")}"
                        binding.accountValidity.text = getDateFormatyyyyMMddToyyyyMMdd(
                            document.data!!.get("end_date").toString()
                        ) + " Remaining Days - " + days

                        binding.accountStatus.text = document.data!!.get("active").toString()
                        binding.accountPlan.text = document.data!!.get("plan_name").toString()

                        if (days < 10) {
                            showPlanValidityDialog(
                                days.toString(),
                                document.data!!.get("plan_name").toString()
                            )
                        } else if (days <= 0) {
                            showPlanVExpiredDialog(
                                days.toString(),
                                document.data!!.get("plan_name").toString()
                            )
                        }
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

    fun showPlanValidityDialog(days: String, plan: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Your Plan is Expiring Soon!")
        builder.setMessage("Hello your $plan plan about to expiry in $days days.\nFor uninterrupted service renew your plan")
        builder.setPositiveButton("Renew Plan") { dialog, which ->

            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(
                applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showPlanVExpiredDialog(days: String, plan: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Your Plan is Expired")
        builder.setMessage("Hello your $plan plan is expired from $days days.\nFor uninterrupted service renew your plan")
        builder.setPositiveButton("Renew Plan") { dialog, which ->

            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(
                applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }
        builder.show()
    }
}