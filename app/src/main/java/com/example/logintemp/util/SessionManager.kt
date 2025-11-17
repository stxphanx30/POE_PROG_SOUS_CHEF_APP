package com.example.logintemp.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private val KEY_FIREBASE_UID = "firebase_uid"

    // Save user ID to preferences
    fun saveUserId(id: Int) = prefs.edit().putInt("user_id", id).apply()
    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun saveUsername(username: String) = prefs.edit().putString("username", username).apply()
    fun getUsername(): String? = prefs.getString("username", null)

    // Firebase uid
    fun saveFirebaseUid(uid: String?) = prefs.edit().putString(KEY_FIREBASE_UID, uid).apply()
    fun getFirebaseUid(): String? = prefs.getString(KEY_FIREBASE_UID, null)

    fun clear() = prefs.edit().clear().apply()
    fun isLoggedIn() = getUserId() != -1

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
}