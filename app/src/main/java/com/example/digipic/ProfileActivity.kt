package com.example.digipic

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var usernameText: TextView
    private lateinit var postsCount: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Bind views
        usernameText = findViewById(R.id.userNameText)
        postsCount = findViewById(R.id.postsCount)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        profileImage = findViewById(R.id.profileImage)

        // Get username from SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = prefs.getString("userUsername", null)

        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Show username
        usernameText.text = username

        // Get user data from Firestore
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    postsCount.text = document.getLong("postsCount")?.toString() ?: "0"
                    followersCount.text = document.getLong("followersCount")?.toString() ?: "0"
                    followingCount.text = document.getLong("followingCount")?.toString() ?: "0"
                    // TODO: Load profile picture using Glide or Picasso if stored in Firestore
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }

    }

}
