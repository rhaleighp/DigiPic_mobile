package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameText: TextView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var dailyTaskTitle: TextView
    private lateinit var dailyTaskSubtitle: TextView

    // 🔼 New TextViews for course info
    private lateinit var currentCourseName: TextView
    private lateinit var remainingCourses: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val username = prefs.getString("userUsername", null)

        if (!isLoggedIn || username == null) {
            startActivity(Intent(this, toLogin::class.java))
            finish()
            return
        }

        setContentView(R.layout.home)

        firestore = FirebaseFirestore.getInstance()

        userNameText = findViewById(R.id.userNameText)
        progressIndicator = findViewById(R.id.circularProgress)
        dailyTaskTitle = findViewById(R.id.dailyTaskTitle)
        dailyTaskSubtitle = findViewById(R.id.dailyTaskSubtitle)

        // 🔼 Initialize the new views
        currentCourseName = findViewById(R.id.currentCourseName)
        remainingCourses = findViewById(R.id.remainingCourses)

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val userDoc = userDocs.first()
                    val userId = userDoc.id
                    userNameText.text = userDoc.getString("username") ?: "User"

                    // ✅ Get completedCourses to determine next unlocked course
                    firestore.collection("users").document(userId)
                        .collection("completedCourses")
                        .get()
                        .addOnSuccessListener { completedCourses ->

                            val completedCount = completedCourses.size()
                            val unlockedCourseNumber = completedCount + 1

                            // 🔼 Show how many courses left
                            firestore.collection("courses").get()
                                .addOnSuccessListener { allCourses ->
                                    val totalCourses = allCourses.size()
                                    val remaining = totalCourses - completedCount
                                    remainingCourses.text = "$remaining Course${if (remaining > 1) "s" else ""} to Go"
                                }

                            // ✅ Find the current unlocked course
                            firestore.collection("courses")
                                .whereEqualTo("courseNumber", unlockedCourseNumber)
                                .get()
                                .addOnSuccessListener { courseDocs ->
                                    if (!courseDocs.isEmpty) {
                                        val courseDoc = courseDocs.first()
                                        val courseId = courseDoc.id

                                        // 🔼 Set current course name
                                        val courseTitle = courseDoc.getString("title") ?: "Unknown Course"
                                        currentCourseName.text = "Current Course: $courseTitle"

                                        // ✅ Get modules for this course
                                        firestore.collection("modules")
                                            .whereEqualTo("courseId", courseId)
                                            .get()
                                            .addOnSuccessListener { allModules ->
                                                val totalModules = allModules.size()
                                                if (totalModules == 0) {
                                                    progressIndicator.progress = 0
                                                    dailyTaskTitle.text = "No modules available"
                                                    dailyTaskSubtitle.text = "Please check back later"
                                                    return@addOnSuccessListener
                                                }

                                                // ✅ Get user's completed modules for this course
                                                firestore.collection("users")
                                                    .document(userId)
                                                    .collection("completedModules")
                                                    .whereEqualTo("courseId", courseId)
                                                    .get()
                                                    .addOnSuccessListener { completedModules ->
                                                        val completedCountModules = completedModules.size()

                                                        val progressPercent = (completedCountModules * 100) / totalModules
                                                        progressIndicator.progress = progressPercent
                                                        dailyTaskTitle.text = "Today's Progress"
                                                        dailyTaskSubtitle.text = "Modules Completed: $completedCountModules / $totalModules"
                                                    }
                                            }
                                    } else {
                                        currentCourseName.text = "No unlocked course"
                                        dailyTaskTitle.text = "No unlocked course"
                                        dailyTaskSubtitle.text = "Complete previous course first"
                                    }
                                }
                        }
                } else {
                    userNameText.text = "Unknown"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show()
            }

        // ➡ Navigate to course list
        findViewById<ImageButton>(R.id.btnPhotography).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }

        // Navigate to Bottom Navigation
        findViewById<ImageButton>(R.id.mainGallery).setOnClickListener {
            Toast.makeText(this, "Main Gallery clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.navLessons).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }

        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

}
