package kr.ac.gachon.sw.safenoisecanceling

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.ac.gachon.sw.safenoisecanceling.db.AppDatabase
import kr.ac.gachon.sw.safenoisecanceling.utils.Preferences

class ApplicationClass: Application() {
    companion object {
        const val SC_SERVICE_CHANNEL_ID = "SNC_SoundService"
        const val TRANSITIONS_RECEIVER_ACTION = "SNC_TRANSITIONS"
        const val YAMNET_MODEL_FILE = "yamnet.tflite"

        lateinit var roomDatabase: AppDatabase
        lateinit var SharedPreferences: Preferences
    }
    override fun onCreate() {
        roomDatabase = AppDatabase.getInstance(applicationContext)!!
        SharedPreferences = Preferences(applicationContext)
        super.onCreate()
    }
}