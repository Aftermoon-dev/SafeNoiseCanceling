package kr.ac.gachon.sw.safenoisecanceling.ui.setting

import kr.ac.gachon.sw.safenoisecanceling.models.DatabaseModel

class SettingPresenter: SettingContract.Presenter {
    private var settingView: SettingContract.View? = null

    override fun deleteAllSoundData() {
        DatabaseModel.deleteAllSoundData()
    }

    override fun createView(view: SettingContract.View) {
        settingView = view
    }

    override fun destroyView() {
        settingView = null
    }
}