package com.example.e_learningedusystem

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.navigation.NavigationView

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var availableAdapter: CourseAdapter
    private lateinit var enrolledAdapter: CourseAdapter
    
    private var userId: Int = -1
    private var allCourses = mutableListOf<Course>()
    private var availableCourses = mutableListOf<Course>()
    private var enrolledCourses = mutableListOf<Course>()
    private var selectedCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME")

        setupToolbar()
        setupDrawer(userName)
        setupRecyclerViews()
        setupSearchAndFilters()

        findViewById<TextView>(R.id.tvWelcomeName).text = "Hello, $userName!"
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Student Dashboard"
    }

    private fun setupDrawer(userName: String?) {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderName).text = userName
        header.findViewById<TextView>(R.id.tvHeaderEmail).text = "Student"

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerViews() {
        val rvAvailable = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        val rvEnrolled = findViewById<RecyclerView>(R.id.rvEnrolledCourses)
        
        rvAvailable.layoutManager = LinearLayoutManager(this)
        rvEnrolled.layoutManager = LinearLayoutManager(this)
        
        availableAdapter = CourseAdapter(availableCourses, object : CourseAdapter.OnCourseClickListener {
            override fun onEdit(course: Course) {}
            override fun onDelete(course: Course, position: Int) {}
            override fun onItemClick(course: Course) { openCourse(course) }
        })
        
        enrolledAdapter = CourseAdapter(enrolledCourses, object : CourseAdapter.OnCourseClickListener {
            override fun onEdit(course: Course) {}
            override fun onDelete(course: Course, position: Int) {}
            override fun onItemClick(course: Course) { openCourse(course) }
        })

        rvAvailable.adapter = availableAdapter
        rvEnrolled.adapter = enrolledAdapter
        
        refreshData()
    }

    private fun openCourse(course: Course) {
        val intent = Intent(this, CourseActivity::class.java)
        intent.putExtra("courseId", course.id)
        intent.putExtra("courseName", course.title)
        intent.putExtra("teacherName", course.teacherName)
        intent.putExtra("studentId", userId)
        startActivity(intent)
    }

    private fun refreshData() {
        allCourses = AppData.courses.toMutableList()
        val enrolledIds = AppData.enrollments.filter { it.second == userId }.map { it.first }
        
        enrolledCourses.clear()
        enrolledCourses.addAll(allCourses.filter { it.id in enrolledIds })
        
        availableCourses.clear()
        availableCourses.addAll(allCourses.filter { it.id !in enrolledIds })

        val tvEnrolledHeader = findViewById<TextView>(R.id.tvEnrolledHeader)
        val rvEnrolled = findViewById<RecyclerView>(R.id.rvEnrolledCourses)
        
        if (enrolledCourses.isEmpty()) {
            tvEnrolledHeader.visibility = View.GONE
            rvEnrolled.visibility = View.GONE
        } else {
            tvEnrolledHeader.visibility = View.VISIBLE
            rvEnrolled.visibility = View.VISIBLE
        }

        filterCourses(findViewById<EditText>(R.id.etSearch).text.toString())
    }

    private fun setupSearchAndFilters() {
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategories)

        addCategoryChip("All", true)
        allCourses.map { it.category }.distinct().forEach { addCategoryChip(it) }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterCourses(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val chip = group.findViewById<Chip>(checkedId ?: -1)
            selectedCategory = chip?.text?.toString() ?: "All"
            filterCourses(etSearch.text.toString())
        }
    }

    private fun addCategoryChip(category: String, isChecked: Boolean = false) {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategories)
        val chip = Chip(this)
        chip.text = category
        chip.isCheckable = true
        chip.isChecked = isChecked
        chipGroup.addView(chip)
    }

    private fun filterCourses(query: String) {
        val enrolledIds = AppData.enrollments.filter { it.second == userId }.map { it.first }
        
        val filteredAvailable = allCourses.filter { course ->
            course.id !in enrolledIds && 
            (course.title.contains(query, ignoreCase = true) || course.description.contains(query, ignoreCase = true)) &&
            (selectedCategory == "All" || course.category == selectedCategory)
        }
        
        availableCourses.clear()
        availableCourses.addAll(filteredAvailable)
        availableAdapter.notifyDataSetChanged()

        findViewById<TextView>(R.id.tvNoResults).visibility = if (availableCourses.isEmpty() && enrolledCourses.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }
}