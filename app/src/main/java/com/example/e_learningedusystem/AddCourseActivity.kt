package com.example.e_learningedusystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCourseActivity : AppCompatActivity() {

    private var teacherId: Int = -1
    private var courseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        teacherId = intent.getIntExtra("TEACHER_ID", -1)
        courseId = intent.getIntExtra("COURSE_ID", -1)

        val tvTitle = findViewById<TextView>(R.id.tvAddCourseTitle)
        val etTitle = findViewById<EditText>(R.id.etCourseTitle)
        val etDesc = findViewById<EditText>(R.id.etCourseDesc)
        val etCategory = findViewById<EditText>(R.id.etCourseCategory)
        val etDuration = findViewById<EditText>(R.id.etCourseDuration)
        val btnSave = findViewById<Button>(R.id.btnSaveCourse)

        // If editing, load existing data
        if (courseId != -1) {
            tvTitle.text = "Edit Course"
            val course = AppData.courses.find { it.id == courseId }
            course?.let {
                etTitle.setText(it.title)
                etDesc.setText(it.description)
                etCategory.setText(it.category)
                etDuration.setText(it.duration)
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val cat = etCategory.text.toString().trim()
            val dur = etDuration.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || cat.isEmpty() || dur.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (courseId == -1) {
                // Add New
                val teacherName = AppData.getUserById(teacherId)?.name ?: "Unknown"
                val newCourse = Course(
                    teacherId = teacherId,
                    title = title,
                    description = desc,
                    category = cat,
                    duration = dur,
                    teacherName = teacherName
                )
                AppData.addCourse(this, newCourse)
                Toast.makeText(this, "Course Created!", Toast.LENGTH_SHORT).show()
            } else {
                // Update Existing
                val updatedCourse = AppData.courses.find { it.id == courseId }?.copy(
                    title = title,
                    description = desc,
                    category = cat,
                    duration = dur
                )
                updatedCourse?.let { AppData.updateCourse(this, it) }
                Toast.makeText(this, "Course Updated!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}