package com.example.logintemp.util

import android.content.Context

object OnboardingPrefs {
    private const val PREF = "onboarding_prefs"
    private const val KEY_SEEN = "has_seen_onboarding"

    fun hasSeen(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SEEN, true).apply()
    }
}
