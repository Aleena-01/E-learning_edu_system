package com.example.e_learningedusystem

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class QuizActivity : AppCompatActivity() {

    private var courseId: Int = -1
    private var outlineItemId: Int = -1
    private var quizList = mutableListOf<Quiz>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var isTeacher = false
    private var currentUserId = -1

    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var btnAddMore: Button
    private lateinit var quizProgressBar: ProgressBar
    private lateinit var llTeacherControls: View
    private lateinit var llStudentControls: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        courseId = intent.getIntExtra("courseId", -1)
        outlineItemId = intent.getIntExtra("outlineItemId", -1)
        currentUserId = intent.getIntExtra("userId", -1)
        isTeacher = AppData.getUserById(currentUserId)?.role == "Teacher"

        setupToolbar()
        initViews()
        loadQuizData()
        displayQuestion()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarQuiz)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        tvQuestion = findViewById(R.id.tvQuestion)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        rgOptions = findViewById(R.id.rgOptions)
        btnNext = findViewById(R.id.btnNext)
        btnAddMore = findViewById(R.id.btnAddMoreMCQs)
        quizProgressBar = findViewById(R.id.quizProgressBar)
        llTeacherControls = findViewById(R.id.btnAddMoreMCQs) // Button itself is the teacher control here
        llStudentControls = findViewById(R.id.btnNext)
        
        if (isTeacher) {
            btnAddMore.visibility = View.VISIBLE
            btnNext.visibility = View.GONE
            btnAddMore.setOnClickListener { showAddQuestionDialog() }
            quizProgressBar.visibility = View.GONE
        } else {
            btnAddMore.visibility = View.GONE
            btnNext.visibility = View.VISIBLE
            quizProgressBar.visibility = View.VISIBLE
        }

        btnNext.setOnClickListener { handleNextClick() }
    }

    private fun loadQuizData() {
        quizList = AppData.quizzes.filter { it.outlineItemId == outlineItemId }.toMutableList()
        quizProgressBar.max = quizList.size
    }

    private fun displayQuestion() {
        if (quizList.isEmpty()) {
            tvQuestion.text = if (isTeacher) "No questions added yet. Use the button below to add questions." else "No questions available."
            tvQuestionNumber.text = "0 Questions"
            btnNext.visibility = View.GONE
            return
        }

        if (!isTeacher) btnNext.visibility = View.VISIBLE
        
        val quiz = quizList[currentQuestionIndex]
        tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${quizList.size}"
        tvQuestion.text = quiz.question
        quizProgressBar.progress = currentQuestionIndex + 1
        
        rgOptions.removeAllViews()
        quiz.options.forEachIndexed { index, option ->
            val rb = RadioButton(this)
            rb.text = option
            rb.id = index
            
            // Modern Radio Button Styling
            val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 32)
            rb.layoutParams = params
            rb.setPadding(48, 48, 48, 48)
            rb.background = ContextCompat.getDrawable(this, R.drawable.quiz_option_selector)
            rb.buttonDrawable = null // Hide default circle
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            rb.setTextColor(ContextCompat.getColor(this, R.color.text_main))
            
            rgOptions.addView(rb)
        }
        rgOptions.clearCheck()
        btnNext.text = if (currentQuestionIndex == quizList.size - 1) "Finish Quiz" else "Next Question"
    }

    private fun handleNextClick() {
        val selectedId = rgOptions.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedId == quizList[currentQuestionIndex].correctOptionIndex) score++

        currentQuestionIndex++
        if (currentQuestionIndex < quizList.size) {
            displayQuestion()
        } else {
            showResult()
        }
    }

    private fun showResult() {
        AppData.saveQuizResult(this, QuizResult(outlineItemId = outlineItemId, studentId = currentUserId, score = score, total = quizList.size))
        AppData.markItemComplete(this, outlineItemId, currentUserId)

        val resultView = LayoutInflater.from(this).inflate(R.layout.dialog_quiz_result, null)
        resultView.findViewById<TextView>(R.id.tvResultScore).text = "$score / ${quizList.size}"
        val percent = if(quizList.isEmpty()) 0 else (score * 100 / quizList.size)
        resultView.findViewById<TextView>(R.id.tvResultPercent).text = "$percent%"
        
        val dialog = AlertDialog.Builder(this)
            .setView(resultView)
            .setCancelable(false)
            .create()
        
        resultView.findViewById<Button>(R.id.btnFinishResult).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        
        dialog.show()
    }

    private fun showAddQuestionDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_quiz, null)
        val etQ = view.findViewById<EditText>(R.id.etQuestion)
        val etA = view.findViewById<EditText>(R.id.etOptA)
        val etB = view.findViewById<EditText>(R.id.etOptB)
        val etC = view.findViewById<EditText>(R.id.etOptC)
        val etD = view.findViewById<EditText>(R.id.etOptD)
        val etAns = view.findViewById<EditText>(R.id.etCorrectIndex)

        AlertDialog.Builder(this)
            .setTitle("Add MCQ Question")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val q = etQ.text.toString().trim()
                val options = listOf(etA.text.toString().trim(), etB.text.toString().trim(), etC.text.toString().trim(), etD.text.toString().trim())
                val ansIdx = etAns.text.toString().toIntOrNull() ?: 0
                
                if (q.isNotEmpty()) {
                    AppData.addQuiz(this, Quiz(courseId = courseId, outlineItemId = outlineItemId, question = q, options = options, correctOptionIndex = ansIdx))
                    loadQuizData()
                    displayQuestion()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
