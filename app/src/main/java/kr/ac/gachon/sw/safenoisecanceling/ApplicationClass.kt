package kr.ac.gachon.sw.safenoisecanceling

import android.app.Application

import kr.ac.gachon.sw.safenoisecanceling.db.AppDatabase
import kr.ac.gachon.sw.safenoisecanceling.utils.Preferences
import kr.ac.gachon.sw.safenoisecanceling.utils.RxEventBus

class ApplicationClass: Application() {
    companion object {
        const val SC_SERVICE_CHANNEL_ID = "SNC_SoundService"
        const val SC_WARNING_CHANNEL_ID = "SNC_WARNING"
        const val TRANSITIONS_RECEIVER_ACTION = "SNC_TRANSITIONS"
        const val YAMNET_MODEL_FILE = "yamnet.tflite"

        lateinit var roomDatabase: AppDatabase
        lateinit var SharedPreferences: Preferences
        lateinit var rxEventBus: RxEventBus
    }
    override fun onCreate() {
        roomDatabase = AppDatabase.getInstance(applicationContext)!!
        SharedPreferences = Preferences(applicationContext)
        rxEventBus = RxEventBus.getInstance()!!
        super.onCreate()
    }
}