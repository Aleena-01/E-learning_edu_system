package com.example.e_learningedusystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private val courseList: MutableList<Course>,
    private val listener: OnCourseClickListener
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnCourseClickListener {
        fun onEdit(course: Course)
        fun onDelete(course: Course, position: Int)
        fun onItemClick(course: Course)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.tvName.text = course.title
        holder.tvDesc.text = course.description

        // Item animation
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.slide_in_left)
        holder.itemView.startAnimation(animation)

        holder.itemView.setOnClickListener { v ->
            // Card click zoom effect
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
                listener.onItemClick(course)
            }
        }

        holder.btnEdit.setOnClickListener { listener.onEdit(course) }
        holder.btnDelete.setOnClickListener { listener.onDelete(course, position) }
    }

    override fun getItemCount(): Int = courseList.size

    fun removeAt(position: Int) {
        courseList.removeAt(position)
        notifyItemRemoved(position)
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCourseName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvCourseDesc)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }
}