package com.example.logintemp.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.logintemp.R
import com.example.logintemp.utils.LocaleHelper
import com.example.logintemp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var textLanguage: TextView
    private lateinit var langSwitch: ImageView

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val PREFS = "app_prefs"
    private val KEY_NOTIF = "pref_notifications_enabled"
    private lateinit var prefs: SharedPreferences

    // launcher pour permission POST_NOTIFICATIONS
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // appelé après la demande
            if (granted) {
                // enabled by user
                saveNotificationsEnabled(true)
                binding.switchBiometric.isChecked = true
            } else {
                // refused -> show disabled and offer settings link
                saveNotificationsEnabled(false)
                binding.switchBiometric.isChecked = false
                Toast.makeText(requireContext(), "Notifications denied. You can enable them in App settings.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textLanguage = view.findViewById(R.id.password)
        langSwitch = view.findViewById(R.id.edit3)

        // Load saved language
        var currentLang = LocaleHelper.getLanguage(requireContext())
        updateLanguageText(currentLang)

        langSwitch.setOnClickListener {
            // Toggle language (tu avais en <-> pt dans ton exemple)
            currentLang = if (currentLang == "en") "pt" else "en"

            // Apply new language
            LocaleHelper.setLocale(requireContext(), currentLang)

            // Update TextView
            updateLanguageText(currentLang)

            // Recreate activity to refresh UI
            requireActivity().recreate()
        }

        // initial state for notifications switch
        val enabled = prefs.getBoolean(KEY_NOTIF, false)
        binding.switchBiometric.isChecked = enabled

        // click listener for the switch
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle(isChecked)
        }

        // Contact us -> open email
        binding.btnContactUs.setOnClickListener {
            openContactEmail()
        }

        // Optional: back button if present in your layout (id imageView used in NotificationFragment)
        // binding.imageView?.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun updateLanguageText(lang: String) {
        textLanguage.text = if (lang == "en") "Language: English" else "Language: Portuguese"
    }

    private fun handleNotificationToggle(shouldEnable: Boolean) {
        if (shouldEnable) {
            // If Android 13+, check permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (granted) {
                    saveNotificationsEnabled(true)
                    binding.switchBiometric.isChecked = true
                } else {
                    // request permission
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // older Android: permission not required
                saveNotificationsEnabled(true)
                binding.switchBiometric.isChecked = true
            }
        } else {
            // user turned off -> save false
            saveNotificationsEnabled(false)
            binding.switchBiometric.isChecked = false
        }
    }

    private fun saveNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF, value).apply()
    }

    private fun openContactEmail() {
        val supportEmail = "support@souschef.example" // change to real email if you want
        val subject = "Support request - Sous Chef"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Bonjour,\n\nJ'ai besoin d'aide concernant...")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // no email app
            Toast.makeText(requireContext(), "No email app found on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}