package kr.ac.gachon.sw.safenoisecanceling.ui.home

import android.os.Bundle
import android.view.View
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseFragment
import kr.ac.gachon.sw.safenoisecanceling.databinding.FragmentHomeBinding
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home), HomeContract.View {
    private lateinit var homePresenter: HomePresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homePresenter.createView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homePresenter.destroyView()
    }

    override fun onStart() {
        super.onStart()
        homePresenter.subscribeActivityEvent()
        viewBinding.tvCurrentactivity.text = Utils.activityToString(ApplicationClass.SharedPreferences.lastActivity)
    }

    override fun onPause() {
        super.onPause()
        homePresenter.unsubscribeActivityEvent()
    }

    override fun initPresenter() {
        homePresenter = HomePresenter()
    }

    override fun updateCurrentActivity(activityStr: String) {
        viewBinding.tvCurrentactivity.text = activityStr
    }

}