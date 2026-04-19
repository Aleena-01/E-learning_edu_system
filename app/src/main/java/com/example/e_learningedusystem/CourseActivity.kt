package com.example.e_learningedusystem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
        
        toolbar.setNavigationOnClickListener { 
            onBackPressedDispatcher.onBackPressed()
        }
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
        
        // MCQ Fields
        val llMcq = dialogView.findViewById<LinearLayout>(R.id.llMcqSection)
        val etQ = dialogView.findViewById<EditText>(R.id.etQuizQuestion)
        val etA = dialogView.findViewById<EditText>(R.id.etQuizOptA)
        val etB = dialogView.findViewById<EditText>(R.id.etQuizOptB)
        val etC = dialogView.findViewById<EditText>(R.id.etQuizOptC)
        val etD = dialogView.findViewById<EditText>(R.id.etQuizOptD)
        val etAns = dialogView.findViewById<EditText>(R.id.etQuizCorrectIndex)

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
                
                if (selectedType == "Video") etLink.hint = "Enter Video URL (YouTube/MP4)"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        itemToEdit?.let {
            etTitle.setText(it.title)
            etWeekDay.setText(it.weekOrDay)
            etLink.setText(it.contentUri)
            etDesc.setText(it.description)
            spType.setSelection(types.indexOf(it.type))
            
            if (it.type == "Quiz") {
                val quiz = AppData.quizzes.find { q -> q.outlineItemId == it.id }
                quiz?.let { q ->
                    etQ.setText(q.question)
                    etA.setText(q.options[0])
                    etB.setText(q.options[1])
                    etC.setText(q.options[2])
                    etD.setText(q.options[3])
                    etAns.setText(q.correctOptionIndex.toString())
                }
            }
        }

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }
            filePickerLauncher.launch(intent)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (itemToEdit == null) "Add Content" else "Edit Content")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val weekDay = etWeekDay.text.toString().trim()
            val type = spType.selectedItem.toString()
            val linkText = etLink.text.toString().trim()
            val finalUri = if (tempFileUri.isNotEmpty() && tempFileUri.startsWith("content://")) tempFileUri else linkText

            if (title.isEmpty() || weekDay.isEmpty()) { Toast.makeText(this, "Title and Week required", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            if (type == "Video" && tempFileUri.isEmpty()) {
                if (!Patterns.WEB_URL.matcher(linkText).matches() && !linkText.endsWith(".mp4")) {
                    etLink.error = "Invalid Video URL"; return@setOnClickListener
                }
            }

            var newItemId = -1
            if (itemToEdit == null) {
                newItemId = AppData.addOutlineItem(this, OutlineItem(courseId = courseId, title = title, type = type, weekOrDay = weekDay, contentUri = finalUri, description = etDesc.text.toString()))
            } else {
                val index = AppData.outlines.indexOf(itemToEdit)
                if (index != -1) {
                    AppData.outlines[index] = itemToEdit.copy(title = title, type = type, weekOrDay = weekDay, contentUri = finalUri, description = etDesc.text.toString())
                    AppData.saveData(this)
                    newItemId = itemToEdit.id
                }
            }

            // Save MCQ if Quiz
            if (type == "Quiz" && newItemId != -1) {
                val qText = etQ.text.toString().trim()
                if (qText.isNotEmpty()) {
                    val options = listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString())
                    val ansIdx = etAns.text.toString().toIntOrNull() ?: 0
                    
                    // Remove old quiz question for this item and add new one
                    AppData.quizzes.removeAll { it.outlineItemId == newItemId }
                    AppData.addQuiz(this, Quiz(courseId = courseId, outlineItemId = newItemId, question = qText, options = options, correctOptionIndex = ansIdx))
                }
            }

            loadOutlineData()
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }
    }

    private fun handleOutlineItemClick(item: OutlineItem) {
        when (item.type) {
            "Quiz" -> startActivity(Intent(this, QuizActivity::class.java).apply { putExtra("courseId", courseId); putExtra("outlineItemId", item.id); putExtra("userId", currentUserId) })
            "Assignment" -> startActivity(Intent(this, AssignmentActivity::class.java).apply { putExtra("outlineItemId", item.id); putExtra("isTeacher", isTeacher); putExtra("studentId", currentUserId) })
            else -> {
                if (item.contentUri.startsWith("http")) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.contentUri)))
                else if (item.contentUri.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(Uri.parse(item.contentUri), if (item.type == "Video") "video/*" else "*/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        startActivity(intent)
                    } catch (e: Exception) { Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show() }
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

    private fun generateCertificate() {
        val course = AppData.courses.find { it.id == courseId }
        val user = AppData.getUserById(currentUserId)
        val msg = "Certificate of Completion\\n\\n${user?.name} has completed ${course?.title}"
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, msg) }, "Share Certificate"))
    }

    // Inner Adapter for Enrolled Students (Teacher View)
    private inner class StudentProgressAdapter(val studentIds: List<Int>, val outlineItems: List<OutlineItem>) : RecyclerView.Adapter<StudentProgressAdapter.StudentViewHolder>() {
        
        inner class StudentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tvStudentName)
            val pb = v.findViewById<ProgressBar>(R.id.pbStudentProgress)
            val tvPercent = v.findViewById<TextView>(R.id.tvStudentProgressPercent)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StudentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_student_progress, parent, false))
        
        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            val student = AppData.getUserById(studentIds[position])
            holder.tvName.text = student?.name ?: "Unknown Student"
            val completedCount = outlineItems.count { AppData.isItemComplete(it.id, studentIds[position]) }
            val progress = if (outlineItems.isEmpty()) 0 else (completedCount * 100 / outlineItems.size)
            holder.pb.progress = progress
            holder.tvPercent.text = "$progress%"
        }

        override fun getItemCount() = studentIds.size
    }
}
