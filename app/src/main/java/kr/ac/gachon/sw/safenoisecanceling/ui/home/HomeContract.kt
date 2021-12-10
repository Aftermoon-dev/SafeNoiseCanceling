package kr.ac.gachon.sw.safenoisecanceling.ui.home

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface HomeContract {
    interface View: BaseView {
        fun updateCurrentActivity(activityType: Int)
        fun updateCurrentDecibel(decibelValue: Float)
    }
    interface Presenter: BasePresenter<View> {
        fun subscribeActivityEvent()
        fun unsubscribeActivityEvent()
        fun subscribeDecibelEvent()
        fun unsubscribeDecibelEvent()
    }
}