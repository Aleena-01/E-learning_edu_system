package com.example.e_learningedusystem

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * This Singleton object replaces the Database.
 * It uses ArrayLists for in-memory storage and GSON for Persistence.
 */
object AppData {
    private const val PREF_NAME = "EduVerseData"
    
    var users = mutableListOf<User>()
    var courses = mutableListOf<Course>()
    var enrollments = mutableListOf<Pair<Int, Int>>() // CourseId, StudentId
    var outlines = mutableListOf<OutlineItem>()
    var quizzes = mutableListOf<Quiz>()
    var submissions = mutableListOf<AssignmentSubmission>()
    var completions = mutableListOf<Pair<Int, Int>>() // OutlineItemId, StudentId
    
    private var lastUserId = 0
    private var lastCourseId = 0
    private var lastOutlineId = 0
    private var lastQuizId = 0
    private var lastSubmissionId = 0

    // Initialize and load data from SharedPreferences
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        prefs.getString("users", null)?.let {
            val type = object : TypeToken<MutableList<User>>() {}.type
            users = gson.fromJson(it, type)
            lastUserId = users.maxByOrNull { it.id }?.id ?: 0
        }

        prefs.getString("courses", null)?.let {
            val type = object : TypeToken<MutableList<Course>>() {}.type
            courses = gson.fromJson(it, type)
            lastCourseId = courses.maxByOrNull { it.id }?.id ?: 0
        }

        prefs.getString("enrollments", null)?.let {
            val type = object : TypeToken<MutableList<Pair<Int, Int>>>() {}.type
            enrollments = gson.fromJson(it, type)
        }

        prefs.getString("outlines", null)?.let {
            val type = object : TypeToken<MutableList<OutlineItem>>() {}.type
            outlines = gson.fromJson(it, type)
            lastOutlineId = outlines.maxByOrNull { it.id }?.id ?: 0
        }

        prefs.getString("quizzes", null)?.let {
            val type = object : TypeToken<MutableList<Quiz>>() {}.type
            quizzes = gson.fromJson(it, type)
            lastQuizId = quizzes.maxByOrNull { it.id }?.id ?: 0
        }

        prefs.getString("submissions", null)?.let {
            val type = object : TypeToken<MutableList<AssignmentSubmission>>() {}.type
            submissions = gson.fromJson(it, type)
            lastSubmissionId = submissions.maxByOrNull { it.id }?.id ?: 0
        }

        prefs.getString("completions", null)?.let {
            val type = object : TypeToken<MutableList<Pair<Int, Int>>>() {}.type
            completions = gson.fromJson(it, type)
        }
    }

    // Call this whenever data changes to save it permanently
    fun saveData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()

        editor.putString("users", gson.toJson(users))
        editor.putString("courses", gson.toJson(courses))
        editor.putString("enrollments", gson.toJson(enrollments))
        editor.putString("outlines", gson.toJson(outlines))
        editor.putString("quizzes", gson.toJson(quizzes))
        editor.putString("submissions", gson.toJson(submissions))
        editor.putString("completions", gson.toJson(completions))
        editor.apply()
    }

    fun addUser(context: Context, user: User): Int {
        lastUserId++
        val newUser = user.copy(id = lastUserId)
        users.add(newUser)
        saveData(context)
        return lastUserId
    }

    fun addCourse(context: Context, course: Course): Int {
        lastCourseId++
        val newCourse = course.copy(id = lastCourseId)
        courses.add(newCourse)
        saveData(context)
        return lastCourseId
    }

    fun updateCourse(context: Context, updatedCourse: Course) {
        val index = courses.indexOfFirst { it.id == updatedCourse.id }
        if (index != -1) {
            courses[index] = updatedCourse
            saveData(context)
        }
    }

    fun deleteCourse(context: Context, courseId: Int) {
        courses.removeAll { it.id == courseId }
        outlines.removeAll { it.courseId == courseId }
        quizzes.removeAll { it.courseId == courseId }
        enrollments.removeAll { it.first == courseId }
        saveData(context)
    }

    fun addOutlineItem(context: Context, item: OutlineItem): Int {
        lastOutlineId++
        val newItem = item.copy(id = lastOutlineId)
        outlines.add(newItem)
        saveData(context)
        return lastOutlineId
    }

    fun addQuiz(context: Context, quiz: Quiz): Int {
        lastQuizId++
        val newQuiz = quiz.copy(id = lastQuizId)
        quizzes.add(newQuiz)
        saveData(context)
        return lastQuizId
    }

    fun addSubmission(context: Context, sub: AssignmentSubmission): Int {
        lastSubmissionId++
        val newSub = sub.copy(id = lastSubmissionId)
        submissions.add(newSub)
        saveData(context)
        return lastSubmissionId
    }

    fun updateSubmissionMarks(context: Context, subId: Int, marks: Int, feedback: String) {
        val index = submissions.indexOfFirst { it.id == subId }
        if (index != -1) {
            submissions[index].marks = marks
            submissions[index].feedback = feedback
            saveData(context)
        }
    }

    fun checkUser(email: String): User? {
        return users.find { it.email == email }
    }

    fun getUserById(id: Int) = users.find { it.id == id }

    fun enrollStudent(context: Context, courseId: Int, studentId: Int) {
        if (!enrollments.contains(Pair(courseId, studentId))) {
            enrollments.add(Pair(courseId, studentId))
            saveData(context)
        }
    }

    fun markItemComplete(context: Context, outlineItemId: Int, studentId: Int) {
        if (!completions.contains(Pair(outlineItemId, studentId))) {
            completions.add(Pair(outlineItemId, studentId))
            saveData(context)
        }
    }

    fun isItemComplete(outlineItemId: Int, studentId: Int): Boolean {
        return completions.contains(Pair(outlineItemId, studentId))
    }

    fun getEnrolledCount(teacherId: Int): Int {
        val teacherCourseIds = courses.filter { it.teacherId == teacherId }.map { it.id }
        return enrollments.count { it.first in teacherCourseIds }
    }
}
