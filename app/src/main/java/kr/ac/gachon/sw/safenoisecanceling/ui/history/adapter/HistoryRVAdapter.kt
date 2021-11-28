package kr.ac.gachon.sw.safenoisecanceling.ui.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.ac.gachon.sw.safenoisecanceling.R
import kr.ac.gachon.sw.safenoisecanceling.databinding.ItemClassifyhistoryBinding
import kr.ac.gachon.sw.safenoisecanceling.models.Sound
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class HistoryRVAdapter(private val context: Context, private val initList: ArrayList<Sound>): RecyclerView.Adapter<HistoryRVAdapter.ViewHolder>() {
    private val historyList: ArrayList<Sound> = initList
    private lateinit var viewBinding: ItemClassifyhistoryBinding

    //2021-11-28 오후 11:45:00
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd aa hh:mm:ss", Locale.getDefault())

    class ViewHolder(binding: ItemClassifyhistoryBinding): RecyclerView.ViewHolder(binding.root) {
        val tvDate = binding.tvHistoryDate
        val tvSoundType = binding.tvHistorySoundtype
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        viewBinding = ItemClassifyhistoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val soundInfo = historyList[position]
        holder.tvDate.text = dateFormat.format(Date(soundInfo.time))

        val toPercent = round(soundInfo.score * 100)
        holder.tvSoundType.text = context.getString(R.string.history_type, soundInfo.soundType, toPercent.toString())
    }

    override fun getItemCount(): Int = historyList.size

    fun addItems(soundList: List<Sound>) {
        val lastidx = itemCount
        historyList.addAll(soundList)
        notifyItemRangeInserted(lastidx, soundList.size)
    }
}