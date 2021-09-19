package kr.ac.gachon.sw.safenoisecanceling

import android.app.Application
import kr.ac.gachon.sw.safenoisecanceling.utils.Preferences

class ApplicationClass: Application() {
    companion object {
        const val SC_SERVICE_CHANNEL_ID = "SNC_SoundService"
        const val TRANSITIONS_RECEIVER_ACTION = "SNC_TRANSITIONS"

        lateinit var SharedPreferences: Preferences
    }
    override fun onCreate() {
        SharedPreferences = Preferences(applicationContext)
        super.onCreate()
    }
}