package com.ldnhat.controldevice.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ldnhat.controldevice.R
import com.ldnhat.controldevice.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntro()
    }

    private fun handleIntro(){
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}