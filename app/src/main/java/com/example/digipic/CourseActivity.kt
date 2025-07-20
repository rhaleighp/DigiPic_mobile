package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CourseActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var courseContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_course)

        courseContainer = findViewById(R.id.courseListContainer)

        db = FirebaseFirestore.getInstance()

        fetchCourses()
    }

    private fun fetchCourses() {
        db.collection("courses")
            .get()
            .addOnSuccessListener { snapshot ->
                courseContainer.removeAllViews()
                for (doc in snapshot) {
                    val courseId = doc.id
                    val title = doc.getString("title") ?: "Untitled"
                    val description = doc.getString("description") ?: "No description"

                    val courseView = LayoutInflater.from(this)
                        .inflate(R.layout.course_card_item, courseContainer, false)

                    courseView.findViewById<TextView>(R.id.courseTitle).text = title
                    courseView.findViewById<TextView>(R.id.courseDescription).text = description

                    courseView.setOnClickListener {
                        val intent = Intent(this, LessonAvailableActivity::class.java)
                        intent.putExtra("courseId", courseId)
                        startActivity(intent)
                    }

                    courseContainer.addView(courseView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load courses: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
