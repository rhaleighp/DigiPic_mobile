package com.example.digipic

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import android.widget.ImageView

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchSounds: Switch
    private lateinit var logoutButton: Button
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        sharedPrefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)

        // Initialize sound switch
        switchSounds = findViewById(R.id.switchSounds)
        val isSoundEnabled = sharedPrefs.getBoolean("soundsEnabled", true)
        switchSounds.isChecked = isSoundEnabled

        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit {
                putBoolean("soundsEnabled", isChecked)
            }
            Toast.makeText(this, if (isChecked) "Sounds enabled" else "Sounds disabled", Toast.LENGTH_SHORT).show()
        }

        // Logout button
        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            sharedPrefs.edit {
                clear()
            }
            val intent = Intent(this, toLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Top icon (acts like a back button)
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            onBackPressed()
        }

        // Setup bottom navigation
        setupBottomNavigation()

    }

    // ✅ Function moved outside of onCreate
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
        findViewById<TextView>(R.id.accountOption).setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }
    }
}
