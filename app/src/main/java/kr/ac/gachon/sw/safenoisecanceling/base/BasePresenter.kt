package kr.ac.gachon.sw.safenoisecanceling.base

interface BasePresenter<T> {
    fun createView(view: T)
    fun destroyView()
}