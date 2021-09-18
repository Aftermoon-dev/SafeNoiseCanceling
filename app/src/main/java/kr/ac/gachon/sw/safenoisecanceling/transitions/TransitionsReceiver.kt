package kr.ac.gachon.sw.safenoisecanceling.transitions

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
                        Log.d("TransReceiver", "Current Activity : ${event.activityType}")
                    }
                }
            }
        }
    }
}