package kr.ac.gachon.sw.safenoisecanceling.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    // 서비스 활성화 여부
    var isSNCEnable: Boolean
        get() = preferences.getBoolean("isSNCEnable", false)
        set(value) = preferences.edit().putBoolean("isSNCEnable", value).apply()

    // 소리 인식 Threshold Value
    var classifyThresholds: Float
        get() = preferences.getFloat("classifyThresholds", 0.3f)
        set(value) = preferences.edit().putFloat("classifyThresholds", value).apply()

    // Base Max Amplitude
    var baseMaxAmplitude: Float
        get() = preferences.getFloat("baseMaxAmplitude", Float.MIN_VALUE)
        set(value) = preferences.edit().putFloat("baseMaxAmplitude", value).apply()

    // Mic Threshold
    var micThresholds: Float
        get() = preferences.getFloat("micThresholds", 0.5f)
        set(value) = preferences.edit().putFloat("micThresholds", value).apply()

    // Classify Period
    var classifyPeriod: Long
        get() = preferences.getLong("classifyPeriod", 500L)
        set(value) = preferences.edit().putLong("classifyPeriod", value).apply()
}