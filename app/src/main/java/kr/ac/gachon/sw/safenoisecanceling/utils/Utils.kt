package kr.ac.gachon.sw.safenoisecanceling.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
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
     * Activity Number to String
     * @author Minjae Seon
     * @return 활동 번호를 String 형태로 변환
     */
    fun activityToString(activitynum: Int): String {
        if (activitynum == 0) {
            return "차량 안"
        }
        else if (activitynum == 1) {
            return "자전거"
        }
        else if (activitynum == 2) {
            return "도보 (걷기 or 뛰기)"
        }
        else if (activitynum == 3) {
            return "가만히 있음"
        }
        else if (activitynum == 5) {
            return "기기 기울어짐"
        }
        else if (activitynum == 7) {
            return "걷기"
        }
        else if (activitynum == 8) {
            return "뛰기"
        }
        return "알 수 없음"
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
}