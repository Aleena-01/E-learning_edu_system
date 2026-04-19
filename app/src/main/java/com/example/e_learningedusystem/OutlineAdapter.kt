package com.example.e_learningedusystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OutlineAdapter(
    private val items: MutableList<OutlineItem>,
    private val isTeacher: Boolean,
    private val currentUserId: Int,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<OutlineAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: OutlineItem)
        fun onEdit(item: OutlineItem)
        fun onDelete(item: OutlineItem, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeekDay: TextView = view.findViewById(R.id.tvWeekDay)
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val ivIcon: ImageView = view.findViewById(R.id.ivTypeIcon)
        val ivEdit: ImageView = view.findViewById(R.id.ivEditItem)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteItem)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_outline, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvWeekDay.text = item.weekOrDay
        holder.tvTitle.text = "${item.type}: ${item.title}"

        holder.ivIcon.setImageResource(when(item.type){
            "Video" -> android.R.drawable.ic_menu_slideshow
            "Document" -> android.R.drawable.ic_menu_agenda
            "Quiz" -> android.R.drawable.ic_menu_help
            else -> android.R.drawable.ic_menu_edit
        })

        if (isTeacher) {
            holder.ivEdit.visibility = View.VISIBLE
            holder.ivDelete.visibility = View.VISIBLE
            holder.tvStatus.visibility = View.GONE
            
            holder.ivEdit.setOnClickListener { listener.onEdit(item) }
            holder.ivDelete.setOnClickListener { listener.onDelete(item, position) }
        } else {
            holder.ivEdit.visibility = View.GONE
            holder.ivDelete.visibility = View.GONE
            
            val isComplete = AppData.isItemComplete(item.id, currentUserId)
            if (isComplete) {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = "COMPLETED"
            } else {
                holder.tvStatus.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener { listener.onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
