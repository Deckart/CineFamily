package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cinefamille_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MEMBER_ID = "logged_in_member_id"
    }

    fun saveLoggedInMemberId(memberId: String) {
        prefs.edit().putString(KEY_MEMBER_ID, memberId).apply()
    }

    fun getLoggedInMemberId(): String? {
        return prefs.getString(KEY_MEMBER_ID, null)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_MEMBER_ID).apply()
    }
}
