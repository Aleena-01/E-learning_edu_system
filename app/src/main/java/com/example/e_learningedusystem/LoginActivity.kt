package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPass = findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignup = findViewById<TextView>(R.id.tvSignupLink)

        btnLogin.setOnClickListener { v ->
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)

                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()

                if (!validateEmail(email)) {
                    etEmail.error = "Invalid email format"
                    return@withEndAction
                }

                if (pass.isEmpty()) {
                    etPass.error = "Password required"
                    return@withEndAction
                }

                // Check in-memory persistent storage
                val user = AppData.checkUser(email)
                if (user != null) {
                    Toast.makeText(this, "Welcome ${user.name}!", Toast.LENGTH_SHORT).show()
                    
                    val intent = if (user.role == "Teacher") {
                        Intent(this, TeacherDashboardActivity::class.java)
                    } else {
                        Intent(this, StudentDashboardActivity::class.java)
                    }
                    intent.putExtra("USER_ID", user.id)
                    intent.putExtra("USER_NAME", user.name)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User not found or invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvForgotPass.setOnClickListener {
            Toast.makeText(this, "Password reset simulation: Default is '123456'", Toast.LENGTH_SHORT).show()
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}