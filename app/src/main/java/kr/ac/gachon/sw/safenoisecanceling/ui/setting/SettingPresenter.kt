package kr.ac.gachon.sw.safenoisecanceling.ui.setting

class SettingPresenter: SettingContract.Presenter {
    private var settingView: SettingContract.View? = null

    override fun createView(view: SettingContract.View) {
        settingView = view
    }

    override fun destroyView() {
        settingView = null
    }
}