package kr.ac.gachon.sw.safenoisecanceling.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.gms.location.DetectedActivity

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
    var baseMaxDecibel: Float
        get() = preferences.getFloat("baseMaxDecibel", Float.MIN_VALUE)
        set(value) = preferences.edit().putFloat("baseMaxDecibel", value).apply()

    // Mic Threshold
    var micThresholds: Float
        get() = preferences.getFloat("micThresholds", 0.5f)
        set(value) = preferences.edit().putFloat("micThresholds", value).apply()

    // Classify Period
    var classifyPeriod: Long
        get() = preferences.getLong("classifyPeriod", 500L)
        set(value) = preferences.edit().putLong("classifyPeriod", value).apply()

    // 최종 Activity
    var lastActivity: Int
        get() = preferences.getInt("lastActivity", DetectedActivity.STILL)
        set(value) = preferences.edit().putInt("lastActivity", value).apply()

    // 미디어 일시정지 활성화
    var enableMediaOff: Boolean
        get() = preferences.getBoolean("enableMediaOff", true)
        set(value) = preferences.edit().putBoolean("enableMediaOff", value).apply()

    // 알림 Type
    var notiType: Int
        get() = preferences.getInt("notiType", 0)
        set(value) = preferences.edit().putInt("notiType", value).apply()
}