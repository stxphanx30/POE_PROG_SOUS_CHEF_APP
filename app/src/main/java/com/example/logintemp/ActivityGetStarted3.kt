package com.example.logintemp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.logintemp.ui.auth.LandingActivity
import com.example.logintemp.util.OnboardingPrefs

class ActivityGetStarted3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getstarted3)

        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            OnboardingPrefs.markSeen(this)
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }
    }
}
