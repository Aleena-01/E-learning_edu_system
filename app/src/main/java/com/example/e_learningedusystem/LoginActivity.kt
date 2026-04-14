package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener { v ->
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)

                val email = etEmail.text.toString()
                val pass = etPass.text.toString()

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val user = db.checkUser(email, pass)
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
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}