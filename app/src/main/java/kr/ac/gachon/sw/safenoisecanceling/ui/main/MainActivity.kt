package kr.ac.gachon.sw.safenoisecanceling.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivityMainBinding
import kr.ac.gachon.sw.safenoisecanceling.service.SoundClassificationService
import kr.ac.gachon.sw.safenoisecanceling.ui.calibration.CalibrationActivity
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    MainContract.View {
    private lateinit var mPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.createView(this)

        val navView: BottomNavigationView = viewBinding.navView

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_setting
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        permissionCheck()
    }

    override fun initPresenter() {
        mPresenter = MainPresenter()
    }

    /**
    * Permission ??????
    * @author Minjae Seon
    */
    private fun permissionCheck() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Log.d("MainActivity", "Permission Granted")

                // ?????? Max Decibel??? ?????? ?????????
                if(ApplicationClass.SharedPreferences.baseMaxDecibel == Float.MIN_VALUE) {
                    // Calibration ???????????? Return
                    startActivity(Intent(this@MainActivity, CalibrationActivity::class.java))
                    return
                }

                // ?????? ???????????? ?????? ????????? ??????
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
}