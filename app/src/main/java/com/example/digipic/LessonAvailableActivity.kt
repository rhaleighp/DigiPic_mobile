package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LessonAvailableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_available)

        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)

        tabCompleted.setOnClickListener {
            val intent = Intent(this, LessonCompletedActivity::class.java)
            startActivity(intent)
        }
    }
}
