package kr.ac.gachon.sw.safenoisecanceling.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sound(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name="type") val soundType: String,
    @ColumnInfo(name="score") val score: Float,
    @ColumnInfo(name="time") val time: Long
)
