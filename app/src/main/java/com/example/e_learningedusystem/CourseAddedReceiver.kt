package com.example.e_learningedusystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class CourseAddedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("com.example.EDU_VERSE_COURSE_ADDED" == intent.action) {
            Toast.makeText(context, "Custom Broadcast: New Course successfully added to EduVerse!", Toast.LENGTH_LONG).show()
        }
    }
}