package com.deecto.callsmsmarketing

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deecto.callsmsmarketing.databinding.ActivityDashboardBinding
import com.deecto.callsmsmarketing.services.ManagePermissions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
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
    lateinit var web_url: String
    lateinit var web_user: String
    lateinit var web_pass: String


    val list = listOf<String>(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sharedPref = getSharedPreferences("Call", Context.MODE_PRIVATE) ?: return
        sharedPref.edit().putBoolean("popup", true)
            .apply()
        val whats = sharedPref.getInt("whats", R.id.btnOff)
//        if (whats == R.id.btnAuto) {
//            if (!isAccessibilityOn(this@Dashboard, WhatsappAccessibilityService::class.java)) {
//                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//                this@Dashboard.startActivity(intent)
//            }
//        }
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
            checkUserData()
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d("TAG", msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this, list, permissionsRequestCode)
        managePermissions.checkPermissions()
        askNotificationPermission()

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
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("web_url", web_url)
            startActivity(intent)
        }
        binding.cardAutoWhatsapp.setOnClickListener {
            val intent = Intent(this, WhatsAppAuto::class.java)
            intent.putExtra("web_url", web_url)
            startActivity(intent)
        }
        binding.settingCard.setOnClickListener {
            val intent = Intent(this, PermissionActivity::class.java)
            startActivity(intent)
        }
        binding.cardWebsite.setOnClickListener {
            if (web_url.isNullOrEmpty()) {
                showWebNotCreated()
            } else {
                val intent = Intent(this, WebSite::class.java)
                intent.putExtra("web_url", web_url)
                intent.putExtra("web_user", web_user)
                intent.putExtra("web_pass", web_pass)
                startActivity(intent)
            }
        }
        binding.cardSocialMediaImages.setOnClickListener {
            val intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardAutoDialer.setOnClickListener {
            val intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardContactBlocker.setOnClickListener {
            val intent = Intent(this, BlockedContactsActivity::class.java)
            startActivity(intent)
        }
        binding.cardTutorial.setOnClickListener {
            val intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.cardReminder.setOnClickListener {
            val intent = Intent(this, ComingSoon::class.java)
            startActivity(intent)
        }
        binding.accountCard.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

    }

    private fun getDateFormatyyyyMMddToyyyyMMdd(string: String?): String? {
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

    private fun getDaysBetweenDates(
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
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Loading Data")
        mProgressDialog.setMessage("Let's get started marketing")
        mProgressDialog.show()

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

                        binding.accountStatus.text = document.data!!["active"].toString()
                        binding.accountPlan.text = document.data!!["plan_name"].toString()

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
                        web_url = document.data!!["web_url"].toString()
                        web_user = document.data!!["web_user"].toString()
                        web_pass = document.data!!["web_pass"].toString()
                    }
                    mProgressDialog.dismiss()
                } else {
                    mProgressDialog.dismiss()
                    Log.e(ContentValues.TAG, "No such document")
//                                    RegisterUser
                    startActivity(Intent(this, RegisterUser::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
                mProgressDialog.dismiss()
//                                    RegisterUser
                startActivity(Intent(this, RegisterUser::class.java))
                finish()
            }
    }

    private fun showPlanValidityDialog(days: String, plan: String) {
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

    private fun showWebNotCreated() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Website")
        builder.setMessage("Your website is not available contact customer care to create.")
        builder.setPositiveButton("Ok") { dialog, which ->
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun isAccessibilityOn(
        context: Context,
        clazz: Class<out AccessibilityService?>
    ): Boolean {
        var accessibilityEnabled = 0
        val service: String = context.packageName + "/" + clazz.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (ignored: Settings.SettingNotFoundException) {
        }
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue: String = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            colonSplitter.setString(settingValue)
            while (colonSplitter.hasNext()) {
                val accessibilityService = colonSplitter.next()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}