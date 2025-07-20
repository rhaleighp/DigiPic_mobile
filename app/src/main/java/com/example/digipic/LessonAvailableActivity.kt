package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LessonAvailableActivity : AppCompatActivity() {

    private lateinit var moduleContainer: LinearLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_available)

        moduleContainer = findViewById(R.id.moduleContainer)
        db = FirebaseFirestore.getInstance()

        val courseId = intent.getStringExtra("courseId") ?: return

        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)
        tabCompleted.setOnClickListener {
            startActivity(Intent(this, LessonCompletedActivity::class.java))
        }

        loadModulesForCourse(courseId)
    }

    private fun loadModulesForCourse(courseId: String) {
        db.collection("modules")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { docs ->
                moduleContainer.removeAllViews()

                if (docs.isEmpty) {
                    Toast.makeText(this, "No modules found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (doc in docs) {
                    val moduleTitle = doc.getString("title") ?: "Untitled Module"
                    val moduleType = doc.getString("type") ?: "Text"
                    val moduleDescription = doc.getString("description") ?: "No description provided"

                    val view = LayoutInflater.from(this).inflate(R.layout.module_card_item, moduleContainer, false)

                    view.findViewById<TextView>(R.id.moduleTitle).text = moduleTitle
                    view.findViewById<TextView>(R.id.moduleType).text = moduleType

                    view.setOnClickListener {
                        val intent = Intent(this, ModuleDetailActivity::class.java)
                        intent.putExtra("moduleTitle", moduleTitle)
                        intent.putExtra("moduleContent", moduleDescription) // You could also use "filePath" or "videoUrl" here
                        intent.putExtra("moduleType", moduleType)
                        startActivity(intent)
                    }

                    moduleContainer.addView(view)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load modules: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
