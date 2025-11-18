package com.example.logintemp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.logintemp.R
import com.example.logintemp.utils.LocaleHelper

class SettingsFragment : Fragment() {

    private lateinit var textLanguage: TextView
    private lateinit var langSwitch: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textLanguage = view.findViewById(R.id.password)
        langSwitch = view.findViewById(R.id.edit3)

        // Load saved language
        var currentLang = LocaleHelper.getLanguage(requireContext())
        updateLanguageText(currentLang)

        langSwitch.setOnClickListener {
            // Toggle language
            currentLang = if (currentLang == "en") "pt" else "en"

            // Apply new language
            LocaleHelper.setLocale(requireContext(), currentLang)

            // Update TextView
            updateLanguageText(currentLang)

            // Recreate activity to refresh UI
            requireActivity().recreate()
        }
    }

    private fun updateLanguageText(lang: String) {
        textLanguage.text = if (lang == "en") "Language: English" else "Language: Portuguese"
    }
}
