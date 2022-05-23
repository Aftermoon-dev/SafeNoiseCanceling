package kr.ac.gachon.sw.safenoisecanceling.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseFragment
import kr.ac.gachon.sw.safenoisecanceling.databinding.FragmentSettingBinding
import kr.ac.gachon.sw.safenoisecanceling.service.SoundClassificationService
import kr.ac.gachon.sw.safenoisecanceling.ui.calibration.CalibrationActivity
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::bind, R.layout.fragment_setting), SettingContract.View {
    private val TAG = "SettingFragment"
    private lateinit var settingPresenter: SettingPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingPresenter.createView(this)
        setInitValue()
        setListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        settingPresenter.destroyView()
    }

    override fun initPresenter() {
        settingPresenter = SettingPresenter()
    }

    /**
     * Event 설정
     * @author Minjae Seon
     */
    private fun setListener() {
        // 마이크 보정 Click Listener
        viewBinding.layoutCalibration.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.caution)
                message(R.string.setting_calibration_msg)
                positiveButton {
                    // 서비스 먼저 끄기
                    viewBinding.swcEnableService.isChecked = false
                    requireContext().stopService(Intent(requireContext(), SoundClassificationService::class.java))

                    // Calibration Activity로
                    val calibrationIntent = Intent(requireContext(), CalibrationActivity::class.java)
                    startActivity(calibrationIntent)
                }
                negativeButton { this.cancel() }
            }
        }

        // 기록 삭제 Click Listener
        viewBinding.layoutResethistory.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.warning)
                message(R.string.setting_resethistory_dialog_msg)
                positiveButton {
                    settingPresenter.deleteAllSoundData()
                }
                negativeButton { this.cancel() }
            }
        }

        viewBinding.sbClassifyperiod.addOnChangeListener { slider, value, fromUser ->
            if(fromUser) ApplicationClass.SharedPreferences.classifyPeriod = value.toLong()
            Log.d(TAG, "New Classify Period : $value, ${ApplicationClass.SharedPreferences.classifyPeriod}")
        }

        // 마이크 민감도 설정 Listener
        viewBinding.sbMicthreshold.addOnChangeListener { _, value, fromUser ->
            if(fromUser) ApplicationClass.SharedPreferences.micThresholds = value
            Log.d(TAG, "New Mic Threshold : ${ApplicationClass.SharedPreferences.micThresholds}")
        }
    }

    /**
     * 초기 값 설정
     * @author Minjae Seon
     */
    private fun setInitValue() {
        // Enable Switch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.swcEnableService.isChecked = Utils.checkPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) &&
                    Utils.checkPermission(requireContext(), android.Manifest.permission.ACTIVITY_RECOGNITION) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }
        else {
            viewBinding.swcEnableService.isChecked = Utils.checkPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }

        viewBinding.swcEnableSubway.isEnabled = viewBinding.swcEnableService.isChecked
        viewBinding.swcEnableMediaoff.isEnabled = viewBinding.swcEnableService.isChecked
        viewBinding.swcEnableService.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(TAG, "SNCService Switch Changed $isChecked")

            ApplicationClass.SharedPreferences.isSNCEnable = isChecked
            viewBinding.swcEnableMediaoff.isEnabled = isChecked

            if (isChecked) {
                Intent(requireContext(), SoundClassificationService::class.java).also {
                    if (ApplicationClass.SharedPreferences.isSNCEnable) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (Utils.checkPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) &&
                                Utils.checkPermission(requireContext(), android.Manifest.permission.ACTIVITY_RECOGNITION))
                                requireContext().startForegroundService(it)
                        } else {
                            requireContext().startService(it)
                        }
                    }
                }
            }
            else {
                requireContext().stopService(Intent(requireContext(), SoundClassificationService::class.java))
            }
        }

        // Notification Setting
        viewBinding.layoutNotisetting.setOnClickListener {
            val settingsIntent: Intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, ApplicationClass.SC_WARNING_CHANNEL_ID)
            startActivity(settingsIntent)
        }

        // Subway Detet Switch
        viewBinding.swcEnableSubway.isChecked = ApplicationClass.SharedPreferences.enableSubwayAnnounceDetect

        viewBinding.swcEnableSubway.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(TAG, "Subway Detect Switch Changed $isChecked")
            ApplicationClass.SharedPreferences.enableMediaOff = isChecked
        }

        // Media Enable Switch
        viewBinding.swcEnableMediaoff.isChecked = ApplicationClass.SharedPreferences.enableMediaOff

        viewBinding.swcEnableMediaoff.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(TAG, "Media Off Switch Changed $isChecked")
            ApplicationClass.SharedPreferences.enableMediaOff = isChecked
        }

        // Classify Period
        viewBinding.sbClassifyperiod.setLabelFormatter {
            return@setLabelFormatter getString(R.string.setting_classifyperiod_label, "%.1f".format(it / 1000.0))
        }
        viewBinding.sbClassifyperiod.setValues(ApplicationClass.SharedPreferences.classifyPeriod.toFloat())

        // Mic Threshold
        viewBinding.sbMicthreshold.setValues(ApplicationClass.SharedPreferences.micThresholds)
    }

}