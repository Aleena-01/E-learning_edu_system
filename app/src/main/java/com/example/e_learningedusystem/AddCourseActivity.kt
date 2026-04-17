package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AddCourseActivity : AppCompatActivity() {

    private var teacherId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        teacherId = intent.getIntExtra("TEACHER_ID", -1)

        val etTitle = findViewById<EditText>(R.id.etCourseTitle)
        val etDesc = findViewById<EditText>(R.id.etCourseDesc)
        val etCat = findViewById<EditText>(R.id.etCourseCategory)
        val etUrl = findViewById<EditText>(R.id.etVideoUrl)
        val btnSave = findViewById<Button>(R.id.btnSaveCourse)

        btnSave.setOnClickListener { v ->
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)

                val title = etTitle.text.toString()
                val desc = etDesc.text.toString()
                val cat = etCat.text.toString()
                val url = etUrl.text.toString()

                if (title.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(this, "Title and Description are required", Toast.LENGTH_SHORT).show()
                } else {
                    val teacherName = AppData.getUserById(teacherId)?.name ?: "Instructor"
                    val course = Course(
                        teacherId = teacherId, 
                        title = title, 
                        description = desc, 
                        category = cat, 
                        videoUrl = url,
                        teacherName = teacherName
                    )
                    
                    // Save to persistent ArrayList storage
                    AppData.addCourse(this, course)
                    
                    showSuccessDialog()
                    
                    // Custom Broadcast still works fine
                    val intent = Intent("com.example.EDU_VERSE_COURSE_ADDED")
                    sendBroadcast(intent)
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Course published successfully!")
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }
}