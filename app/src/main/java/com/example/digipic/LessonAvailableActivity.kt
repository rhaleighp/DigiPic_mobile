package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView

class LessonAvailableActivity : AppCompatActivity() {

    private lateinit var moduleContainer: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var completedModules = mutableSetOf<String>()
    private var courseId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_available)

        moduleContainer = findViewById(R.id.moduleContainer)
        db = FirebaseFirestore.getInstance()

        courseId = intent.getStringExtra("courseId") ?: return

        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)
        tabCompleted.setOnClickListener {
            startActivity(Intent(this, LessonCompletedActivity::class.java))
        }

        val username = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
            .getString("userUsername", null)

        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔁 Load on start
        setupBottomNavigation()
        fetchUserAndCompletedModules(username)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            completedModules.clear()
            db.collection("users").document(userId).collection("completedModules")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener { completed ->
                    for (doc in completed) {
                        completedModules.add(doc.id)
                    }


                    loadModulesForCourse()
                }
        }
    }

    private fun fetchUserAndCompletedModules(username: String) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    userId = userDocs.first().id

                    db.collection("users").document(userId).collection("completedModules")
                        .whereEqualTo("courseId", courseId)
                        .get()
                        .addOnSuccessListener { completed ->
                            for (doc in completed) {
                                completedModules.add(doc.id)
                            }
                            loadModulesForCourse()
                        }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadModulesForCourse() {
        db.collection("modules")
            .whereEqualTo("courseId", courseId)
            .orderBy("moduleNumber")
            .get()
            .addOnSuccessListener { docs ->
                moduleContainer.removeAllViews()

                if (docs.isEmpty) {
                    Toast.makeText(this, "No modules found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var previousCompleted = true

                for (doc in docs) {
                    val moduleId = doc.id
                    val moduleTitle = doc.getString("title") ?: "Untitled"
                    val moduleType = doc.getString("type") ?: "Text"
                    val moduleDesc = doc.getString("description") ?: ""

                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.module_card_item, moduleContainer, false)

                    view.findViewById<TextView>(R.id.moduleTitle).text = moduleTitle
                    view.findViewById<TextView>(R.id.moduleType).text = moduleType

                    val isCompleted = completedModules.contains(moduleId)

                    if (!previousCompleted) {
                        view.alpha = 0.5f
                        view.isEnabled = false
                    } else {
                        view.setOnClickListener {
                            val intent = Intent(this, ModuleDetailActivity::class.java)
                            intent.putExtra("moduleId", moduleId)
                            intent.putExtra("moduleTitle", moduleTitle)
                            intent.putExtra("moduleContent", moduleDesc)
                            intent.putExtra("moduleType", moduleType)
                            intent.putExtra("courseId", courseId)
                            startActivityForResult(intent, 1001)
                        }
                    }

                    if (!isCompleted) previousCompleted = false

                    moduleContainer.addView(view)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load modules: ${it.message}", Toast.LENGTH_SHORT).show()
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
