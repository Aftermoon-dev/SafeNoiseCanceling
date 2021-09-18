package kr.ac.gachon.sw.safenoisecanceling.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object Utils {
    /**
     * 활동 인식 권한 확인
     * @author Minjae Seon
     * @return If Permission Granted, True or False.
     */
    fun checkActivityRecognitionPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}