package kr.ac.gachon.sw.safenoisecanceling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioRecord
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.HandlerCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.receiver.TransitionsReceiver
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

class SoundClassificationService: Service() {
    private val TAG: String = "SCService"
    private var MINIMUM_DISPLAY_THRESHOLD: Float = 0.3f

    private val checkCategories: ArrayList<Int> = arrayListOf(294, 300, 301, 302, 303, 304, 305)
    private var audioClassifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private var classificationInterval = 500L
    private lateinit var handler: Handler

    private lateinit var transitionReceiver: TransitionsReceiver
    private lateinit var transitionRequest: ActivityTransitionRequest
    private lateinit var transitionPendingIntent: PendingIntent
    private val transitionList: List<ActivityTransition> = listOf(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_BICYCLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_BICYCLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_FOOT)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_FOOT)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
    )

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 권한 못 받았으면 서비스 종료
        if (!Utils.checkPermission(applicationContext, android.Manifest.permission.ACTIVITY_RECOGNITION)
            && !Utils.checkPermission(applicationContext, android.Manifest.permission.RECORD_AUDIO)) {
            Log.d("SCService", "Permission not allowed, stopSelf.")
            stopSelf()
        }

        // 서비스 비활성화 되어있으면 서비스 종료
        if (!ApplicationClass.SharedPreferences.isSNCEnable) {
            Log.d("SCService", "Service is not Enabled, stopSelf.")
            stopSelf()
        }

        Log.d("SCService", "Service Start!")

        initForegroundService()

        // Transition Receiver 등록
        transitionReceiver = TransitionsReceiver()
        registerReceiver(transitionReceiver, IntentFilter(ApplicationClass.TRANSITIONS_RECEIVER_ACTION))

        // PendingIntent 등록
        transitionRequest = ActivityTransitionRequest(transitionList)
        val transitionIntent = Intent(ApplicationClass.TRANSITIONS_RECEIVER_ACTION)
        transitionPendingIntent = PendingIntent.getBroadcast(this, 0, transitionIntent, 0)

        // Transition Update 등록
        registerTransitionUpdate()

        // Handler 생성
        val handlerThread = HandlerThread("backgroundThread")
        handlerThread.start()
        handler = HandlerCompat.createAsync(handlerThread.looper)

        // 인식 시작
        startClassification()

        return START_STICKY
    }

    override fun onDestroy() {
        // 인식 해제
        stopAudioClassification()

        // Transition Receiver 해제
        unregisterReceiver(transitionReceiver)

        // 상태 변화 업데이트 해제
        unregisterTransitionUpdate()

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
     * 인식 Start
     */
    private fun startClassification() {
        if (audioClassifier != null) return

        val classifier = AudioClassifier.createFromFile(this, "yamnet.tflite")
        val audioTensor = classifier.createInputTensorAudio()

        val record = classifier.createAudioRecord()
        record.startRecording()

        // Define the classification runnable
        val run = object : Runnable {
            override fun run() {
                // Load the latest audio sample
                audioTensor.load(record)
                val output = classifier.classify(audioTensor)

                // Filter out results above a certain threshold, and sort them descendingly
                val filteredModelOutput = output[0].categories.filter {
                    it.score > MINIMUM_DISPLAY_THRESHOLD
                }.sortedBy {
                    -it.score
                }

                // filteredModelOutput이 현재 인식된 카테고리
                for(category in filteredModelOutput) {
                    // category의 index가 checkCategories에 포함되었다면
                    if(category.index in checkCategories)  {
                        // Log 출력
                        Log.d(TAG, "Detected - ${category.index} / ${category.label} / ${category.score}")
                    }
                }
                handler.postDelayed(this, classificationInterval)
            }
        }

        // Start the classification process
        handler.post(run)

        // Save the instances we just created for use later
        audioClassifier = classifier
        audioRecord = record
    }

    /**
     * 인식 Stop
     */
    private fun stopAudioClassification() {
        handler.removeCallbacksAndMessages(null)
        audioRecord?.stop()
        audioRecord = null
        audioClassifier = null
    }

    /**
     * 꺼지지 않는 Foreground Service 설정
     * @author Minjae Seon
     */
    private fun initForegroundService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("SCService", "Init Foreground Service Notification..")
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