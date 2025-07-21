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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Check SharedPreferences session
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

        // ⛳ Bind views
        userNameText = findViewById(R.id.userNameText)
        progressIndicator = findViewById(R.id.circularProgress)
        dailyTaskTitle = findViewById(R.id.dailyTaskTitle)
        dailyTaskSubtitle = findViewById(R.id.dailyTaskSubtitle)

        // ✅ Load user and progress data
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val userDoc = userDocs.first()
                    val userId = userDoc.id
                    userNameText.text = userDoc.getString("username") ?: "User"

                    // 🔁 Fetch completed modules count
                    firestore.collection("users")
                        .document(userId)
                        .collection("completedModules")
                        .get()
                        .addOnSuccessListener { completedDocs ->
                            val completedCount = completedDocs.size()

                            // 🔁 Fetch total module count
                            firestore.collection("modules")
                                .get()
                                .addOnSuccessListener { allModules ->
                                    val totalModules = allModules.size()

                                    val progressPercent = if (totalModules > 0) {
                                        (completedCount * 100) / totalModules
                                    } else 0

                                    // ✅ Display progress
                                    progressIndicator.progress = progressPercent
                                    dailyTaskTitle.text = "Today's Progress"
                                    dailyTaskSubtitle.text = "Modules Completed: $completedCount / $totalModules"
                                }
                        }
                } else {
                    userNameText.text = "Unknown"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show()
            }

        // ✅ Navigate to courses
        val button = findViewById<ImageButton>(R.id.btnPhotography)
        button.setOnClickListener {
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
        }
    }
}
