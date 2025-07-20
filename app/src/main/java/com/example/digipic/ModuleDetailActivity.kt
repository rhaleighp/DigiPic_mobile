package com.example.digipic

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ModuleDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_info_txt)
// your layout XML

        // Get views
        val headerText = findViewById<TextView>(R.id.title)
        val moduleTitle = findViewById<TextView>(R.id.textTitle)
        val moduleContent = findViewById<TextView>(R.id.textContent)

        val tabIllustrations = findViewById<Button>(R.id.tabIllustrations)
        val tabVideo = findViewById<Button>(R.id.tabVideo)
        val tabText = findViewById<Button>(R.id.tabText)

        // Get data from intent
        val title = intent.getStringExtra("moduleTitle") ?: "Untitled"
        val content = intent.getStringExtra("moduleContent") ?: "No description"
        val type = intent.getStringExtra("moduleType") ?: "text"

        // Populate UI
        headerText.text = type.uppercase()
        moduleTitle.text = title
        moduleContent.text = content

        // Handle tab visibility and styling (optional logic)
        when (type.lowercase()) {
            "text" -> {
                tabText.setBackgroundResource(R.drawable.tab_selected)
                tabText.setTextColor(resources.getColor(android.R.color.white))
            }
            "video" -> {
                tabVideo.setBackgroundResource(R.drawable.tab_selected)
                tabVideo.setTextColor(resources.getColor(android.R.color.white))
                // TODO: Add logic to launch or embed video player
            }
            "illustrations" -> {
                tabIllustrations.setBackgroundResource(R.drawable.tab_selected)
                tabIllustrations.setTextColor(resources.getColor(android.R.color.white))
                // TODO: Add logic for showing image carousel
            }
        }
    }
}
