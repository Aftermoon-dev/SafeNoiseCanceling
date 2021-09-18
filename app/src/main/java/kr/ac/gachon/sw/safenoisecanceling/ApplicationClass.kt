package kr.ac.gachon.sw.safenoisecanceling

import android.app.Application

class ApplicationClass: Application() {
    companion object {
        const val SC_SERVICE_CHANNEL_ID = "SNC_SoundService"
        const val TRANSITIONS_RECEIVER_ACTION = "SNC_TRANSITIONS"
    }
    override fun onCreate() {
        super.onCreate()
    }
}