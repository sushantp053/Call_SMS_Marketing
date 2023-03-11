package com.deecto.callsmsmarketing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deecto.callsmsmarketing.databinding.ActivityDashboardBinding
import com.deecto.callsmsmarketing.databinding.ActivityWhatsAppAutoBinding

class WhatsAppAuto : AppCompatActivity() {
    private lateinit var binding: ActivityWhatsAppAutoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWhatsAppAutoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
}