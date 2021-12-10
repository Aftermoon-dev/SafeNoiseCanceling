package kr.ac.gachon.sw.safenoisecanceling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioRecord
import android.net.Uri
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
import kr.ac.gachon.sw.safenoisecanceling.ui.main.MainActivity
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import kotlin.math.round
import kotlin.math.roundToInt

class SoundClassificationService(): Service() {
    // LOG Tag
    private val TAG: String = "SCService"

    /** Sound Classification **/
    // 소리 인식할 카테고리 리스트
    private val checkCategories: ArrayList<Int> = arrayListOf(6, 9, 11, 198, 294, 300, 301, 302, 303, 304, 306, 307,
        308, 309, 310, 311, 312, 315, 316, 317, 318, 319, 320, 321,
        335, 337, 338, 342, 343, 345, 347, 390, 391, 393, 394, 419)

    // Audio Classifier
    private var audioClassifier: AudioClassifier? = null

    // 녹음
    private var audioRecord: AudioRecord? = null

    // Background에서 소리 녹음 및 인식을 처리하기 위한 Handler
    private lateinit var handler: Handler

    // Calibration을 위한 시작인지 확인
    private var isCalibration = false

    // Calibration용
    private var calibrationCnt: Int = 0
    private var calibrationSum: Float = 0.0f

    // Sound Level 목록
    private val soundLevelList: ArrayList<Float> = arrayListOf()

    // 이전 Max Amplitude
    private var mEMA: Float = 0.0F

    // Notificaiton
    private lateinit var notificationManager: NotificationManager
    private var lastNotificationTime: Long = Long.MAX_VALUE

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
        if(intent != null) {
            isCalibration = intent.getBooleanExtra("isCalibration", false)
        }

        // 권한 못 받았으면 서비스 종료
        if (!Utils.checkPermission(applicationContext, android.Manifest.permission.ACTIVITY_RECOGNITION)
            && !Utils.checkPermission(applicationContext, android.Manifest.permission.RECORD_AUDIO)) {
            Log.d(TAG, "Permission not allowed, stopSelf.")
            stopSelf()
        }

        // 캘리브레이션도 아니고 서비스 비활성화 되어있으면 서비스 종료
        if (!isCalibration && !ApplicationClass.SharedPreferences.isSNCEnable) {
            Log.d(TAG, "Service is not Enabled, stopSelf.")
            stopSelf()
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Service Start!\nCurrent Base Decibel : ${ApplicationClass.SharedPreferences.baseMaxDecibel}")
        Log.d(TAG, "isCalibration: $isCalibration")

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

        // 캘리브레이션 값 업데이트
        if(isCalibration) {
            ApplicationClass.SharedPreferences.baseMaxDecibel = (round( calibrationSum / calibrationCnt * 1000).roundToInt() / 1000.0f)
            Log.d(TAG, "Calibration Complete - ${ApplicationClass.SharedPreferences.baseMaxDecibel}")
        }

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
                Log.d("SCService", "Success to Register Transition Update")
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
                Log.d("SCService", "Success to Unregister Transition Update")
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
                if(!isCalibration && !ApplicationClass.SharedPreferences.isSNCEnable) return

                // Sound Buffer
                val newData = FloatArray(record.channelCount * record.bufferSizeInFrames)
                val loadedValues = record.read(newData, 0, newData.size, AudioRecord.READ_NON_BLOCKING)

                // Get Max Amplitude
                val maxAmplitude = newData.maxOrNull()

                // Decibel
                var decibel: Float? = null

                // Max Amplitude가 제대로 가져와진 경우
                if(maxAmplitude != null) {

                    // Decibel로 변환
                    decibel = Utils.convertDecibel(maxAmplitude, mEMA)

                    // 이번 Amplitude를 새로운 mEMA 값으로 지정
                    mEMA = maxAmplitude

                    Log.d(TAG, "Max Amplitude : $maxAmplitude \n convert : $decibel dB")

                    // 현재 Decibel을 Activity에서도 알 수 있도록 Event 전송
                    ApplicationClass.rxEventBus.sendEvent("currentDecibel", decibel)

                    // 평균 리스트가 5개 이상이면
                    while(soundLevelList.size > 4) {
                        // 가장 오래된 정보 삭제
                        soundLevelList.removeAt(0)
                    }
                    // 평균 리스트에 데시벨 추가
                    soundLevelList.add(decibel)
                }
                else {
                    Log.d(TAG, "Max Amplitude is Null!\n$newData")
                }

                // 캘리브레이션을 위한게 아니라면
                if(!isCalibration) {
                    // MaxAmplitude를 가져올 수 없었거나
                    if(maxAmplitude == null ||
                        // Decibel 정보가 Null이 아니고, 최근 5개 Decibel의 평균이 Calibration에서 측정된 값에 Threshold를 곱한 값보다 크다면
                        (decibel != null &&
                                (Utils.calculateAvginFloatList(soundLevelList) >= ApplicationClass.SharedPreferences.baseMaxDecibel + (ApplicationClass.SharedPreferences.baseMaxDecibel * ApplicationClass.SharedPreferences.micThresholds)))) {
                        // 마지막으로 녹음된 Record 가져옴
                        audioTensor.load(newData, 0, loadedValues)

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
                        for (category in filteredModelOutput) {
                            if (category.index == 494) continue

                            // Logging
                            Log.d(TAG, "Detected - ${category.index} / ${category.label} / ${category.score}")

                            if(category.index in checkCategories) {
                                sendNotification()
                            }

                            // 데이터베이스에 인식 정보 쓰기
                            DatabaseModel.writeNewClassificationData(category.label, category.score)
                        }
                    }
                }
                else {
                    Log.d(TAG, "Decibel $decibel")
                    // 캘리브레이션용 데이터 저장
                    if(maxAmplitude != null && decibel != null) {
                        calibrationCnt += 1
                        calibrationSum += decibel
                        Log.d(TAG, "Cnt $calibrationCnt, Current Decibel $decibel Sum $calibrationSum")
                    }

                }

                // 반복 주기만큼 Delayed
                handler.postDelayed(this, ApplicationClass.SharedPreferences.classifyPeriod)

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
     * 위험 알림 전송
     */
    private fun sendNotification() {
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.beep)
        val soundAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiChannel = NotificationChannel(ApplicationClass.SC_WARNING_CHANNEL_ID, getString(R.string.soundservice_warning_channel), NotificationManager.IMPORTANCE_HIGH)
            notiChannel.setSound(sound, soundAttributes)
            notificationManager.createNotificationChannel(notiChannel)
        }

        val notiBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, ApplicationClass.SC_WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.soundservice_warning_title))
            .setContentText(getString(R.string.soundservice_warning_msg))
            .setSound(sound)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(pendingIntent)

        // Notification Delay
        if(lastNotificationTime + 10000 <= System.currentTimeMillis()) {
            // 노래 일시정지
            Utils.pauseMediaPlay(applicationContext)
            lastNotificationTime = System.currentTimeMillis()
            notificationManager.notify(1000, notiBuilder.build())
        }
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

            notificationManager.createNotificationChannel(notiChannel)
            startForeground(1, notiBuilder.build())
        }
    }
}