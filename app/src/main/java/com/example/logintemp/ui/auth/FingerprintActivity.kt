package com.example.logintemp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.logintemp.MainActivity
import com.example.logintemp.databinding.ActivityFingerprintBinding
import com.example.logintemp.util.SessionManager

class FingerprintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFingerprintBinding
    private lateinit var session: SessionManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFingerprintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // Check if device supports biometrics and fingerprints exist
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                session.setBiometricEnabled(false)
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                session.setBiometricEnabled(false)
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
                return
            }
            else -> { /* OK */ }
        }

        setupBiometricPrompt()

        // Auto show biometric prompt on open
        biometricPrompt.authenticate(promptInfo)

        // Retry button
        binding.btnUnlock.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@FingerprintActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Don't finish the activity — let user tap "Unlock" again
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Optional: show a toast if needed
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock App")
            .setSubtitle("Use your fingerprint to continue")
            .setNegativeButtonText("Cancel")
            .build()
    }
}
