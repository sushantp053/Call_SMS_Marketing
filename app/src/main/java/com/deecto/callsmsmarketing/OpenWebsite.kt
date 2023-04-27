package com.deecto.callsmsmarketing

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class OpenWebsite : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_website)


        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Loading")
        mProgressDialog.setMessage("Let's get start marketing")
        mProgressDialog.setCancelable(true)
        mProgressDialog.show()

        val webUrl:String = intent.getStringExtra("web_url").toString()

        val myWebView: WebView = findViewById(R.id.loginWebView)
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Check here if url is equal to your site URL.
                mProgressDialog.dismiss()
            }
        })

        myWebView.loadUrl(webUrl)
    }
}