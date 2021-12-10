package kr.ac.gachon.sw.safenoisecanceling.ui.home

import android.util.Log
import io.reactivex.rxjava3.disposables.Disposable
import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass
import kr.ac.gachon.sw.safenoisecanceling.utils.Utils

class HomePresenter: HomeContract.Presenter {
    private val TAG = this.javaClass.simpleName
    private var homeView: HomeContract.View? = null
    private var currentActivityDisposable: Disposable? = null
    private var currentDecibelDisposable: Disposable? = null

    override fun subscribeActivityEvent() {
        Log.d(TAG, "CurrentActivity Subscribed")
        currentActivityDisposable = ApplicationClass.rxEventBus.receiveEvent("currentActivity").subscribe ({
            val activityNum = it as Int
            homeView!!.updateCurrentActivity(activityNum)
        }, {
            Log.e(TAG, "Get Activity Event Failed", it)
        })
    }

    override fun unsubscribeActivityEvent() {
        if(currentActivityDisposable != null) {
            Log.d(TAG, "CurrentActivity Disposed")
            currentActivityDisposable!!.dispose()
        }
    }

    override fun subscribeDecibelEvent() {
        Log.d(TAG, "CurrentDecibel Subscribed")
        currentDecibelDisposable = ApplicationClass.rxEventBus.receiveEvent("currentDecibel").subscribe ({
            val decibelVal = it as Float
            homeView!!.updateCurrentDecibel(decibelVal)
        }, {
            Log.e(TAG, "Get Decibel Event Failed", it)
        })
    }

    override fun unsubscribeDecibelEvent() {
        if(currentDecibelDisposable != null) {
            Log.d(TAG, "CurrentDecibel Disposed")
            currentDecibelDisposable!!.dispose()
        }
    }


    override fun createView(view: HomeContract.View) {
        homeView = view
    }

    override fun destroyView() {
        homeView = null
    }
}