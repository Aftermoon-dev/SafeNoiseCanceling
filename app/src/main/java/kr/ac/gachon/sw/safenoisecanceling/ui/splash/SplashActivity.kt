package kr.ac.gachon.sw.safenoisecanceling.ui.splash


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivitySplashBinding
import kr.ac.gachon.sw.safenoisecanceling.ui.main.MainActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate),
    SplashContract.View {
    private lateinit var mPresenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.createView(this)

        Handler(Looper.getMainLooper()).postDelayed ({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    override fun initPresenter() {
        mPresenter = SplashPresenter()
    }
}