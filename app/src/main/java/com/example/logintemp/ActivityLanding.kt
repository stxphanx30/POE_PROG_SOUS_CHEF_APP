package com.example.logintemp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityLanding : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing) //

        // Get references to views
        //val btnSignUp = findViewById<Button>(R.id.btnSignUp)
      //  val signInText = findViewById<TextView>(R.id.signIn)

        //  Sign Up button click
       // btnSignUp.setOnClickListener {
           // val intent = Intent(this, SignUpActivity::class.java)
          ///  startActivity(intent)
        //}

        //  Sign In text click
       // signInText.setOnClickListener {
            //val intent = Intent(this, SignInActivity::class.java)
          //  startActivity(intent)
       // }
    }
}