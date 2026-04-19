package com.example.e_learningedusystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmissionAdapter(
    private val submissions: List<AssignmentSubmission>,
    private val onGradeClick: (AssignmentSubmission) -> Unit
) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvSubmissionText: TextView = view.findViewById(R.id.tvSubmissionText)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvMarks: TextView = view.findViewById(R.id.tvMarks)
        val btnGrade: Button = view.findViewById(R.id.btnGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = submissions[position]
        holder.tvStudentName.text = sub.studentName
        holder.tvSubmissionText.text = sub.submissionText
        holder.tvFileName.text = if (sub.fileUri.isNotEmpty()) "File attached" else "No file"
        
        holder.tvMarks.text = if (sub.marks == -1) "Marks: Not Graded" else "Marks: ${sub.marks}/${sub.totalMarks}"
        
        holder.btnGrade.setOnClickListener { onGradeClick(sub) }
    }

    override fun getItemCount() = submissions.size
}
