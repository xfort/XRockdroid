package org.xfort.xrockdroid.recyclerview

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 ** Created by ZhangHuaXin on 2021/11/5.
 **/
class XRockViewHolder(vb: ViewBinding) : RecyclerView.ViewHolder(vb.root), View.OnClickListener {

    var onClickListener: XRockViewHolder.OnClickListener? = null

    override fun onClick(view: View?) {
        onClickListener?.onViewHolderClick(view, adapterPosition, createClickData(view))
    }

    fun createClickData(v: View?): Intent? {
        return null
    }

    public interface OnClickListener {
        fun onViewHolderClick(v: View?, position: Int, data: Intent?)
    }
}

