package kr.ac.gachon.sw.safenoisecanceling.ui.splash

class SplashPresenter: SplashContract.Presenter {
    private var mView: SplashContract.View? = null

    override fun createView(view: SplashContract.View) {
        mView = view
    }

    override fun destroyView() {
        mView = null
    }
}