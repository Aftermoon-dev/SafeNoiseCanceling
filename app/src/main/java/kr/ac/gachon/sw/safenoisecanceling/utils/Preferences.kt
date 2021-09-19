package kr.ac.gachon.sw.safenoisecanceling.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    var isSNCEnable: Boolean
        get() = preferences.getBoolean("isSNCEnable", false)
        set(value) = preferences.edit().putBoolean("isSNCEnable", value).apply()
}