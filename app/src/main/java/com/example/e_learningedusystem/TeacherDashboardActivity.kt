package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: CourseAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)
        
        userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Teacher Dashboard"

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderName).text = userName
        header.findViewById<TextView>(R.id.tvHeaderEmail).text = "Instructor"

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadDashboardStats()
        loadCourses()

        findViewById<ExtendedFloatingActionButton>(R.id.fabAddCourse).setOnClickListener {
            val intent = Intent(this, AddCourseActivity::class.java)
            intent.putExtra("TEACHER_ID", userId)
            startActivity(intent)
        }
    }

    private fun loadDashboardStats() {
        val teacherCourses = AppData.courses.filter { it.teacherId == userId }
        val studentsCount = AppData.getEnrolledCount(userId)

        findViewById<TextView>(R.id.tvTotalCourses).text = teacherCourses.size.toString()
        findViewById<TextView>(R.id.tvTotalStudents).text = studentsCount.toString()
    }

    private fun loadCourses() {
        val teacherCourses = AppData.courses.filter { it.teacherId == userId }
        
        adapter = CourseAdapter(teacherCourses.toMutableList(), object : CourseAdapter.OnCourseClickListener {
            override fun onEdit(course: Course) {}

            override fun onDelete(course: Course, position: Int) {
                AlertDialog.Builder(this@TeacherDashboardActivity)
                    .setTitle("Delete Course")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes") { _, _ ->
                        AppData.courses.remove(course)
                        AppData.saveData(this@TeacherDashboardActivity)
                        adapter.removeAt(position)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            override fun onItemClick(course: Course) {
                val intent = Intent(this@TeacherDashboardActivity, CourseActivity::class.java)
                intent.putExtra("courseId", course.id)
                intent.putExtra("courseName", course.title)
                intent.putExtra("teacherName", course.teacherName)
                intent.putExtra("isTeacher", true)
                startActivity(intent)
            }
        })
        findViewById<RecyclerView>(R.id.recyclerViewCourses).adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadDashboardStats()
        loadCourses()
    }
}