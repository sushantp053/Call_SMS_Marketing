package com.deecto.callsmsmarketing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.databinding.ActivityPermissionBinding
import com.judemanutd.autostarter.AutoStartPermissionHelper


private const val PERMISSION_REQUEST_CODE = 1
open class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding

    private fun showDialog(titleText: String, messageText: String) {
        with(AlertDialog.Builder(this)) {
            title = titleText
            setMessage(messageText)
            setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun requestPermissionFloatingWindow() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        try {
            startActivityForResult(intent, PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            showDialog(
                "System Overlay Permission",
                "To Alert you when call received."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.cardFloatingWindow.setOnClickListener {
            requestPermissionFloatingWindow();
        }

        binding.cardAutoPlayPermission.setOnClickListener {
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(this)
        }
        binding.cardBatteryPermission.setOnClickListener {
            ignoreBatteryOptimization()
        }

    }

    open fun ignoreBatteryOptimization() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            Toast.makeText(
                this@PermissionActivity,
                "Battery optimization accepted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Don't check for resultCode == Activity.RESULT_OK because the overlay activity
        // is closed with the back button and so the RESULT_CANCELLED is always received.
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (drawOverOtherAppsEnabled()) {
                // The permission has been granted.
                // Resend the last command - we have only one, so no additional logic needed.
//                startFloatingService(INTENT_COMMAND_NOTE)
//                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun Context.drawOverOtherAppsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            Settings.canDrawOverlays(this)
        }
    }


    fun Context.startPermissionActivity() {
        startActivity(
            Intent(this, PermissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}