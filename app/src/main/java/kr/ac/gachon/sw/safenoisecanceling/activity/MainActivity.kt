package kr.ac.gachon.sw.safenoisecanceling.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivityMainBinding
import kr.ac.gachon.sw.safenoisecanceling.service.SoundClassificationService
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    MainContract.View {
    private lateinit var mPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.createView(this)

        // 퍼미션 확인
        permissionCheck()

        // 스위치 초기 값 설정
        setServiceSwitch()
    }

    override fun initPresenter() {
        mPresenter = MainPresenter()
    }

    /**
     * Permission 확인
     * @author Minjae Seon
     */
    private fun permissionCheck() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Log.d("MainActivity", "Permission Granted")
                Intent(this@MainActivity, SoundClassificationService::class.java).also {
                    if (ApplicationClass.SharedPreferences.isSNCEnable) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (Utils.checkPermission(this@MainActivity, android.Manifest.permission.ACTIVITY_RECOGNITION)
                                && Utils.checkPermission(this@MainActivity, android.Manifest.permission.RECORD_AUDIO))
                                    startForegroundService(it)
                        } else {
                            startService(it)
                        }
                    }
                }
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Log.d("MainActivity", "Permission Denied")
                Toast.makeText(this@MainActivity, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                finish()
            }
        }

        val permission = TedPermission.create()
            .setPermissionListener(permissionListener)
            .setRationaleMessage(R.string.permission_req_msg)
            .setDeniedMessage(R.string.permission_req_msg)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permission.setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACTIVITY_RECOGNITION)
        }
        else {
            permission.setPermissions(Manifest.permission.RECORD_AUDIO)
        }

        permission.check()
    }

    /**
     * 권한 및 ON/OFF 여부에 따른 Switch 초기 설정
     * @author Minjae Seon
     */
    private fun setServiceSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.swcSncservice.isChecked = Utils.checkPermission(this@MainActivity, android.Manifest.permission.RECORD_AUDIO) &&
                    Utils.checkPermission(this@MainActivity, android.Manifest.permission.ACTIVITY_RECOGNITION) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }
        else {
            viewBinding.swcSncservice.isChecked = Utils.checkPermission(this@MainActivity, android.Manifest.permission.RECORD_AUDIO) &&
                    ApplicationClass.SharedPreferences.isSNCEnable
        }
        ApplicationClass.SharedPreferences.isSNCEnable = viewBinding.swcSncservice.isChecked

        viewBinding.swcSncservice.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("MainActivity", "SNCService Switch Changed $isChecked")

            ApplicationClass.SharedPreferences.isSNCEnable = isChecked

            if (isChecked) {
                Intent(this, SoundClassificationService::class.java).also {
                    if (ApplicationClass.SharedPreferences.isSNCEnable) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (Utils.checkPermission(this@MainActivity, android.Manifest.permission.RECORD_AUDIO) &&
                                Utils.checkPermission(this@MainActivity, android.Manifest.permission.ACTIVITY_RECOGNITION))
                                    startForegroundService(it)
                        } else {
                            startService(it)
                        }
                    }
                }
            }
            else {
                stopService(Intent(this, SoundClassificationService::class.java))
            }
        }
    }
}


