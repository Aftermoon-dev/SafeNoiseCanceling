package kr.ac.gachon.sw.safenoisecanceling.ui.history

import kr.ac.gachon.sw.safenoisecanceling.models.DatabaseModel
import kr.ac.gachon.sw.safenoisecanceling.models.Sound

class HistoryPresenter: HistoryContract.Presenter {
    private var historyView: HistoryContract.View? = null
    override fun getSoundListByPaging(startIdx: Int, itemCnt: Int): List<Sound> {
        return DatabaseModel.getSoundDatabyIndex(startIdx, itemCnt)
    }

    override fun createView(view: HistoryContract.View) {
        historyView = view
    }

    override fun destroyView() {
        historyView = null
    }
}