package com.example.e_learningedusystem

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        
        val btnSubmit = findViewById<Button>(R.id.btnSubmitQuiz)
        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Quiz Submitted Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}