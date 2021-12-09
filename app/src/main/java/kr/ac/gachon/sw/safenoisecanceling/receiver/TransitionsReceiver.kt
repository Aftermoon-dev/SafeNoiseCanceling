package kr.ac.gachon.sw.safenoisecanceling.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.utils.RxEventBus

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

                        // 행동에 진입하는 경우
                        if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            // 현재 Activity를 실시간 Update하기 위해 Event 전송
                            ApplicationClass.rxEventBus.sendEvent("currentActivity", event.activityType)

                            // 안 켜져있을 수도 있으므로 켰을때 바로 볼 수 있도록 SP에 저장
                            ApplicationClass.SharedPreferences.lastActivity = event.activityType

                            // Activity Type에 따라 Threshold 설정
                            when(event.activityType) {
                                // 걷거나 달리는 경우
                                DetectedActivity.ON_FOOT ->
                                    // 0.6 (60% 이상) 일치해야 함
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.6f
                                // 달리는 경우
                                DetectedActivity.RUNNING ->
                                    // 0.55 (55%) 이상 일치해야 함
                                    // 달리니까 바람소리가 들어갈 수 있음을 고려해서 낮게
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.55f
                                // 걷는 경우
                                DetectedActivity.WALKING ->
                                    // 0.6 (60% 이상) 일치해야 함
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.6f
                                // 자전거 타는 경우
                                DetectedActivity.ON_BICYCLE ->
                                    // 0.4 (40%) 이상 일치해야 함
                                    // 달리니까 바람소리가 들어갈 수 있음을 고려해서 낮게
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.4f
                                // 탈 것에 타고 있는 경우
                                DetectedActivity.IN_VEHICLE ->
                                    // 0.99 (99%) 일치해야 함
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.99f
                                // 그 이외의 경우
                                else ->
                                    // 0.6 (60% 이상) 일치해야 함
                                    ApplicationClass.SharedPreferences.classifyThresholds = 0.6f
                            }

                            Log.d("TransReceiver", "Currently Threshold : ${ApplicationClass.SharedPreferences.classifyThresholds}")
                        }
                    }
                }
            }
        }
    }
}