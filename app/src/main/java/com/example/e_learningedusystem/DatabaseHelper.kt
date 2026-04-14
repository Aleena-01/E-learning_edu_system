package com.example.e_learningedusystem

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EduVerse.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USERS = "users"
        private const val TABLE_COURSES = "courses"
        private const val TABLE_QUIZZES = "quizzes"
        private const val TABLE_ASSIGNMENTS = "assignments"

        private const val KEY_ID = "id"

        // User
        private const val KEY_USER_NAME = "name"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_PASS = "password"
        private const val KEY_USER_ROLE = "role"
        private const val KEY_USER_EDU = "education"
        private const val KEY_USER_SKILLS = "skills"

        // Course
        private const val KEY_COURSE_TEACHER_ID = "teacher_id"
        private const val KEY_COURSE_TITLE = "title"
        private const val KEY_COURSE_DESC = "description"
        private const val KEY_COURSE_CAT = "category"
        private const val KEY_COURSE_VIDEO = "video_url"

        // Quiz
        private const val KEY_QUIZ_COURSE_ID = "course_id"
        private const val KEY_QUIZ_QUES = "question"
        private const val KEY_QUIZ_A = "opt_a"
        private const val KEY_QUIZ_B = "opt_b"
        private const val KEY_QUIZ_C = "opt_c"
        private const val KEY_QUIZ_D = "opt_d"
        private const val KEY_QUIZ_ANS = "answer"

        // Assignment
        private const val KEY_ASGN_COURSE_ID = "course_id"
        private const val KEY_ASGN_TITLE = "title"
        private const val KEY_ASGN_DESC = "description"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_NAME TEXT, $KEY_USER_EMAIL TEXT, $KEY_USER_PASS TEXT, $KEY_USER_ROLE TEXT, $KEY_USER_EDU TEXT, $KEY_USER_SKILLS TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_COURSES($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_COURSE_TEACHER_ID INTEGER, $KEY_COURSE_TITLE TEXT, $KEY_COURSE_DESC TEXT, $KEY_COURSE_CAT TEXT, $KEY_COURSE_VIDEO TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_QUIZZES($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_QUIZ_COURSE_ID INTEGER, $KEY_QUIZ_QUES TEXT, $KEY_QUIZ_A TEXT, $KEY_QUIZ_B TEXT, $KEY_QUIZ_C TEXT, $KEY_QUIZ_D TEXT, $KEY_QUIZ_ANS TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_ASSIGNMENTS($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_ASGN_COURSE_ID INTEGER, $KEY_ASGN_TITLE TEXT, $KEY_ASGN_DESC TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZZES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ASSIGNMENTS")
        onCreate(db)
    }

    fun addUser(user: User, pass: String): Long {
        val values = ContentValues().apply {
            put(KEY_USER_NAME, user.name)
            put(KEY_USER_EMAIL, user.email)
            put(KEY_USER_PASS, pass)
            put(KEY_USER_ROLE, user.role)
            put(KEY_USER_EDU, user.education)
            put(KEY_USER_SKILLS, user.skills)
        }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun checkUser(email: String, pass: String): User? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_USER_EMAIL=? AND $KEY_USER_PASS=?", arrayOf(email, pass))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), cursor.getString(6))
        }
        cursor.close()
        return user
    }

    fun addCourse(course: Course): Long {
        val values = ContentValues().apply {
            put(KEY_COURSE_TEACHER_ID, course.teacherId)
            put(KEY_COURSE_TITLE, course.title)
            put(KEY_COURSE_DESC, course.description)
            put(KEY_COURSE_CAT, course.category)
            put(KEY_COURSE_VIDEO, course.videoUrl)
        }
        return writableDatabase.insert(TABLE_COURSES, null, values)
    }

    fun getAllCourses(): List<Course> {
        val list = mutableListOf<Course>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_COURSES", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Course(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addQuiz(quiz: Quiz): Long {
        val values = ContentValues().apply {
            put(KEY_QUIZ_COURSE_ID, quiz.courseId)
            put(KEY_QUIZ_QUES, quiz.question)
            put(KEY_QUIZ_A, quiz.optionA)
            put(KEY_QUIZ_B, quiz.optionB)
            put(KEY_QUIZ_C, quiz.optionC)
            put(KEY_QUIZ_D, quiz.optionD)
            put(KEY_QUIZ_ANS, quiz.correctAnswer)
        }
        return writableDatabase.insert(TABLE_QUIZZES, null, values)
    }

    fun getQuizzesByCourse(courseId: Int): List<Quiz> {
        val list = mutableListOf<Quiz>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_QUIZZES WHERE $KEY_QUIZ_COURSE_ID=?", arrayOf(courseId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Quiz(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}