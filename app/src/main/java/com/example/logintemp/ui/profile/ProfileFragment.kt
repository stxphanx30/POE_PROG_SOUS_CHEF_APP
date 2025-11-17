package com.example.logintemp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.FragmentProfileBinding
import com.example.logintemp.ui.auth.LandingActivity
import com.example.logintemp.util.DarkModeManager
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private lateinit var repo: UserRepository
    private var currentUserId: Int = -1

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // ignore if permission already granted
            }

            binding.imageProfile.setImageURI(it)
            binding.textProfileInitial.visibility = View.GONE

            lifecycleScope.launch {
                repo.updateProfileImage(currentUserId, it.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        repo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = session.getUsername() ?: "User"
        binding.textUsername.text = username

        // Back button
        binding.imageView3.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Contact Us
        binding.btnContactUs.setOnClickListener {
            sendEmail()
        }

        // Load profile image
        lifecycleScope.launch {
            val uriString = repo.getProfileImage(currentUserId)
            if (!uriString.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(uriString)
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.close()
                    binding.imageProfile.setImageURI(uri)
                    binding.imageProfile.visibility = View.VISIBLE
                    binding.textProfileInitial.visibility = View.GONE
                } catch (e: Exception) {
                    setFallbackImage(username)
                }
            } else {
                setFallbackImage(username)
            }
        }

        // Change profile image
        binding.imageProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            session.clear()
            val intent = Intent(requireContext(), LandingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // Dark mode toggle
        binding.switchDarkMode.isChecked = DarkModeManager.isDarkModeEnabled(requireContext())
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            DarkModeManager.setDarkMode(requireContext(), isChecked)
        }

        // Safe Biometric Toggle
        binding.switchBiometric.isChecked = session.isBiometricEnabled()
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val biometricManager = BiometricManager.from(requireContext())
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        session.setBiometricEnabled(true)
                        Toast.makeText(requireContext(), "Biometric login enabled", Toast.LENGTH_SHORT).show()
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        Toast.makeText(requireContext(),
                            "No fingerprints enrolled. Please add one first.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.switchBiometric.isChecked = false
                        session.setBiometricEnabled(false)

                        // Optional: direct user to fingerprint enrollment
                        val enrollIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                        try {
                            startActivity(enrollIntent)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                "Cannot open fingerprint settings on this device.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        Toast.makeText(requireContext(),
                            "This device does not support fingerprint authentication.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.switchBiometric.isChecked = false
                        session.setBiometricEnabled(false)
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        Toast.makeText(requireContext(),
                            "Biometric hardware is currently unavailable.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.switchBiometric.isChecked = false
                        session.setBiometricEnabled(false)
                    }
                    else -> {
                        Toast.makeText(requireContext(),
                            "Biometric setup not available.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.switchBiometric.isChecked = false
                        session.setBiometricEnabled(false)
                    }
                }
            } else {
                session.setBiometricEnabled(false)
                Toast.makeText(requireContext(), "Biometric login disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFallbackImage(username: String) {
        binding.imageProfile.setImageResource(R.drawable.circle_background)
        binding.textProfileInitial.text = username.firstOrNull()?.uppercase() ?: "?"
        binding.textProfileInitial.visibility = View.VISIBLE
    }

    private fun sendEmail() {
        val recipientEmail = "st10392257@vcconnect.edu.za"
        val subject = "Contact Us Feedback"
        val mailto = "mailto:" + Uri.encode(recipientEmail) +
                "?subject=" + Uri.encode(subject)

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = mailto.toUri()
        }

        if (activity?.packageManager?.resolveActivity(emailIntent, 0) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
