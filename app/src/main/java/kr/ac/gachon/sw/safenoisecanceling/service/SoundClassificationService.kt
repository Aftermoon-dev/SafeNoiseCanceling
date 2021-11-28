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
import kr.ac.gachon.sw.safenoisecanceling.models.DatabaseModel
import kr.ac.gachon.sw.safenoisecanceling.receiver.TransitionsReceiver
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

class SoundClassificationService: Service() {
    // LOG Tag
    private val TAG: String = "SCService"

    /** Sound Classification **/
    // 소리 인식할 카테고리 리스트
    private val checkCategories: ArrayList<Int> = arrayListOf(294, 300, 301, 302, 303, 304, 305)

    // Audio Classifier
    private var audioClassifier: AudioClassifier? = null

    // 녹음
    private var audioRecord: AudioRecord? = null

    // 인식 주기
    private var classificationInterval = 500L

    // Background에서 소리 녹음 및 인식을 처리하기 위한 Handler
    private lateinit var handler: Handler

    /** Activity Recognition **/

    // 변화 동작을 받을 BroadcastReceiver
    private lateinit var transitionReceiver: TransitionsReceiver

    // App이 Activity Transition 변화 요청을 받도록 요청하는 Object
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
            Log.d(TAG, "Permission not allowed, stopSelf.")
            stopSelf()
        }

        // 서비스 비활성화 되어있으면 서비스 종료
        if (!ApplicationClass.SharedPreferences.isSNCEnable) {
            Log.d(TAG, "Service is not Enabled, stopSelf.")
            stopSelf()
        }

        Log.d(TAG, "Service Start!")

        // Foreground Service 관련 초기화
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

        // TF Lite Model 파일을 불러와서 Audio Classifier 생성
        val classifier = AudioClassifier.createFromFile(this, ApplicationClass.YAMNET_MODEL_FILE)

        // Input Tensor Audio 생성
        val audioTensor = classifier.createInputTensorAudio()

        // Audio Recording 시작
        val record = classifier.createAudioRecord()
        record.startRecording()

        // 인식 진행하는 Runnable Object
        val run = object : Runnable {
            override fun run() {
                // 마지막으로 녹음된 Record 가져옴
                audioTensor.load(record)

                // 아까 생성한 Classifier로 소리 분석
                val output = classifier.classify(audioTensor)

                // Threshold 값 이상으로 감지된 카테고리를 넣고 내림차순으로 정렬
                val filteredModelOutput = output[0].categories.filter {
                    // 이때 Threshold 값은 SP에서 받아옴 (SP 값 변경은 TransitionsReceiver에서 처리)
                    it.score > ApplicationClass.SharedPreferences.classifyThresholds
                }.sortedBy {
                    -it.score
                }

                // filteredModelOutput이 현재 인식된 카테고리
                for(category in filteredModelOutput) {
                    // Logging
                    Log.d(TAG, "Detected - ${category.index} / ${category.label} / ${category.score}")

                    // 데이터베이스에 인식 정보 쓰기
                    DatabaseModel.writeNewClassificationData(category.label, category.score)
                }

                // 반복 주기만큼 Delayed
                handler.postDelayed(this, classificationInterval)
            }
        }

        // 인식 시작
        handler.post(run)

        // 나중에 또 사용할 수 있도록 Instance 저장
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