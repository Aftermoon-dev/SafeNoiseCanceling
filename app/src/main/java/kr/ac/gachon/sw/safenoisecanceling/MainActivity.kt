package kr.ac.gachon.sw.safenoisecanceling

import android.os.Bundle
import kr.ac.gachon.sw.safenoisecanceling.base.BaseActivity
import kr.ac.gachon.sw.safenoisecanceling.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate), MainContract.View {
    private lateinit var mPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun initPresenter() {
        mPresenter = MainPresenter()
    }
}


