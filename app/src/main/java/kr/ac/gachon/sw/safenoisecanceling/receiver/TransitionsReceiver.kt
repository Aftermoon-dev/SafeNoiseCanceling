package kr.ac.gachon.sw.safenoisecanceling.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.ActivityTransitionResult
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class TransitionsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ApplicationClass.TRANSITIONS_RECEIVER_ACTION) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result: ActivityTransitionResult? = ActivityTransitionResult.extractResult(intent)

                if(result != null) {
                    val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notiChannel = NotificationChannel("transition-test", "트랜지션 테스트", NotificationManager.IMPORTANCE_DEFAULT)
                        notificationManager.createNotificationChannel(notiChannel)
                    }

                    for (event in result.transitionEvents) {
                        Log.d("TransReceiver", "Current Activity : ${event.activityType} ${event.transitionType}")

                        val notiBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context!!, ApplicationClass.SC_SERVICE_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("상태 변화 감지")
                            .setContentText("현재 상태 : ${Utils.activityToString(event.activityType)} ${event.transitionType}")
                            .setStyle(NotificationCompat.BigTextStyle())
                        notificationManager.notify(event.activityType, notiBuilder.build())

                    }
                }
            }
        }
    }
}