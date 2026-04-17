package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: CourseAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Student Dashboard"

        findViewById<TextView>(R.id.tvWelcomeName).text = "Hello, $userName!"

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderName).text = userName
        header.findViewById<TextView>(R.id.tvHeaderEmail).text = "Student"

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadCourses()
    }

    private fun loadCourses() {
        // Use AppData instead of DB
        val courses = AppData.courses
        adapter = CourseAdapter(courses.toMutableList(), object : CourseAdapter.OnCourseClickListener {
            override fun onEdit(course: Course) {}
            override fun onDelete(course: Course, position: Int) {}
            override fun onItemClick(course: Course) {
                val intent = Intent(this@StudentDashboardActivity, CourseActivity::class.java)
                intent.putExtra("courseId", course.id)
                intent.putExtra("courseName", course.title)
                intent.putExtra("teacherName", course.teacherName)
                intent.putExtra("teacherId", course.teacherId)
                intent.putExtra("studentId", userId)
                startActivity(intent)
            }
        })
        findViewById<RecyclerView>(R.id.recyclerViewCourses).adapter = adapter
    }
}