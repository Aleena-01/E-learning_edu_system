package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CourseAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        showToast("onCreate called")

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Student Dashboard"

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        
        // Setup Drawer Header
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderName).text = userName
        header.findViewById<TextView>(R.id.tvHeaderEmail).text = "Student Profile"

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        val courses = db.getAllCourses()
        adapter = CourseAdapter(courses.toMutableList(), object : CourseAdapter.OnCourseClickListener {
            override fun onEdit(course: Course) {} // Students can't edit
            override fun onDelete(course: Course, position: Int) {} // Students can't delete
            override fun onItemClick(course: Course) {
                val intent = Intent(this@StudentDashboardActivity, CourseActivity::class.java)
                intent.putExtra("courseName", course.title)
                startActivity(intent)
            }
        })
        recyclerView.adapter = adapter
    }

    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onStart() { super.onStart(); showToast("onStart") }
    override fun onResume() { super.onResume(); showToast("onResume") }
    override fun onPause() { super.onPause(); showToast("onPause") }
    override fun onStop() { super.onStop(); showToast("onStop") }
    override fun onDestroy() { super.onDestroy(); showToast("onDestroy") }
}