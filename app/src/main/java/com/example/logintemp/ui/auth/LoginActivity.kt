package com.example.logintemp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.logintemp.MainActivity
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.ActivityLoginBinding
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = UserRepository(AppDatabase.getInstance(this).userDao())
        session = SessionManager(this)

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = "Required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Required"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                repo.loginLocal(username, password).onSuccess { user ->

                    // Save session
                    session.saveUserId(user.id)
                    session.saveUsername(user.username)

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finishAffinity()

                }.onFailure { error ->
                    binding.tvError.text = error.message ?: "Login failed"
                }
            }
        }

        binding.imageView.setOnClickListener {
            finish()
        }
    }
}