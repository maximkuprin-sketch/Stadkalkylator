package com.example.flyttstadning.data.repository

import android.content.Context
import android.content.SharedPreferences

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("flytt_prefs", Context.MODE_PRIVATE)

    fun saveLastImportTime(timestamp: Long) {
        prefs.edit().putLong("last_import_time", timestamp).apply()
    }

    fun getLastImportTime(): Long {
        return prefs.getLong("last_import_time", 0L)
    }
}
