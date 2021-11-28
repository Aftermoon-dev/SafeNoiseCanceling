package kr.ac.gachon.sw.safenoisecanceling.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import kr.ac.gachon.sw.safenoisecanceling.databinding.DialogLoadingBinding

class LoadingDialog(context: Context): Dialog(context) {
    private val viewBinding: DialogLoadingBinding

    init {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}