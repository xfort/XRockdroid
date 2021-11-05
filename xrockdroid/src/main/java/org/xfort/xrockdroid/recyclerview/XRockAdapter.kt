package org.xfort.xrockdroid.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 ** Created by ZhangHuaXin on 2021/11/5.
 * RecyclerView.Adapter 封装 数据列表、ViewHolder点击
 **/
abstract class XRockAdapter<Data, VH : XRockViewHolder> : RecyclerView.Adapter<VH>(),
    XRockViewHolder.OnClickListener {
    val dateList = LinkedList<Data>()

    var onViewHolderClick: XRockViewHolder.OnClickListener? = null
    private var layoutInflater: LayoutInflater? = null

    /**
     * 设置数据
     * @param append true添加新数据到队尾;false清空老数据后填充
     */
    fun setData(data: List<Data>?, append: Boolean) {
        if (append) {
            if (!data.isNullOrEmpty()) {
                var count = dateList.size
                dateList.addAll(data)
                notifyItemRangeInserted(count, data.size)
            }
        } else {
            var count = dateList.size
            dateList.clear()
            if (data.isNullOrEmpty()) {
                notifyItemRangeRemoved(0, count)
            } else {
                dateList.addAll(data)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return dateList.size
    }

    abstract fun onCreateViewHolder(
        layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val vh = onCreateViewHolder(layoutInflater!!, parent, viewType)
        vh.onClickListener = this
        return vh
    }

    /**
     * ViewHolder 被点击
     */
    override fun onViewHolderClick(v: View?, position: Int, data: Intent?) {
        onViewHolderClick?.onViewHolderClick(v, position, data)
    }
}