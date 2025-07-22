package com.example.digipic

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ModuleDetailActivity : AppCompatActivity() {

    private lateinit var markCompletedButton: Button
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

        if (username != null && moduleId != null && courseId != null) {
            // ✅ Check if this module is already completed and update button state
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
                                    // 🔒 Already completed
                                    markCompletedButton.isEnabled = false
                                    markCompletedButton.text = "Completed"
                                } else {
                                    // ✅ Allow marking as completed
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
}
