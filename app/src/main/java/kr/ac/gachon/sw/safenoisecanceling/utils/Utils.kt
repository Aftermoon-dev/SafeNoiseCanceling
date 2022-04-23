package kr.ac.gachon.sw.safenoisecanceling.utils

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import androidx.core.app.ActivityCompat
import kr.ac.gachon.sw.safenoisecanceling.R
import java.util.zip.DataFormatException
import kotlin.math.log10

object Utils {
    /**
     * 권한 확인
     * @author Minjae Seon
     * @return If Permission Granted, True or False.
     */
    fun checkPermission(context: Context, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Activity Number to Drawable
     * @author Minjae Seon
     * @return 활동 번호를 Drawable Int 형태로 변환
     */
    fun activityToDrawable(activityNum: Int): Int {
        return when {
            activityNum == 0 ->
                R.drawable.car
            activityNum == 1 ->
                R.drawable.ic_baseline_directions_bike_24
            activityNum == 2 ->
                R.drawable.footprints
            activityNum == 3 ->
                R.drawable.person
            activityNum == 7 ->
                R.drawable.walk
            activityNum == 8 ->
                R.drawable.walk
            else ->
                R.drawable.ic_baseline_help_24
        }
    }

    /**
     * Decibel to String
     * @author Minjae Seon
     * @return 데시벨 수치에 따라 String으로 바꿈
     */
    fun decibelToString(context: Context, decibel: Float): String {
        val decibelStrArray = context.resources.getStringArray(R.array.decibel)
        return when {
            decibel < 10 -> "알 수 없음"
            decibel < 20 -> decibelStrArray[0]
            decibel < 30 -> decibelStrArray[1]
            decibel < 40 -> decibelStrArray[2]
            decibel < 50 -> decibelStrArray[3]
            decibel < 60 -> decibelStrArray[4]
            decibel < 70 -> decibelStrArray[5]
            decibel < 80 -> decibelStrArray[6]
            decibel < 90 -> decibelStrArray[7]
            else -> decibelStrArray[8]
        }
    }

    /**
     * Amplitude to Decibel
     * @author Minjae Seon
     * @reference https://github.com/wgcv/Sound-decibel-Meter/blob/master/dBT/app/src/main/java/me/wgcv/dbt/MainActivity.java
     * @param amp Amplitude
     * @param mEMA Before Amplitude (If First, 0)
     * @return Decibel (Double)
     */
    fun convertDecibel(amp: Float, mEMA: Float): Float {
        val convertAmp = (((amp * 32767) / 1) + 1)
        val EMA_FILTER = 0.6
        val mEMAValue = EMA_FILTER  * convertAmp + (1.0 - EMA_FILTER) * mEMA
        return 20 * log10((mEMAValue / 51805.5336) / 0.000028251).toFloat()
    }

    /**
     * Float List의 평균 계산
     * @author Minjae Seon
     * @param list List of Float
     * @return Average in List Elements
     */
    fun calculateAvginFloatList(list: List<Float>): Float {
        var sum = 0f
        for(value in list) {
            sum += value
        }

        return sum / list.size
    }

    /**
     * 미디어 (영상, 노래 등) 일시정지 신호 보내기
     * @author Minjae Seon
     * @param context Application Context
     */
    fun pauseMediaPlay(context: Context) {
        val eventTime = SystemClock.uptimeMillis() - 1
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                0
            )
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                0
            )
        )
    }

    /**
     * 볼륨 설정하기
     * @author Minjae Seon
     * @param context Context
     * @param newVolume New Volume (Integer)
     */
    fun setVolume(context: Context, newVolume: Int) {
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) >= newVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        else
            throw DataFormatException("newVolume too big")
    }
}