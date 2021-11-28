package kr.ac.gachon.sw.safenoisecanceling.ui.home

class HomePresenter: HomeContract.Presenter {
    private var homeView: HomeContract.View? = null

    override fun createView(view: HomeContract.View) {
        homeView = view
    }

    override fun destroyView() {
        homeView = null
    }
}