package kr.ac.gachon.sw.safenoisecanceling.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransitionResult
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass

class TransitionsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ApplicationClass.TRANSITIONS_RECEIVER_ACTION) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result: ActivityTransitionResult? = ActivityTransitionResult.extractResult(intent)

                if(result != null) {
                    for (event in result.transitionEvents) {
                        Log.d("TransReceiver", "Current Activity : ${event.activityType} ${event.transitionType}")

                        // TODO : transitionType이 0일 때, activityType에 따라서 Threshold 값 조절
                        // event.activityType : 변화를 감지한 Event
                        // event.transitionType : activityType에 진입 or 빠져나감 (0이 진입)
                        // Threshold Value는 ApplicationClass.SharedPreferences.classifyThresholds 에다가 Float 형태로 Assign
                        // ex. ApplicationClass.SharedPreferences.classifyThresholds = 0.3f
                    }
                }
            }
        }
    }
}