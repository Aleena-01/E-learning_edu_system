package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProfileSetupActivity : AppCompatActivity() {

    private var role: String? = null
    private var name: String? = null
    private var email: String? = null
    private var pass: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Get data from SignupActivity
        name = intent.getStringExtra("EXTRA_NAME")
        email = intent.getStringExtra("EXTRA_EMAIL")
        pass = intent.getStringExtra("EXTRA_PASS")
        role = intent.getStringExtra("EXTRA_ROLE")

        val llTeacherFields = findViewById<LinearLayout>(R.id.llTeacherFields)
        val llStudentFields = findViewById<LinearLayout>(R.id.llStudentFields)
        
        val etQualification = findViewById<EditText>(R.id.etQualification)
        val etExperience = findViewById<EditText>(R.id.etExperience)
        val etExpertise = findViewById<EditText>(R.id.etExpertise)
        
        val etEducationLevel = findViewById<EditText>(R.id.etEducationLevel)
        val etInterests = findViewById<EditText>(R.id.etInterests)
        
        val etBio = findViewById<EditText>(R.id.etBio)
        val btnFinish = findViewById<Button>(R.id.btnCompleteSignup)

        if (role == "Teacher") {
            llTeacherFields.visibility = View.VISIBLE
            llStudentFields.visibility = View.GONE
        } else {
            llTeacherFields.visibility = View.GONE
            llStudentFields.visibility = View.VISIBLE
        }

        btnFinish.setOnClickListener {
            val bio = etBio.text.toString().trim()
            val qualification = etQualification.text.toString().trim()
            val experience = etExperience.text.toString().trim()
            val expertise = etExpertise.text.toString().trim()
            val educationLevel = etEducationLevel.text.toString().trim()
            val interests = etInterests.text.toString().trim()

            // Basic validation
            if (role == "Teacher") {
                if (qualification.isEmpty()) { etQualification.error = "Required"; return@setOnClickListener }
                if (experience.isEmpty()) { etExperience.error = "Required"; return@setOnClickListener }
                if (expertise.isEmpty()) { etExpertise.error = "Required"; return@setOnClickListener }
            } else {
                if (educationLevel.isEmpty()) { etEducationLevel.error = "Required"; return@setOnClickListener }
            }

            val user = User(
                name = name ?: "",
                email = email ?: "",
                password = pass ?: "",
                role = role ?: "",
                qualification = qualification,
                experience = experience,
                expertise = expertise,
                educationLevel = educationLevel,
                interests = interests,
                bio = bio
            )

            // Save to AppData
            AppData.addUser(this, user)
            
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}