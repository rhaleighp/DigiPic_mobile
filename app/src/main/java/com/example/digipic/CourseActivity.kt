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
    private var userCompletedCourses: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_course)

        courseContainer = findViewById(R.id.courseListContainer)
        db = FirebaseFirestore.getInstance()

        loadUserProgress()
    }

    private fun loadUserProgress() {
        val sharedPref = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("userUsername", null)

        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val userDocId = userDocs.first().id
                    db.collection("users")
                        .document(userDocId)
                        .collection("completedCourses")
                        .get()
                        .addOnSuccessListener { completedDocs ->
                            userCompletedCourses = completedDocs.size()
                            fetchCourses()
                        }
                } else {
                    fetchCourses()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch progress", Toast.LENGTH_SHORT).show()
                fetchCourses()
            }
    }

    private fun fetchCourses() {
        db.collection("courses")
            .orderBy("courseNumber")
            .get()
            .addOnSuccessListener { snapshot ->
                courseContainer.removeAllViews()

                for (doc in snapshot) {
                    val courseId = doc.id
                    val title = doc.getString("title") ?: "Untitled"
                    val description = doc.getString("description") ?: "No description"
                    val courseNumber = doc.getLong("courseNumber")?.toInt() ?: 0

                    val courseView = LayoutInflater.from(this)
                        .inflate(R.layout.course_card_item, courseContainer, false)

                    val titleText = courseView.findViewById<TextView>(R.id.courseTitle)
                    val descText = courseView.findViewById<TextView>(R.id.courseDescription)

                    titleText.text = title
                    descText.text = description

                    val isUnlocked = courseNumber <= userCompletedCourses + 1

                    if (isUnlocked) {
                        courseView.setOnClickListener {
                            val intent = Intent(this, LessonAvailableActivity::class.java)
                            intent.putExtra("courseId", courseId)
                            startActivityForResult(intent, 1002)  // ✅ track result
                        }
                    } else {
                        courseView.alpha = 0.5f
                        courseView.isEnabled = false
                    }

                    courseContainer.addView(courseView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load courses: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ✅ Refresh after coming back
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002 && resultCode == RESULT_OK) {
            loadUserProgress()
        }
    }
}
