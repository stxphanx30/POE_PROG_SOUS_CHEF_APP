package com.example.logintemp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.example.logintemp.MainActivity
import com.example.logintemp.R
import com.example.logintemp.util.SessionManager

class SetupFingerprintActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_fingerprint)

        session = SessionManager(this)

        val skip = findViewById<TextView>(R.id.skip)
        val proceed = findViewById<TextView>(R.id.textView23) // “Yes, proceed”

        skip.setOnClickListener {
            session.setBiometricEnabled(false)
            navigateToMain()
        }

        proceed.setOnClickListener {
            setupBiometric()
        }
    }

    private fun setupBiometric() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                session.setBiometricEnabled(true)
                Toast.makeText(this, "Biometric login enabled successfully", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this,
                    "No fingerprints enrolled. Please add one first.",
                    Toast.LENGTH_LONG
                ).show()
                session.setBiometricEnabled(false)
                try {
                    startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                } catch (e: Exception) {
                    Toast.makeText(this, "Cannot open fingerprint settings.", Toast.LENGTH_SHORT).show()
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this,
                    "Your device does not support fingerprint authentication.",
                    Toast.LENGTH_LONG
                ).show()
                session.setBiometricEnabled(false)
                navigateToMain()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this,
                    "Biometric hardware is currently unavailable. Try again later.",
                    Toast.LENGTH_LONG
                ).show()
                session.setBiometricEnabled(false)
                navigateToMain()
            }

            else -> {
                Toast.makeText(this,
                    "Biometric setup not available.",
                    Toast.LENGTH_LONG
                ).show()
                session.setBiometricEnabled(false)
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
