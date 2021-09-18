package kr.ac.gachon.sw.safenoisecanceling

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface MainContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View>
}