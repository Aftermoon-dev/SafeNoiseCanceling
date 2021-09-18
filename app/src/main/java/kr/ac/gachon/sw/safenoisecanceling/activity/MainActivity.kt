package kr.ac.gachon.sw.safenoisecanceling.activity

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    MainContract.View {
    private lateinit var mPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 퍼미션 확인
        permissionCheck()
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
                // 권한 허용했으므로 Pass
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                finish()
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage(R.string.permission_req_msg)
            .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACTIVITY_RECOGNITION)
            .check()
    }
}


