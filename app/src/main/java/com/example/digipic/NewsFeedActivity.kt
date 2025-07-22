package com.example.digipic

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class NewsFeedActivity : AppCompatActivity() {

    private lateinit var likeButton: ImageButton
    private lateinit var commentButton: ImageButton
    private lateinit var commentPreview: TextView
    private var liked = false // Tracks like status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_feed) // Ensure your XML file is named news_feed.xml

        likeButton = findViewById(R.id.likeButton)
        commentButton = findViewById(R.id.commentButton)
        commentPreview = findViewById(R.id.commentPreview)

        // Top-right icon acts as a back button or notification trigger
        findViewById<ImageView>(R.id.notificationIcon).setOnClickListener {
            onBackPressed() // You can change this to open a NotificationsActivity if needed
        }

        likeButton.setOnClickListener {
            liked = !liked
            likeButton.setImageResource(if (liked) R.drawable.ic_liked else R.drawable.ic_like)
            Toast.makeText(this, if (liked) "You liked this" else "Like removed", Toast.LENGTH_SHORT).show()
        }

        commentButton.setOnClickListener {
            // Replace with real comment behavior
            Snackbar.make(it, "Open comment section", Snackbar.LENGTH_SHORT).show()
        }

        // Click on avatar or name leads to profile
        findViewById<ImageView>(R.id.avatar).setOnClickListener {
            goToProfile(it)
        }
        findViewById<TextView>(R.id.publisherName).setOnClickListener {
            goToProfile(it)
        }
    }

    // Click handler for XML-defined onClick="goToProfile"
    fun goToProfile(view: View) {
        // Example navigation; replace with actual logic
        Toast.makeText(this, "Opening profile...", Toast.LENGTH_SHORT).show()
        // startActivity(Intent(this, ProfileActivity::class.java))
    }
}
