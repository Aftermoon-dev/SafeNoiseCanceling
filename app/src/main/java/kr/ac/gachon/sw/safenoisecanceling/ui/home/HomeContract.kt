package kr.ac.gachon.sw.safenoisecanceling.ui.home

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface HomeContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View>
}