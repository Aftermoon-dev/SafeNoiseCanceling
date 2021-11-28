package kr.ac.gachon.sw.safenoisecanceling.ui.history

import kr.ac.gachon.sw.safenoisecanceling.base.BasePresenter
import kr.ac.gachon.sw.safenoisecanceling.base.BaseView
import kr.ac.gachon.sw.safenoisecanceling.models.Sound

interface HistoryContract {
    interface View: BaseView
    interface Presenter: BasePresenter<View> {
        fun getSoundListByPaging(startIdx: Int, itemCnt: Int): List<Sound>
    }
}