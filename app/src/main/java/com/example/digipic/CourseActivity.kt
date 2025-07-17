package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class CourseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_course)

        val cardBasic = findViewById<LinearLayout>(R.id.cardBasic)

        cardBasic.setOnClickListener {
            val intent = Intent(this, LessonAvailableActivity::class.java)
            startActivity(intent)
        }
    }
}
