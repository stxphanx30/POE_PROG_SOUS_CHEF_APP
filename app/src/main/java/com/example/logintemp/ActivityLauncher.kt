package com.example.logintemp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.logintemp.ui.auth.LandingActivity
import com.example.logintemp.util.OnboardingPrefs
import com.example.logintemp.util.SessionManager

class ActivityLauncher : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        session = SessionManager(this)

        // optional splash delay (4s)
        Handler(Looper.getMainLooper()).postDelayed({
            routeFromLauncher()
        }, 4000)
    }

    private fun routeFromLauncher() {
        // 1) First run? Show onboarding flow
        if (!OnboardingPrefs.hasSeen(this)) {
            startActivity(Intent(this, ActivityGetStarted1::class.java))
            finish()
            return
        }

        // 2) Already onboarded → check auth
        if (session.isLoggedIn()) {
            if (session.isBiometricEnabled()) {
                showBiometricPrompt {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // On cancel/error, just fall back to Landing or finish
                    startActivity(Intent(this@ActivityLauncher, LandingActivity::class.java))
                    finish()
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Authenticate to continue")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(info)
    }
}
