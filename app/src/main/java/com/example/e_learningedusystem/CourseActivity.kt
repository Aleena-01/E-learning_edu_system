package com.example.e_learningedusystem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CourseActivity : AppCompatActivity() {

    private var courseId: Int = -1
    private var isTeacher: Boolean = false
    private var currentUserId: Int = -1
    private lateinit var adapter: OutlineAdapter
    private val outlineItems = mutableListOf<OutlineItem>()
    
    private var tempFileUri: String = ""
    private var tvSelectedFileRef: TextView? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                tempFileUri = uri.toString()
                tvSelectedFileRef?.text = "Selected: ${getFileName(uri)}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        courseId = intent.getIntExtra("courseId", -1)
        isTeacher = intent.getBooleanExtra("isTeacher", false)
        currentUserId = intent.getIntExtra("studentId", -1)
        if (currentUserId == -1) currentUserId = intent.getIntExtra("teacherId", -1)

        val course = AppData.courses.find { it.id == courseId } ?: return

        setupToolbar(course.title)
        setupCourseHeader(course)
        setupOutlineRecyclerView()

        val btnEnroll = findViewById<Button>(R.id.btnMarkComplete) // Reusing button for Enroll/Cert

        if (isTeacher) {
            findViewById<LinearLayout>(R.id.llTeacherTools).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.llStudentProgress).visibility = View.GONE
            findViewById<Button>(R.id.btnAddOutlineItem).setOnClickListener { showAddOutlineDialog() }
            btnEnroll.visibility = View.GONE
        } else {
            val isEnrolled = AppData.enrollments.contains(Pair(courseId, currentUserId))
            if (isEnrolled) {
                setupStudentProgress()
            } else {
                findViewById<LinearLayout>(R.id.llStudentProgress).visibility = View.GONE
                btnEnroll.visibility = View.VISIBLE
                btnEnroll.text = "Enroll in Course"
                btnEnroll.setOnClickListener {
                    AppData.enrollStudent(this, courseId, currentUserId)
                    Toast.makeText(this, "Enrolled Successfully!", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            }
        }
    }

    private fun setupToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbarCourse)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCourseHeader(course: Course) {
        findViewById<TextView>(R.id.tvCourseTitle).text = course.title
        findViewById<TextView>(R.id.tvTeacherInfo).text = "By: ${course.teacherName}"
        findViewById<TextView>(R.id.tvCourseCategory).text = course.category
        findViewById<TextView>(R.id.tvCourseDuration).text = course.duration
        findViewById<TextView>(R.id.tvCourseDetails).text = course.description
    }

    private fun setupOutlineRecyclerView() {
        val rvOutline = findViewById<RecyclerView>(R.id.rvOutline)
        rvOutline.layoutManager = LinearLayoutManager(this)
        loadOutlineData()
        adapter = OutlineAdapter(outlineItems, isTeacher, currentUserId, object : OutlineAdapter.OnItemClickListener {
            override fun onItemClick(item: OutlineItem) { handleOutlineItemClick(item) }
            override fun onEdit(item: OutlineItem) { showAddOutlineDialog(item) }
            override fun onDelete(item: OutlineItem, position: Int) {
                AlertDialog.Builder(this@CourseActivity)
                    .setTitle("Delete Content")
                    .setMessage("Remove this item?")
                    .setPositiveButton("Delete") { _, _ ->
                        AppData.outlines.remove(item)
                        AppData.saveData(this@CourseActivity)
                        loadOutlineData()
                        adapter.notifyDataSetChanged()
                    }.setNegativeButton("Cancel", null).show()
            }
            override fun onMarkComplete(item: OutlineItem) {
                AppData.markItemComplete(this@CourseActivity, item.id, currentUserId)
                loadOutlineData()
                adapter.notifyDataSetChanged()
                setupStudentProgress() // Update progress bar
            }
        })
        rvOutline.adapter = adapter
    }

    private fun loadOutlineData() {
        outlineItems.clear()
        outlineItems.addAll(AppData.outlines.filter { it.courseId == courseId })
    }

    private fun setupStudentProgress() {
        findViewById<LinearLayout>(R.id.llStudentProgress).visibility = View.VISIBLE
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgressText)
        val btnCert = findViewById<Button>(R.id.btnMarkComplete)

        // Progress based on "completions" list instead of submissions
        val completedCount = outlineItems.count { AppData.isItemComplete(it.id, currentUserId) }
        val progress = if (outlineItems.isEmpty()) 0 else (completedCount * 100 / outlineItems.size)
        
        pb.progress = progress
        tvProgress.text = "$progress%"
        if (progress == 100) {
            btnCert.visibility = View.VISIBLE
            btnCert.text = "Generate Certificate"
            btnCert.setOnClickListener { generateCertificate() }
        } else {
            btnCert.visibility = View.GONE
        }
    }

    private fun showAddOutlineDialog(itemToEdit: OutlineItem? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_outline, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etItemTitle)
        val etWeekDay = dialogView.findViewById<EditText>(R.id.etWeekDay)
        val etLink = dialogView.findViewById<EditText>(R.id.etContentLink)
        val etDesc = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnUpload = dialogView.findViewById<Button>(R.id.btnUpload)
        val tvSelectedFile = dialogView.findViewById<TextView>(R.id.tvSelectedFile)
        val spType = dialogView.findViewById<Spinner>(R.id.spItemType)
        
        tvSelectedFileRef = tvSelectedFile
        tempFileUri = itemToEdit?.contentUri ?: ""

        val types = arrayOf("Video", "Document", "Assignment", "Quiz")
        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = types[position]
                etLink.visibility = if (selectedType == "Video" || selectedType == "Document") View.VISIBLE else View.GONE
                etDesc.visibility = if (selectedType == "Assignment") View.VISIBLE else View.GONE
                btnUpload.visibility = if (selectedType != "Quiz") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        itemToEdit?.let {
            etTitle.setText(it.title)
            etWeekDay.setText(it.weekOrDay)
            etLink.setText(it.contentUri)
            etDesc.setText(it.description)
            spType.setSelection(types.indexOf(it.type))
            if (it.contentUri.startsWith("content://")) tvSelectedFile.text = "File Attached"
        }

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(this)
            .setTitle(if (itemToEdit == null) "Add Content" else "Edit Content")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val weekDay = etWeekDay.text.toString().trim()
                val type = spType.selectedItem.toString()
                val finalUri = if (tempFileUri.isNotEmpty()) tempFileUri else etLink.text.toString().trim()

                if (title.isEmpty() || weekDay.isEmpty()) {
                    Toast.makeText(this, "Title and Week required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (itemToEdit == null) {
                    val newItem = OutlineItem(courseId = courseId, title = title, type = type, weekOrDay = weekDay, contentUri = finalUri, description = etDesc.text.toString())
                    AppData.addOutlineItem(this, newItem)
                } else {
                    val index = AppData.outlines.indexOf(itemToEdit)
                    if (index != -1) {
                        AppData.outlines[index] = itemToEdit.copy(title = title, type = type, weekOrDay = weekDay, contentUri = finalUri, description = etDesc.text.toString())
                        AppData.saveData(this)
                    }
                }
                loadOutlineData()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun handleOutlineItemClick(item: OutlineItem) {
        when (item.type) {
            "Quiz" -> {
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("courseId", courseId)
                intent.putExtra("outlineItemId", item.id)
                intent.putExtra("userId", currentUserId)
                startActivity(intent)
            }
            "Assignment" -> {
                val intent = Intent(this, AssignmentActivity::class.java)
                intent.putExtra("outlineItemId", item.id)
                intent.putExtra("isTeacher", isTeacher)
                intent.putExtra("studentId", currentUserId)
                startActivity(intent)
            }
            else -> {
                if (item.contentUri.startsWith("http")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.contentUri)))
                } else if (item.contentUri.isNotEmpty()) {
                    Toast.makeText(this, "Opening File: ${item.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { if (it.moveToFirst()) result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) }
        }
        if (result == null) result = uri.path?.let { it.substring(it.lastIndexOf('/') + 1) }
        return result ?: "Unknown"
    }

    private fun generateCertificate() {
        val course = AppData.courses.find { it.id == courseId }
        val user = AppData.getUserById(currentUserId)
        val msg = "Certificate of Completion\n\n${user?.name} has completed ${course?.title}\nInstructor: ${course?.teacherName}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
    }
}
