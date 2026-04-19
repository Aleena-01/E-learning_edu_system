package com.example.e_learningedusystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class QuizActivity : AppCompatActivity() {

    private var courseId: Int = -1
    private var outlineItemId: Int = -1
    private var quizList = mutableListOf<Quiz>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var isTeacher = false

    private lateinit var tvQuestion: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var btnAddQuiz: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        courseId = intent.getIntExtra("courseId", -1)
        outlineItemId = intent.getIntExtra("outlineItemId", -1)
        isTeacher = AppData.getUserById(intent.getIntExtra("userId", -1))?.role == "Teacher"

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
        rgOptions = findViewById(R.id.rgOptions)
        btnNext = findViewById(R.id.btnNext)
        btnAddQuiz = Button(this).apply { 
            text = "Add Question (Teacher Only)"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120)
            setOnClickListener { showAddQuestionDialog() }
        }
        
        if (isTeacher) {
            findViewById<LinearLayout>(R.id.llQuizContainer).addView(btnAddQuiz, 0)
        }

        btnNext.setOnClickListener { handleNextClick() }
    }

    private fun loadQuizData() {
        quizList = AppData.quizzes.filter { it.outlineItemId == outlineItemId }.toMutableList()
    }

    private fun displayQuestion() {
        if (quizList.isEmpty()) {
            tvQuestion.text = if (isTeacher) "No questions added. Click the button above to add one." else "No questions available for this quiz yet."
            btnNext.visibility = View.GONE
            return
        }

        btnNext.visibility = View.VISIBLE
        val quiz = quizList[currentQuestionIndex]
        tvQuestion.text = "Question ${currentQuestionIndex + 1}/${quizList.size}\n\n${quiz.question}"
        
        rgOptions.removeAllViews()
        quiz.options.forEachIndexed { index, option ->
            val rb = RadioButton(this)
            rb.text = option
            rb.id = index
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
        AlertDialog.Builder(this)
            .setTitle("Quiz Result")
            .setMessage("Score: $score/${quizList.size}")
            .setPositiveButton("OK") { _, _ -> finish() }.show()
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
            .setTitle("Add MCQ")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val q = etQ.text.toString()
                val options = listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString())
                val ansIdx = etAns.text.toString().toIntOrNull() ?: 0
                
                val newQuiz = Quiz(courseId = courseId, outlineItemId = outlineItemId, question = q, options = options, correctOptionIndex = ansIdx)
                AppData.addQuiz(this, newQuiz)
                loadQuizData()
                displayQuestion()
            }.show()
    }
}