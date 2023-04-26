package com.deecto.callsmsmarketing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deecto.callsmsmarketing.databinding.ActivityBlockedContactsBinding
import com.deecto.callsmsmarketing.databinding.ActivityWebSiteBinding

class WebSite : AppCompatActivity() {
    private lateinit var binding: ActivityWebSiteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebSiteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.cardWebsiteVisit.setOnClickListener {  }
        binding.cardWebsiteEdit.setOnClickListener {  }
    }
}