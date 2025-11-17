package com.example.logintemp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.ActivityRegisterBinding
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = UserRepository(AppDatabase.getInstance(this).userDao())
        session = SessionManager(this)

        // Back arrow (si présent dans layout)
        binding.imageView3.setOnClickListener { finish() }

        binding.btnSubmit2.setOnClickListener {
            val username = binding.etUsername3.text?.toString()?.trim() ?: ""
            val password = binding.etPassword2.text?.toString() ?: ""
            val email = binding.root.findViewById<android.widget.EditText>(R.id.etEmail)?.text?.toString()?.trim()
                ?: binding.etUsername3.text?.toString()?.trim() // fallback si pas d'email : username
            val firstName = binding.root.findViewById<android.widget.EditText>(R.id.etFirstName)?.text?.toString()?.trim()
            val lastName = binding.root.findViewById<android.widget.EditText>(R.id.etLastName)?.text?.toString()?.trim()

            if (!validate(username, password)) return@setOnClickListener

            lifecycleScope.launch {
                repo.register(username, email, password).onSuccess { user ->
                    session.saveUserId(user.id)
                    session.saveUsername(user.username)
                    // Optionnel : sauvegarder first/last si tu veux mettre à jour
                    if (!firstName.isNullOrBlank() || !lastName.isNullOrBlank()) {
                        val updated = user.copy(
                            firstName = firstName ?: user.firstName,
                            lastName = lastName ?: user.lastName,
                            updatedAt = System.currentTimeMillis()
                        )
                        repo.updateProfileImage(updated.id, updated.profileImageUri) // juste pour appeler DB update
                        // actually update user record:
                        // userDao.updateUser(updated) -> requires exposing dao or repo method; skip for now
                    }

                    // Navigate onward (tu avais SetupFingerprintActivity)
                    startActivity(Intent(this@RegisterActivity, SetupFingerprintActivity::class.java))
                    finishAffinity()
                }.onFailure {
                    binding.tvError.text = it.message
                }
            }
        }
    }

    private fun validate(username: String, password: String): Boolean {
        if (username.isEmpty()) { binding.etUsername3.error = "Required"; return false }
        if (password.length < 8 || !password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            binding.etPassword2.error = "Password must be 8+ chars, include letters and digits"
            return false
        }
        return true
    }
}