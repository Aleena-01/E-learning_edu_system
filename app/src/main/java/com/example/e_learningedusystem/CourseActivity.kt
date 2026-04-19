package com.example.e_learningedusystem

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.OutputStream

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

        val btnEnroll = findViewById<Button>(R.id.btnMarkComplete) 

        if (isTeacher) {
            findViewById<LinearLayout>(R.id.llTeacherTools).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.llStudentProgress).visibility = View.GONE
            findViewById<Button>(R.id.btnAddOutlineItem).setOnClickListener { showAddOutlineDialog() }
            btnEnroll.visibility = View.GONE
            setupEnrolledStudentsList()
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
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
                // For Video and Document, manual completion is okay if needed,
                // but we handle them automatically in onItemClick too.
                AppData.markItemComplete(this@CourseActivity, item.id, currentUserId)
                loadOutlineData()
                adapter.notifyDataSetChanged()
                setupStudentProgress()
            }
        })
        rvOutline.adapter = adapter
    }

    private fun loadOutlineData() {
        outlineItems.clear()
        outlineItems.addAll(AppData.outlines.filter { it.courseId == courseId })
    }

    override fun onResume() {
        super.onResume()
        // Refresh progress and list when returning from Quiz/Assignment
        loadOutlineData()
        adapter.notifyDataSetChanged()
        if (!isTeacher && AppData.enrollments.contains(Pair(courseId, currentUserId))) {
            setupStudentProgress()
        }
    }

    private fun setupStudentProgress() {
        findViewById<LinearLayout>(R.id.llStudentProgress).visibility = View.VISIBLE
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgressText)
        val btnCert = findViewById<Button>(R.id.btnMarkComplete)

        val completedCount = outlineItems.count { AppData.isItemComplete(it.id, currentUserId) }
        val progress = if (outlineItems.isEmpty()) 0 else (completedCount * 100 / outlineItems.size)
        
        pb.progress = progress
        tvProgress.text = "$progress%"
        if (progress == 100) {
            btnCert.visibility = View.VISIBLE
            btnCert.text = "Get Image Certificate"
            btnCert.setOnClickListener { generateImageCertificate() }
        } else {
            btnCert.visibility = View.GONE
        }
    }

    private fun generateImageCertificate() {
        val course = AppData.courses.find { it.id == courseId } ?: return
        val user = AppData.getUserById(currentUserId) ?: return

        val certView = LayoutInflater.from(this).inflate(R.layout.layout_certificate, null)
        certView.findViewById<TextView>(R.id.tvCertStudentName).text = user.name
        certView.findViewById<TextView>(R.id.tvCertCourseTitle).text = course.title

        // Measure and layout the view
        certView.measure(View.MeasureSpec.makeMeasureSpec(1600, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1200, View.MeasureSpec.EXACTLY))
        certView.layout(0, 0, certView.measuredWidth, certView.measuredHeight)

        val bitmap = Bitmap.createBitmap(certView.measuredWidth, certView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        certView.draw(canvas)

        saveBitmapToGallery(bitmap, "${user.name}_Certificate.png")
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, filename: String) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EduVerse")
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(this, "Certificate saved to Gallery!", Toast.LENGTH_LONG).show()
                
                // Share the image
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
            }
        }
    }

    private fun setupEnrolledStudentsList() {
        val section = findViewById<LinearLayout>(R.id.llTeacherEnrolledStudents)
        val rv = findViewById<RecyclerView>(R.id.rvEnrolledStudents)
        
        val enrolledStudentIds = AppData.enrollments.filter { it.first == courseId }.map { it.second }
        if (enrolledStudentIds.isNotEmpty()) {
            section.visibility = View.VISIBLE
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = StudentProgressAdapter(enrolledStudentIds, outlineItems)
        } else {
            section.visibility = View.GONE
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
        
        val llMcq = dialogView.findViewById<LinearLayout>(R.id.llMcqSection)
        val tvMcqCount = dialogView.findViewById<TextView>(R.id.tvMcqCount)
        val etQ = dialogView.findViewById<EditText>(R.id.etQuizQuestion)
        val etA = dialogView.findViewById<EditText>(R.id.etQuizOptA)
        val etB = dialogView.findViewById<EditText>(R.id.etQuizOptB)
        val etC = dialogView.findViewById<EditText>(R.id.etQuizOptC)
        val etD = dialogView.findViewById<EditText>(R.id.etQuizOptD)
        val etAns = dialogView.findViewById<EditText>(R.id.etQuizCorrectIndex)
        val btnAddMoreMcq = dialogView.findViewById<Button>(R.id.btnAddAnotherMcq)

        val currentMcqs = mutableListOf<Quiz>()
        var mcqCounter = 1

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
                llMcq.visibility = if (selectedType == "Quiz") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnAddMoreMcq.setOnClickListener {
            val qText = etQ.text.toString().trim()
            if (qText.isEmpty()) return@setOnClickListener
            val options = listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString())
            currentMcqs.add(Quiz(courseId = courseId, question = qText, options = options, correctOptionIndex = etAns.text.toString().toIntOrNull() ?: 0))
            etQ.text.clear(); etA.text.clear(); etB.text.clear(); etC.text.clear(); etD.text.clear(); etAns.text.clear()
            mcqCounter = currentMcqs.size + 1
            tvMcqCount.text = "Enter MCQ Question $mcqCounter:"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (itemToEdit == null) "Add Content" else "Edit Content")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val type = spType.selectedItem.toString()
                val finalUri = if (tempFileUri.isNotEmpty()) tempFileUri else etLink.text.toString()

                val newItemId = if (itemToEdit == null) {
                    AppData.addOutlineItem(this, OutlineItem(courseId = courseId, title = title, type = type, weekOrDay = etWeekDay.text.toString(), contentUri = finalUri, description = etDesc.text.toString()))
                } else {
                    val idx = AppData.outlines.indexOf(itemToEdit)
                    if (idx != -1) AppData.outlines[idx] = itemToEdit.copy(title = title, type = type, weekOrDay = etWeekDay.text.toString(), contentUri = finalUri, description = etDesc.text.toString())
                    AppData.saveData(this)
                    itemToEdit.id
                }

                if (type == "Quiz") {
                    if (etQ.text.isNotEmpty()) currentMcqs.add(Quiz(courseId = courseId, outlineItemId = newItemId, question = etQ.text.toString(), options = listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString()), correctOptionIndex = etAns.text.toString().toIntOrNull() ?: 0))
                    currentMcqs.forEach { AppData.addQuiz(this, it.copy(outlineItemId = newItemId)) }
                }
                loadOutlineData(); adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleOutlineItemClick(item: OutlineItem) {
        when (item.type) {
            "Quiz" -> {
                // DON'T mark as complete here. QuizActivity will handle it on finish.
                startActivity(Intent(this, QuizActivity::class.java).apply { 
                    putExtra("courseId", courseId)
                    putExtra("outlineItemId", item.id)
                    putExtra("userId", currentUserId) 
                })
            }
            "Assignment" -> {
                // DON'T mark as complete here. AssignmentActivity will handle it on submit.
                startActivity(Intent(this, AssignmentActivity::class.java).apply { 
                    putExtra("outlineItemId", item.id)
                    putExtra("isTeacher", isTeacher)
                    putExtra("studentId", currentUserId) 
                })
            }
            else -> {
                // Auto mark as complete ONLY for Video and Document for students
                if (!isTeacher) {
                    AppData.markItemComplete(this, item.id, currentUserId)
                    loadOutlineData()
                    adapter.notifyDataSetChanged()
                    setupStudentProgress()
                }

                if (item.contentUri.startsWith("http")) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.contentUri)))
                else if (item.contentUri.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(Uri.parse(item.contentUri), if (item.type == "Video") "video/*" else "*/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        startActivity(intent)
                    } catch (e: Exception) { Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { if (it.moveToFirst()) result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) }
        }
        return result ?: uri.path?.let { it.substring(it.lastIndexOf('/') + 1) } ?: "Unknown"
    }

    private inner class StudentProgressAdapter(val studentIds: List<Int>, val outlineItems: List<OutlineItem>) : RecyclerView.Adapter<StudentProgressAdapter.StudentViewHolder>() {
        inner class StudentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tvStudentName); val pb = v.findViewById<ProgressBar>(R.id.pbStudentProgress); val tvPercent = v.findViewById<TextView>(R.id.tvStudentProgressPercent)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StudentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_student_progress, parent, false))
        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            val student = AppData.getUserById(studentIds[position])
            holder.tvName.text = student?.name ?: "Unknown"
            val progress = if (outlineItems.isEmpty()) 0 else (outlineItems.count { AppData.isItemComplete(it.id, studentIds[position]) } * 100 / outlineItems.size)
            holder.pb.progress = progress; holder.tvPercent.text = "$progress%"
        }
        override fun getItemCount() = studentIds.size
    }
}
