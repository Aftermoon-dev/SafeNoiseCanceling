package kr.ac.gachon.sw.safenoisecanceling.ui.home

import android.util.Log
import io.reactivex.rxjava3.disposables.Disposable
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class HomePresenter: HomeContract.Presenter {
    private val TAG = this.javaClass.simpleName
    private var homeView: HomeContract.View? = null
    private var currentActivityDispose: Disposable? = null

    override fun subscribeActivityEvent() {
        Log.d(TAG, "CurrentActivity Subscribed")
        currentActivityDispose = ApplicationClass.rxEventBus.receiveEvent("currentActivity").subscribe ({
            val activityNum = it as Int
            homeView!!.updateCurrentActivity(Utils.activityToString(activityNum))
        }, {
            Log.e(TAG, "Get Activity Event Failed", it)
        })
    }

    override fun unsubscribeActivityEvent() {
        if(currentActivityDispose != null) {
            Log.d(TAG, "CurrentActivity Disposed")
            currentActivityDispose!!.dispose()
        }
    }


    override fun createView(view: HomeContract.View) {
        homeView = view
    }

    override fun destroyView() {
        homeView = null
    }
}