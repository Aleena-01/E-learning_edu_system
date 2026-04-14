package com.example.e_learningedusystem

data class User(
    val id: Int = -1,
    val name: String,
    val email: String,
    val role: String, // "Teacher" or "Student"
    val education: String,
    val skills: String
)

data class Course(
    val id: Int = -1,
    val teacherId: Int,
    val title: String,
    val description: String,
    val category: String, // Link to skills
    val videoUrl: String = ""
)

data class Quiz(
    val id: Int = -1,
    val courseId: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String
)

data class Assignment(
    val id: Int = -1,
    val courseId: Int,
    val title: String,
    val description: String
)