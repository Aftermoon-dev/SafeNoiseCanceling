package kr.ac.gachon.sw.safenoisecanceling.ui.main

class MainPresenter: MainContract.Presenter {
    private var mView: MainContract.View? = null

    override fun createView(view: MainContract.View) {
        mView = view
    }

    override fun destroyView() {
        mView = null
    }
}