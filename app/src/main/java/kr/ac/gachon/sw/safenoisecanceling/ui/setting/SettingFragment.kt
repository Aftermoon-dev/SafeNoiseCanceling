package kr.ac.gachon.sw.safenoisecanceling.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseFragment
import kr.ac.gachon.sw.safenoisecanceling.databinding.FragmentSettingBinding
import kr.ac.gachon.sw.safenoisecanceling.service.SoundClassificationService
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::bind, R.layout.fragment_setting), SettingContract.View {
    private lateinit var settingPresenter: SettingPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingPresenter.createView(this)
        setServiceSwitch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        settingPresenter.destroyView()
    }

    override fun initPresenter() {
        settingPresenter = SettingPresenter()
    }

    /**
     * 권한 및 ON/OFF 여부에 따른 Switch 초기 설정
     * @author Minjae Seon
     */
    private fun setServiceSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.swcEnableService.isChecked = Utils.checkPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) &&
                    Utils.checkPermission(requireContext(), android.Manifest.permission.ACTIVITY_RECOGNITION) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }
        else {
            viewBinding.swcEnableService.isChecked = Utils.checkPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }
        ApplicationClass.SharedPreferences.isSNCEnable = viewBinding.swcEnableService.isChecked

        viewBinding.swcEnableService.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("MainActivity", "SNCService Switch Changed $isChecked")

            ApplicationClass.SharedPreferences.isSNCEnable = isChecked

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
    }

}