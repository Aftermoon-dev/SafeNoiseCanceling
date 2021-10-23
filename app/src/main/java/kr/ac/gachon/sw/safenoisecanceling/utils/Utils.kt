package kr.ac.gachon.sw.safenoisecanceling.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

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
}