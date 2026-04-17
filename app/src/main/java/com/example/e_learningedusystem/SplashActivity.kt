package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize our Persistent Data Manager
        AppData.init(this)

        val imgLogo = findViewById<ImageView>(R.id.imgLogo)
        val txtAppName = findViewById<TextView>(R.id.txtAppName)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

        // Apply animations
        imgLogo.startAnimation(zoomIn)
        txtAppName.startAnimation(fadeIn)

        // Move to WelcomeActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 3000)
    }
}