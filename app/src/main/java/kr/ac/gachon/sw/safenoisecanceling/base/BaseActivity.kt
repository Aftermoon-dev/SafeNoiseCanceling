package kr.ac.gachon.sw.safenoisecanceling.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B: ViewBinding>(private val inflate: (LayoutInflater) -> B): AppCompatActivity() {
    protected lateinit var viewBinding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = inflate(layoutInflater)
        setContentView(viewBinding.root)

        initPresenter()
    }

    abstract fun initPresenter()
}