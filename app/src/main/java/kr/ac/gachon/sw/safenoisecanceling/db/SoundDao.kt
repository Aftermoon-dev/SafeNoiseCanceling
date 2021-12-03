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

    @Query("SELECT * from sound ORDER BY id DESC LIMIT :startIdx, :itemCount")
    fun getSoundDatabyIndex(startIdx: Int, itemCount: Int): List<Sound>

    @Query("DELETE from sound")
    fun deleteAllData()

    @Insert
    fun insert(sound: Sound)

    @Delete
    fun delete(sound: Sound)
}