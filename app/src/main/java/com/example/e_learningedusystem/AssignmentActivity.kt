package com.example.e_learningedusystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AssignmentActivity : AppCompatActivity() {

    private var outlineItemId: Int = -1
    private var isTeacher: Boolean = false
    private var studentId: Int = -1
    private var selectedFileUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        outlineItemId = intent.getIntExtra("outlineItemId", -1)
        isTeacher = intent.getBooleanExtra("isTeacher", false)
        studentId = intent.getIntExtra("studentId", -1)

        val item = AppData.outlines.find { it.id == outlineItemId } ?: return
        
        setupToolbar(item.title)
        findViewById<TextView>(R.id.tvAsgnTitle).text = item.title

        if (isTeacher) {
            setupTeacherView()
        } else {
            setupStudentView()
        }
    }

    private fun setupToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbarAssignment)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupStudentView() {
        val layout = findViewById<LinearLayout>(R.id.llStudentSubmit)
        layout.visibility = View.VISIBLE

        val etText = findViewById<EditText>(R.id.etSubmissionText)
        val tvStatus = findViewById<TextView>(R.id.tvSubmissionStatus)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAsgn)
        val btnUpload = findViewById<Button>(R.id.btnUploadFile)
        val tvFile = findViewById<TextView>(R.id.tvFileName)

        // Check if already submitted
        val existingSub = AppData.submissions.find { it.outlineItemId == outlineItemId && it.studentId == studentId }
        if (existingSub != null) {
            etText.setText(existingSub.submissionText)
            etText.isEnabled = false
            btnSubmit.visibility = View.GONE
            btnUpload.visibility = View.GONE
            tvStatus.text = if (existingSub.marks == -1) "Status: Submitted (Pending Grade)" 
                            else "Status: Graded (${existingSub.marks}/${existingSub.totalMarks})"
            if (existingSub.feedback.isNotEmpty()) {
                tvStatus.text = "${tvStatus.text}\nFeedback: ${existingSub.feedback}"
            }
        }

        btnUpload.setOnClickListener {
            // Simple mock file picker
            selectedFileUri = "content://uploads/assignment_${outlineItemId}.pdf"
            tvFile.text = "File: assignment.pdf (Attached)"
        }

        btnSubmit.setOnClickListener {
            val text = etText.text.toString().trim()
            if (text.isEmpty() && selectedFileUri.isEmpty()) {
                Toast.makeText(this, "Please provide a response or attach a file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val studentName = AppData.getUserById(studentId)?.name ?: "Student"
            val submission = AssignmentSubmission(
                outlineItemId = outlineItemId,
                studentId = studentId,
                studentName = studentName,
                submissionText = text,
                fileUri = selectedFileUri
            )
            AppData.addSubmission(this, submission)
            Toast.makeText(this, "Assignment Submitted!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupTeacherView() {
        val layout = findViewById<LinearLayout>(R.id.llTeacherView)
        layout.visibility = View.VISIBLE

        val rvSubmissions = findViewById<RecyclerView>(R.id.rvSubmissions)
        rvSubmissions.layoutManager = LinearLayoutManager(this)

        val subs = AppData.submissions.filter { it.outlineItemId == outlineItemId }
        if (subs.isEmpty()) {
            Toast.makeText(this, "No submissions yet", Toast.LENGTH_SHORT).show()
        }

        val adapter = SubmissionAdapter(subs) { submission ->
            showGradeDialog(submission)
        }
        rvSubmissions.adapter = adapter
    }

    private fun showGradeDialog(sub: AssignmentSubmission) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grade_assignment, null)
        val etMarks = dialogView.findViewById<EditText>(R.id.etMarks)
        val etFeedback = dialogView.findViewById<EditText>(R.id.etFeedback)

        AlertDialog.Builder(this)
            .setTitle("Grade ${sub.studentName}'s Work")
            .setView(dialogView)
            .setPositiveButton("Submit Grade") { _, _ ->
                val marks = etMarks.text.toString().toIntOrNull() ?: 0
                val feedback = etFeedback.text.toString().trim()
                
                AppData.updateSubmissionMarks(this, sub.id, marks, feedback)
                Toast.makeText(this, "Graded successfully", Toast.LENGTH_SHORT).show()
                setupTeacherView() // Refresh
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
