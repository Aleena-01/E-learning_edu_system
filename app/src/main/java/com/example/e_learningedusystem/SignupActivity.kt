package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SignupActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etSignupName)
        val etEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etPass = findViewById<EditText>(R.id.etSignupPass)
        val etEdu = findViewById<EditText>(R.id.etSignupEdu)
        val etSkills = findViewById<EditText>(R.id.etSignupSkills)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        // Validation using Focus Change
        val focusListener = View.OnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            if (!hasFocus && et.text.toString().isEmpty()) {
                et.error = "This field is required"
                et.backgroundTintList = ContextCompat.getColorStateList(this, R.color.error_color)
            } else if (hasFocus) {
                et.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }
        }

        etName.onFocusChangeListener = focusListener
        etEmail.onFocusChangeListener = focusListener
        etPass.onFocusChangeListener = focusListener

        btnSignup.setOnClickListener { v ->
            // Animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)

                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val pass = etPass.text.toString()
                val edu = etEdu.text.toString()
                val skills = etSkills.text.toString()
                
                val selectedRoleId = rgRole.checkedRadioButtonId
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || selectedRoleId == -1) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@withEndAction
                }

                val role = findViewById<RadioButton>(selectedRoleId).text.toString()
                val user = User(name = name, email = email, role = role, education = edu, skills = skills)
                
                val result = db.addUser(user, pass)
                if (result != -1L) {
                    Toast.makeText(this, "Signup Successful! Please Login", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}