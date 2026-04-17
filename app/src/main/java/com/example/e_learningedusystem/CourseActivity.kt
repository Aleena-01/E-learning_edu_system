package com.example.e_learningedusystem

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class CourseActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var btnMarkComplete: Button
    private var progress = 0
    private var teacherId: Int = -1
    private var courseId: Int = -1
    private var isTeacher: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCourse)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        courseId = intent.getIntExtra("courseId", -1)
        val courseName = intent.getStringExtra("courseName")
        val teacherName = intent.getStringExtra("teacherName")
        teacherId = intent.getIntExtra("teacherId", -1)
        isTeacher = intent.getBooleanExtra("isTeacher", false)

        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        val tvTeacherInfo = findViewById<TextView>(R.id.tvTeacherInfo)
        
        tvTitle.text = courseName ?: "Course Details"
        tvTeacherInfo.text = "By: ${teacherName ?: "Instructor"}"

        tvTeacherInfo.setOnClickListener {
            if (teacherId != -1) {
                showTeacherProfile(teacherId)
            }
        }

        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        btnMarkComplete = findViewById(R.id.btnMarkComplete)

        if (isTeacher) {
            setupTeacherView()
        } else {
            setupStudentView()
        }
    }

    private fun setupTeacherView() {
        btnMarkComplete.visibility = View.GONE
        tvProgressText.visibility = View.GONE
        progressBar.visibility = View.GONE
        
        val detailsLayout = findViewById<TextView>(R.id.tvCourseDetails).parent as LinearLayout
        
        val teacherActionsCard = MaterialCardView(this).apply {
            radius = 32f
            elevation = 8f
            setContentPadding(40, 40, 40, 40)
        }
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.match_parent,
            LinearLayout.LayoutParams.wrap_content
        )
        params.setMargins(0, 48, 0, 0)
        teacherActionsCard.layoutParams = params

        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val actionTitle = TextView(this).apply {
            text = "Instructor Tools"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(getColor(R.color.primary_blue))
        }

        val btnViewStudents = Button(this).apply {
            text = "View Enrolled Students"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.match_parent,
                140
            ).apply { setMargins(0, 30, 0, 0) }
            setBackgroundResource(R.drawable.rounded_button)
            setTextColor(getColor(R.color.white))
            setOnClickListener { showEnrolledStudents() }
        }

        actionLayout.addView(actionTitle)
        actionLayout.addView(btnViewStudents)
        teacherActionsCard.addView(actionLayout)
        detailsLayout.addView(teacherActionsCard)
    }

    private fun showEnrolledStudents() {
        // Find students enrolled in this course from AppData
        val studentIds = AppData.enrollments.filter { it.first == courseId }.map { it.second }
        val students = AppData.users.filter { it.id in studentIds }

        if (students.isEmpty()) {
            Toast.makeText(this, "No students enrolled yet", Toast.LENGTH_SHORT).show()
            return
        }

        val studentNames = students.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Course Students")
            .setItems(studentNames, null)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupStudentView() {
        // Enroll student automatically if not already
        val studentId = intent.getIntExtra("studentId", -1)
        if (studentId != -1) {
            AppData.enrollStudent(this, courseId, studentId)
        }

        btnMarkComplete.setOnClickListener {
            if (progress < 100) {
                progress += 20
                animateProgressBar(progress)
                tvProgressText.text = "$progress%"
                
                if (progress == 100) {
                    btnMarkComplete.text = "Course Completed"
                    btnMarkComplete.isEnabled = false
                    Toast.makeText(this, "Great job! Course complete.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showTeacherProfile(id: Int) {
        val teacher = AppData.getUserById(id)
        if (teacher != null) {
            AlertDialog.Builder(this)
                .setTitle("About Instructor")
                .setMessage("Name: ${teacher.name}\nBio: ${teacher.bio}\nSkills: ${teacher.skills}")
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun animateProgressBar(targetProgress: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}