package kr.ac.gachon.sw.safenoisecanceling.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.ac.gachon.sw.safenoisecanceling.models.Sound

@Database(entities = [Sound::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundDao(): SoundDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context, AppDatabase::class.java, "snc-database.db")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}