package kr.ac.gachon.sw.safenoisecanceling.ui.calibration

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivityCalibrationBinding
import kr.ac.gachon.sw.safenoisecanceling.service.SoundClassificationService
import kr.ac.gachon.sw.safenoisecanceling.ui.LoadingDialog
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class CalibrationActivity: BaseActivity<ActivityCalibrationBinding>(ActivityCalibrationBinding::inflate),
    CalibrationContract.View {
    private lateinit var mPresenter: CalibrationPresenter
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.createView(this)
        loadingDialog = LoadingDialog(this)
        setClickListener()
    }

    override fun initPresenter() {
        mPresenter = CalibrationPresenter()
    }

    // 뒤로가기 막기
    override fun onBackPressed() { }

    private fun setClickListener() {
        viewBinding.btnStartrecord.setOnClickListener {
            val serviceIntent = Intent(this@CalibrationActivity, SoundClassificationService::class.java)
            serviceIntent.putExtra("isCalibration", true)

            if (Utils.checkPermission(this@CalibrationActivity, android.Manifest.permission.ACTIVITY_RECOGNITION) && Utils.checkPermission(this@CalibrationActivity, android.Manifest.permission.RECORD_AUDIO)) {
                loadingDialog.show()
                viewBinding.tvCalibrationBottomMsg.text = getString(R.string.calibration_bottom_msg_during)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this@CalibrationActivity.startForegroundService(serviceIntent)
                } else {
                    this@CalibrationActivity.startService(serviceIntent)
                }
            }
            else {
                Toast.makeText(this@CalibrationActivity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                finish()
            }

            // 30초 후 종료
            Handler(Looper.getMainLooper()).postDelayed({
                stopService(serviceIntent)
                loadingDialog.dismiss()
                Toast.makeText(this@CalibrationActivity, getString(R.string.calibration_finish), Toast.LENGTH_SHORT).show()
                finish()
            }, 30000)
        }
    }
}
