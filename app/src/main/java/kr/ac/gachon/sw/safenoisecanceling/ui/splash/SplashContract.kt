package kr.ac.gachon.sw.safenoisecanceling.ui.splash

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface SplashContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View>
}