package com.example.logintemp
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.logintemp.ui.auth.LandingActivity

class ActivityGetStarted2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getstarted2)


        val btnNext = findViewById<Button>(R.id.btnNext)
        val skipText = findViewById<TextView>(R.id.skipText)


        btnNext.setOnClickListener {
            val intent = Intent(this, ActivityGetStarted3::class.java)
            startActivity(intent)
            finish()
        }


        skipText.setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}