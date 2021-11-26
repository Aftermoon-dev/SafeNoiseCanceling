package kr.ac.gachon.sw.safenoisecanceling.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kr.ac.gachon.sw.safenoisecanceling.models.Sound

@Dao
interface SoundDao {
    @Query("SELECT * FROM sound")
    fun getAll(): List<Sound>

    @Query("SELECT * from sound where :startTime < time and time < :endTime")
    fun getSpecificTimeData(startTime: Long, endTime: Long): List<Sound>

    @Insert
    fun insert(sound: Sound)

    @Delete
    fun delete(sound: Sound)
}