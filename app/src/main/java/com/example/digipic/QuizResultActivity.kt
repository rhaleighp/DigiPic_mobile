package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

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
            setupBottomNavigation()
        }
    }
    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.mainGallery).setOnClickListener {
            startActivity(Intent(this, NewsFeedActivity::class.java))
        }

        findViewById<ImageView>(R.id.navLessons).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }
    }
}
