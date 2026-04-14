package com.example.e_learningedusystem

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CourseActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var btnMarkComplete: Button
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCourse)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val courseName = intent.getStringExtra("courseName")
        val tvTitle = findViewById<TextView>(R.id.tvCourseTitle)
        tvTitle.text = courseName ?: "Course Details"

        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        btnMarkComplete = findViewById(R.id.btnMarkComplete)

        btnMarkComplete.setOnClickListener {
            if (progress < 100) {
                progress += 20
                animateProgressBar(progress)
                tvProgressText.text = "$progress%"
                
                if (progress == 100) {
                    btnMarkComplete.text = "Lesson Completed"
                    btnMarkComplete.isEnabled = false
                    Toast.makeText(this, "Congratulations! Course Completed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun animateProgressBar(targetProgress: Int) {
        val animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress)
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}