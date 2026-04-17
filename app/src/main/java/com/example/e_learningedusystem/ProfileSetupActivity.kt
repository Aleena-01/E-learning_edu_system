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
        
        val etBio = findViewById<EditText>(R.id.etBio)
        val etInterests = findViewById<EditText>(R.id.etInterests)
        val etCertifications = findViewById<EditText>(R.id.etCertifications)
        val etResumeLink = findViewById<EditText>(R.id.etResumeLink)
        
        val etEducation = findViewById<EditText>(R.id.etEducation)
        val etSkills = findViewById<EditText>(R.id.etSkills)
        
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
            val interests = etInterests.text.toString().trim()
            val certs = etCertifications.text.toString().trim()
            val resume = etResumeLink.text.toString().trim()
            val edu = etEducation.text.toString().trim()
            val skills = etSkills.text.toString().trim()

            // Basic validation
            if (bio.isEmpty() && role == "Teacher") {
                etBio.error = "Bio is required"
                return@setOnClickListener
            }

            val user = User(
                name = name ?: "",
                email = email ?: "",
                role = role ?: "",
                bio = bio,
                interests = interests,
                certifications = certs,
                resume = resume,
                education = edu,
                skills = skills,
                profilePic = ""
            )

            // Save to AppData (ArrayList Persistent Storage)
            AppData.addUser(this, user)
            
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}