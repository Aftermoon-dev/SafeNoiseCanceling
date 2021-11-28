package kr.ac.gachon.sw.safenoisecanceling.ui.setting

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface SettingContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View>
}