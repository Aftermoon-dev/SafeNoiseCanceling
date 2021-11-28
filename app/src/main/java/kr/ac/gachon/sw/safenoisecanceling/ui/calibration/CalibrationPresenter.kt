package kr.ac.gachon.sw.safenoisecanceling.ui.calibration

class CalibrationPresenter: CalibrationContract.Presenter {
    private var mView: CalibrationContract.View? = null

    override fun createView(view: CalibrationContract.View) {
        mView = view
    }

    override fun destroyView() {
        mView = null
    }
}