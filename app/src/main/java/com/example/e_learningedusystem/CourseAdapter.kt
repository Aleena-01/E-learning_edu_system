package com.example.e_learningedusystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
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
        holder.tvTitle.text = course.title
        holder.tvTeacher.text = "by ${course.teacherName}"
        holder.tvCategory.text = course.category
        holder.tvDuration.text = course.duration

        // Item animation
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.fade_in)
        holder.itemView.startAnimation(animation)

        holder.itemView.setOnClickListener { v ->
            v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
                listener.onItemClick(course)
            }
        }

        // Logic for edit/delete visibility should be handled by caller usually, 
        // but if listener is provided and it's teacher dashboard, show them.
        // For simplicity, we'll check if ivActionEdit exists (it's gone by default in XML)
        holder.ivEdit.setOnClickListener { listener.onEdit(course) }
        holder.ivDelete.setOnClickListener { listener.onDelete(course, position) }
    }

    override fun getItemCount(): Int = courseList.size

    fun removeAt(position: Int) {
        courseList.removeAt(position)
        notifyItemRemoved(position)
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val tvTeacher: TextView = itemView.findViewById(R.id.tvTeacherName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCourseCategory)
        val tvDuration: TextView = itemView.findViewById(R.id.tvCourseDuration)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivActionEdit)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivActionDelete)
    }
}