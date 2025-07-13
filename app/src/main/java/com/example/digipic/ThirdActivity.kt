package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.index3)

        val button = findViewById<Button>(R.id.getStartedButton)
        button.setOnClickListener {
            // 👇 This opens a third activity (create it next)
            val intent = Intent(this, LastActivity::class.java)
            startActivity(intent)
        }
    }
}
