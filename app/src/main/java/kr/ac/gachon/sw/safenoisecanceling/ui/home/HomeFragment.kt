package kr.ac.gachon.sw.safenoisecanceling.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseFragment
import kr.ac.gachon.sw.safenoisecanceling.databinding.FragmentHomeBinding
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home), HomeContract.View {
    private lateinit var homePresenter: HomePresenter
    private lateinit var activityString: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homePresenter.createView(this)
        activityString = resources.getStringArray(R.array.activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homePresenter.destroyView()
    }

    override fun onStart() {
        super.onStart()
        homePresenter.subscribeActivityEvent()
        homePresenter.subscribeDecibelEvent()
        viewBinding.ivActivity.setImageResource(Utils.activityToDrawable(ApplicationClass.SharedPreferences.lastActivity))
        viewBinding.tvActivity.text = activityString[ApplicationClass.SharedPreferences.lastActivity]
        viewBinding.tvDecibel.text = getString(R.string.fragment_main_soundlevel, "0")
        viewBinding.tvDecibellevel.text = Utils.decibelToString(requireContext(), 0.0f)
    }

    override fun onPause() {
        super.onPause()
        homePresenter.unsubscribeActivityEvent()
        homePresenter.unsubscribeDecibelEvent()
    }

    override fun initPresenter() {
        homePresenter = HomePresenter()
    }

    override fun updateCurrentActivity(activityType: Int) {
        Handler(Looper.getMainLooper()).post {
            viewBinding.ivActivity.setImageResource(Utils.activityToDrawable(activityType))
            viewBinding.tvActivity.text = activityString[activityType]
        }
    }

    override fun updateCurrentDecibel(decibelValue: Float) {
        Handler(Looper.getMainLooper()).post {
            viewBinding.tvDecibel.text = getString(R.string.fragment_main_soundlevel, decibelValue.toInt().toString())
            viewBinding.tvDecibellevel.text = Utils.decibelToString(requireContext(), decibelValue)
        }
    }

}