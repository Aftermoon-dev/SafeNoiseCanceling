package kr.ac.gachon.sw.safenoisecanceling.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B: ViewBinding>(private val bind: (View) -> B, @LayoutRes layoutResId: Int): Fragment(layoutResId) {
    private var _viewBinding: B? = null

    protected val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initPresenter()
        _viewBinding = bind(super.onCreateView(inflater, container, savedInstanceState)!!)
        return viewBinding.root
    }

    abstract fun initPresenter()

    override fun onDestroyView() {
        _viewBinding = null
        super.onDestroyView()
    }
}