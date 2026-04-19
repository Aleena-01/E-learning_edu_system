package com.example.e_learningedusystem

data class User(
    val id: Int = -1,
    val name: String,
    val email: String,
    val role: String, // "Teacher" or "Student"
    val password: String = "",
    val qualification: String = "",
    val experience: String = "",
    val expertise: String = "",
    val educationLevel: String = "",
    val interests: String = "",
    val bio: String = ""
)

data class Course(
    var id: Int = -1,
    val teacherId: Int,
    var title: String,
    var description: String,
    var category: String,
    var duration: String,
    val teacherName: String = ""
)

data class OutlineItem(
    var id: Int = -1,
    val courseId: Int,
    var title: String,
    val type: String, // "Video", "Document", "Assignment", "Quiz"
    var contentUri: String = "", // File path, Gallery URI, or Link
    var weekOrDay: String = "",
    var description: String = "" // For assignments or instructions
)

data class Quiz(
    val id: Int = -1,
    val courseId: Int,
    val outlineItemId: Int = -1,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

data class QuizResult(
    val id: Int = -1,
    val outlineItemId: Int,
    val studentId: Int,
    val score: Int,
    val total: Int
)

data class AssignmentSubmission(
    val id: Int = -1,
    val outlineItemId: Int,
    val studentId: Int,
    val studentName: String,
    val submissionText: String = "",
    val fileUri: String = "",
    var marks: Int = -1,
    var totalMarks: Int = 100,
    var feedback: String = ""
)
