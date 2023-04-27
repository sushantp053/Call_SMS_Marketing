package com.deecto.callsmsmarketing

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity


class WebLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)


        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Loading")
        mProgressDialog.setMessage("Let's get start marketing")
        mProgressDialog.setCancelable(true)
        mProgressDialog.show()

        val myWebView: WebView = findViewById(R.id.loginWebView)
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Check here if url is equal to your site URL.
                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.loadUrl("javascript:document.getElementsByName('user_id').value = 'sushantp053@gmail.com'")
                myWebView.loadUrl("javascript:document.getElementsByName('user_password').value = '123456'")
                myWebView.loadUrl("javascript:document.forms['login'].submit()")
                mProgressDialog.dismiss()
            }
        })

        myWebView.loadUrl("https://vcard.macmads.com/panel/login/login.php")

    }
}