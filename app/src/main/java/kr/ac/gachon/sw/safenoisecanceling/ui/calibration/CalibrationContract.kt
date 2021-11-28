package kr.ac.gachon.sw.safenoisecanceling.ui.calibration

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView

interface CalibrationContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View>
}