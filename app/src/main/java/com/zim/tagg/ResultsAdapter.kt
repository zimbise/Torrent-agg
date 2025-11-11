package com.zim.tagg

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(
    private val items: MutableList<ResultItem>,
    private val onAction: (ResultItem) -> Unit
) : RecyclerView.Adapter<ResultsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val meta: TextView = v.findViewById(R.id.meta)
        val action: Button = v.findViewById(R.id.actionBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.meta.text = "${item.providerId} • ${item.size ?: "?"} • S:${item.seeders ?: 0}/L:${item.leechers ?: 0}"
        holder.action.setOnClickListener { onAction(item) }
    }

    override fun getItemCount() = items.size

    fun setData(newItems: List<ResultItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
