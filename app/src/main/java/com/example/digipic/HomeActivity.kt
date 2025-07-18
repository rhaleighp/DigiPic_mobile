package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageButton


class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // ✅ Session check
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, toLogin::class.java))
            finish()
            return
        }

        // ✅ User is logged in, show layout
        setContentView(R.layout.home)

        // Bind views
        userNameText = findViewById(R.id.userNameText)

        // ✅ Fetch username from Firestore
        val uid = currentUser.uid
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    userNameText.text = username ?: "User"
                } else {
                    userNameText.text = "Unknown"
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                userNameText.text = "Error"
                Toast.makeText(this, "Error loading username: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val button = findViewById<ImageButton>(R.id.btnPhotography)
        button.setOnClickListener {
            // 👇 This opens a third activity (create it next)
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
        }
    }
}
