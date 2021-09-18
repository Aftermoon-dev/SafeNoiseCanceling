package kr.ac.gachon.sw.safenoisecanceling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.transitions.Transitions
import kr.ac.gachon.sw.safenoisecanceling.transitions.TransitionsReceiver
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class SoundClassificationService: Service() {
    private lateinit var transitionReceiver: TransitionsReceiver
    private lateinit var transitionRequest: ActivityTransitionRequest
    private lateinit var transitionPendingIntent: PendingIntent

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 권한 못 받았으면 서비스 종료
        if (!Utils.checkActivityRecognitionPermission(applicationContext)) {
            stopSelf()
        }

        initForegroundService()

        // Transition Receiver 등록
        transitionReceiver = TransitionsReceiver()
        registerReceiver(transitionReceiver, IntentFilter(ApplicationClass.TRANSITIONS_RECEIVER_ACTION))

        // Transition Update 등록
        registerTransitionUpdate()

        // PendingIntent 등록
        transitionRequest = ActivityTransitionRequest(Transitions.transitionList)
        val transitionIntent = Intent(ApplicationClass.TRANSITIONS_RECEIVER_ACTION)
        transitionPendingIntent = PendingIntent.getBroadcast(this, 0, transitionIntent, 0)

        return START_STICKY
    }

    override fun onDestroy() {
        // 상태 변화 업데이트 해제
        unregisterTransitionUpdate()

        // Transition Receiver 해제
        unregisterReceiver(transitionReceiver)
        super.onDestroy()
    }

    /**
     * 활동 변화 Update 등록
     * @author Minjae Seon
     * @reference https://readystory.tistory.com/198?category=861095
     */
    private fun registerTransitionUpdate() {
        ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(transitionRequest, transitionPendingIntent)
            .addOnSuccessListener {
                Log.d("SCService", "Success to to Register Transition Update")
            }.addOnFailureListener {
                Log.e("SCService", "Failed to Register Transition Update!", it)
            }
    }

    /**
     * 활동 변화 Update 등록 해제
     * @author Minjae Seon
     * @reference https://readystory.tistory.com/198?category=861095
     */
    private fun unregisterTransitionUpdate() {
        ActivityRecognition.getClient(this)
            .removeActivityTransitionUpdates(transitionPendingIntent)
            .addOnSuccessListener {
                Log.d("SCService", "Success to to Unregister Transition Update")
            }.addOnFailureListener {
                Log.e("SCService", "Failed to Unregister Transition Update!", it)
            }
    }

    /**
     * 꺼지지 않는 Foreground Service 설정
     * @author Minjae Seon
     */
    private fun initForegroundService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiSettingIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, ApplicationClass.SC_SERVICE_CHANNEL_ID)

            val pendingIntent = PendingIntent.getActivity(this, 0, notiSettingIntent, 0)

            val notiChannel = NotificationChannel(ApplicationClass.SC_SERVICE_CHANNEL_ID, getString(
                R.string.soundservice_noti_channel), NotificationManager.IMPORTANCE_LOW)

            val notiBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, ApplicationClass.SC_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.soundservice_noti_title))
                .setContentText(getString(R.string.soundservice_noti_msg))
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notiChannel)
            startForeground(1, notiBuilder.build())
        }
    }
}