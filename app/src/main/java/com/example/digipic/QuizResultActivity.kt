package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_result)

        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 0)
        val courseId = intent.getStringExtra("courseId") ?: ""

        val scoreText = findViewById<TextView>(R.id.scoreText)
        val backButton = findViewById<Button>(R.id.backButton)

        scoreText.text = "You scored $score out of $total"

        backButton.setOnClickListener {
            val intent = Intent(this, LessonAvailableActivity::class.java)
            intent.putExtra("courseId", courseId)
            startActivity(intent)
            finish()
        }
    }
}
