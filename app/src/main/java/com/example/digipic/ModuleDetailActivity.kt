package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView

class ModuleDetailActivity : AppCompatActivity() {

    private lateinit var markCompletedButton: Button
    private lateinit var quizButton: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_info_txt)

        val headerText = findViewById<TextView>(R.id.title)
        val moduleTitle = findViewById<TextView>(R.id.textTitle)
        val moduleContent = findViewById<TextView>(R.id.textContent)
        val tabVideo = findViewById<Button>(R.id.tabVideo)
        val tabText = findViewById<Button>(R.id.tabText)
        markCompletedButton = findViewById(R.id.btnMarkCompleted)
        quizButton = findViewById(R.id.quizBtn)

        val title = intent.getStringExtra("moduleTitle") ?: "Untitled"
        val content = intent.getStringExtra("moduleContent") ?: "No description"
        val type = intent.getStringExtra("moduleType") ?: "text"
        val moduleId = intent.getStringExtra("moduleId")
        val courseId = intent.getStringExtra("courseId")

        val sharedPref = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("userUsername", null)

        headerText.text = type.uppercase()
        moduleTitle.text = title
        moduleContent.text = content

        setupBottomNavigation()

        when (type.lowercase()) {
            "text" -> {
                tabText.setBackgroundResource(R.drawable.tab_selected)
                tabText.setTextColor(resources.getColor(android.R.color.white))
            }
            "video" -> {
                tabVideo.setBackgroundResource(R.drawable.tab_selected)
                tabVideo.setTextColor(resources.getColor(android.R.color.white))
            }
        }

        // 🔍 Check if quiz exists and enable/disable the button accordingly
        if (courseId != null && moduleId != null) {
            firestore.collection("quizzes")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("moduleId", moduleId)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        quizButton.isEnabled = false
                        quizButton.alpha = 0.5f
                    } else {
                        val quizId = snapshot.documents.first().id
                        quizButton.setOnClickListener {
                            val intent = Intent(this, QuizActivity::class.java)
                            intent.putExtra("quizId", quizId)
                            intent.putExtra("moduleId", moduleId)
                            intent.putExtra("courseId", courseId)
                            startActivity(intent)
                        }
                    }
                }
        }

        // 🔒 Completion tracking
        if (username != null && moduleId != null && courseId != null) {
            firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { userDocs ->
                    if (!userDocs.isEmpty) {
                        val userDocId = userDocs.first().id

                        firestore.collection("users")
                            .document(userDocId)
                            .collection("completedModules")
                            .document(moduleId)
                            .get()
                            .addOnSuccessListener { completedDoc ->
                                if (completedDoc.exists()) {
                                    markCompletedButton.isEnabled = false
                                    markCompletedButton.text = "Completed"
                                } else {
                                    markCompletedButton.setOnClickListener {
                                        val completionData = hashMapOf(
                                            "completedAt" to System.currentTimeMillis(),
                                            "courseId" to courseId
                                        )

                                        setResult(RESULT_OK)

                                        firestore.collection("users")
                                            .document(userDocId)
                                            .collection("completedModules")
                                            .document(moduleId)
                                            .set(completionData)
                                            .addOnSuccessListener {
                                                setResult(RESULT_OK)
                                                Toast.makeText(this, "Module marked as completed!", Toast.LENGTH_SHORT).show()
                                                markCompletedButton.isEnabled = false
                                                markCompletedButton.text = "Completed"
                                                checkAllModulesCompleted(userDocId, courseId)
                                            }
                                    }
                                }
                            }
                    }
                }
        } else {
            Toast.makeText(this, "Missing user or module info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAllModulesCompleted(userDocId: String, courseId: String) {
        firestore.collection("modules")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { allModules ->
                firestore.collection("users")
                    .document(userDocId)
                    .collection("completedModules")
                    .get()
                    .addOnSuccessListener { completedModules ->
                        val allModuleIds = allModules.map { it.id }
                        val completedIds = completedModules.map { it.id }

                        if (allModuleIds.all { completedIds.contains(it) }) {
                            val courseCompletion = hashMapOf(
                                "courseId" to courseId,
                                "completedAt" to System.currentTimeMillis()
                            )
                            firestore.collection("users")
                                .document(userDocId)
                                .collection("completedCourses")
                                .document(courseId)
                                .set(courseCompletion)
                        }
                    }
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
