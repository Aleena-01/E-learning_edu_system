package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etName = findViewById<EditText>(R.id.etSignupName)
        val etEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etPass = findViewById<EditText>(R.id.etSignupPass)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnNext = findViewById<Button>(R.id.btnSignup)
        val tvLogin = findViewById<TextView>(R.id.tvLoginLink)

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnNext.setOnClickListener { v ->
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)

                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()
                val selectedRoleId = rgRole.checkedRadioButtonId

                if (name.isEmpty()) {
                    etName.error = "Name is required"
                    return@withEndAction
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Invalid email address"
                    return@withEndAction
                }
                if (pass.length < 6) {
                    etPass.error = "Password must be at least 6 characters"
                    return@withEndAction
                }
                if (selectedRoleId == -1) {
                    Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                    return@withEndAction
                }

                val role = findViewById<RadioButton>(selectedRoleId).text.toString()
                
                val intent = Intent(this, ProfileSetupActivity::class.java).apply {
                    putExtra("EXTRA_NAME", name)
                    putExtra("EXTRA_EMAIL", email)
                    putExtra("EXTRA_PASS", pass)
                    putExtra("EXTRA_ROLE", role)
                }
                startActivity(intent)
            }
        }
    }
}