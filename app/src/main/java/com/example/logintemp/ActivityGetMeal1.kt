package com.example.logintemp
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityGetMeal1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_getmeal1) //


        val btnNext = findViewById<Button>(R.id.btnNext)
        //val skipText = findViewById<TextView>(R.id.skipText)


        btnNext.setOnClickListener {
           val intent = Intent(this, ActivityGetMeal2::class.java)
          startActivity(intent)
          finish()
        }


        //skipText.setOnClickListener {
           // val intent = Intent(this, ActivityMealPlanner::class.java)
           // startActivity(intent)
          //  finish()
        //}
    }
}