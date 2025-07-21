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

        // Get views
        val headerText = findViewById<TextView>(R.id.title)
        val moduleTitle = findViewById<TextView>(R.id.textTitle)
        val moduleContent = findViewById<TextView>(R.id.textContent)
        val tabVideo = findViewById<Button>(R.id.tabVideo)
        val tabText = findViewById<Button>(R.id.tabText)
        markCompletedButton = findViewById(R.id.btnMarkCompleted)

        // Get module data from Intent
        val title = intent.getStringExtra("moduleTitle") ?: "Untitled"
        val content = intent.getStringExtra("moduleContent") ?: "No description"
        val type = intent.getStringExtra("moduleType") ?: "text"
        val moduleId = intent.getStringExtra("moduleId")

        // Get username from SharedPreferences
        val sharedPref = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("userUsername", null)

        // Populate UI
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
                // Optional: video logic
            }
        }

        // ✅ Mark as completed logic
        markCompletedButton.setOnClickListener {
            if (username != null && moduleId != null) {
                // Step 1: Get the user document ID from Firestore
                firestore.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        if (!userDocs.isEmpty) {
                            val userDocId = userDocs.first().id

                            // Step 2: Save completion record
                            val completionData = hashMapOf(
                                "moduleId" to moduleId,
                                "moduleTitle" to title,
                                "completedAt" to System.currentTimeMillis()
                            )

                            firestore.collection("users")
                                .document(userDocId)
                                .collection("completedModules")
                                .document(moduleId)
                                .set(completionData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Module marked as completed!", Toast.LENGTH_SHORT).show()
                                    markCompletedButton.isEnabled = false
                                    markCompletedButton.text = "Completed"
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to mark as completed", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Missing user or module info", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
