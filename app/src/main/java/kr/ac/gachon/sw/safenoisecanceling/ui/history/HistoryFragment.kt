package kr.ac.gachon.sw.safenoisecanceling.ui.history

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.base.BaseFragment
import kr.ac.gachon.sw.safenoisecanceling.databinding.FragmentHistoryBinding
import kr.ac.gachon.sw.safenoisecanceling.models.Sound
import kr.ac.gachon.sw.safenoisecanceling.ui.history.adapter.HistoryRVAdapter
import java.util.ArrayList

class HistoryFragment : BaseFragment<FragmentHistoryBinding>(FragmentHistoryBinding::bind, R.layout.fragment_history), HistoryContract.View {
    private lateinit var historyPresenter: HistoryPresenter
    private lateinit var historyRVAdapter: HistoryRVAdapter
    private var lastIdx = 0

    private val adapterScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = viewBinding.rvHistory.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount
            val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

            if (lastVisible >= totalItemCount - 1) {
                // 스크롤할 때 바로 Update하는 것을 막아서 Error를 방지함
                recyclerView.post {
                    historyRVAdapter.addItems(historyPresenter.getSoundListByPaging(lastIdx, 15))
                    lastIdx += 15
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyPresenter.createView(this)
        initAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        historyPresenter.destroyView()
    }

    override fun initPresenter() {
        historyPresenter = HistoryPresenter()
    }

    private fun initAdapter() {
        val soundList = historyPresenter.getSoundListByPaging(lastIdx, 15)
        val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
        historyRVAdapter = HistoryRVAdapter(requireContext(), soundList as ArrayList<Sound>)
        viewBinding.rvHistory.adapter = historyRVAdapter
        viewBinding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.rvHistory.addOnScrollListener(adapterScrollListener)
        viewBinding.rvHistory.addItemDecoration(divider)
    }
}