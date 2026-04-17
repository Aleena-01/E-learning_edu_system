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
    
    private var lastUserId = 0
    private var lastCourseId = 0

    // Initialize and load data from SharedPreferences
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val usersJson = prefs.getString("users", null)
        if (usersJson != null) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            users = gson.fromJson(usersJson, type)
            lastUserId = users.maxByOrNull { it.id }?.id ?: 0
        }

        val coursesJson = prefs.getString("courses", null)
        if (coursesJson != null) {
            val type = object : TypeToken<MutableList<Course>>() {}.type
            courses = gson.fromJson(coursesJson, type)
            lastCourseId = courses.maxByOrNull { it.id }?.id ?: 0
        }

        val enrollJson = prefs.getString("enrollments", null)
        if (enrollJson != null) {
            val type = object : TypeToken<MutableList<Pair<Int, Int>>>() {}.type
            enrollments = gson.fromJson(enrollJson, type)
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

    fun getEnrolledCount(teacherId: Int): Int {
        val teacherCourseIds = courses.filter { it.teacherId == teacherId }.map { it.id }
        return enrollments.count { it.first in teacherCourseIds }
    }
}