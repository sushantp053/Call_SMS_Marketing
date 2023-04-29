package com.deecto.callsmsmarketing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deecto.callsmsmarketing.databinding.ActivityWebSiteBinding

class WebSite : AppCompatActivity() {
    private lateinit var binding: ActivityWebSiteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebSiteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val webUrl:String = intent.getStringExtra("web_url").toString()
        val webUser:String = intent.getStringExtra("web_user").toString()
        val webPass:String = intent.getStringExtra("web_pass").toString()
        val webLogin:String = intent.getStringExtra("web_login").toString()

        binding.webUrlTV.text = webUrl
        binding.userNameTV.text = webUser
        binding.userPassTV.text = webPass

        binding.cardWebsiteVisit.setOnClickListener {
            val intent = Intent(this, OpenWebsite::class.java)
            intent.putExtra("web_url", webUrl)
            startActivity(intent)
        }
        binding.cardWebsiteEdit.setOnClickListener {
            val intent = Intent(this, WebLogin::class.java)
            intent.putExtra("web_url", webLogin)
            intent.putExtra("web_user", webUser)
            intent.putExtra("web_pass", webPass)
            startActivity(intent)
        }
        binding.shareWebSite.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Website")
                var shareMessage = "\nVisit my website\n\n$webUrl"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
    }

}