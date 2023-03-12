package com.deecto.callsmsmarketing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.deecto.callsmsmarketing.databinding.ActivityMainBinding
import com.deecto.callsmsmarketing.databinding.ActivityPermissionBinding
import com.judemanutd.autostarter.AutoStartPermissionHelper

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding

    private val PERMISSION_REQUEST_CODE = 1
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