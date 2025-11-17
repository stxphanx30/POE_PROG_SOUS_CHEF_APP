package com.example.logintemp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.logintemp.ui.auth.LandingActivity
import com.example.logintemp.util.OnboardingPrefs

class ActivityGetStarted1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getstarted1)

        val btnNext = findViewById<Button>(R.id.btnNext)
        val skipText = findViewById<TextView>(R.id.skipText)

        btnNext.setOnClickListener {
            startActivity(Intent(this, ActivityGetStarted2::class.java))
            finish()
        }

        skipText.setOnClickListener {
            OnboardingPrefs.markSeen(this)
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }
    }
}
