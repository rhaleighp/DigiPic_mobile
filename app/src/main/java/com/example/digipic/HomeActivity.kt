package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameText: TextView

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
        userNameText = findViewById(R.id.userNameText)

        // ✅ Fetch user's data using username
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val usernameFetched = documents.first().getString("username")
                    userNameText.text = usernameFetched ?: "User"
                } else {
                    userNameText.text = "Unknown"
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                userNameText.text = "Error"
                Toast.makeText(this, "Error loading user: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // ✅ Navigate to courses
        val button = findViewById<ImageButton>(R.id.btnPhotography)
        button.setOnClickListener {
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
        }
    }
}
