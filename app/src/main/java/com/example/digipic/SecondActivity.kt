package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.index2)

        val button = findViewById<Button>(R.id.getStartedButton)
        button.setOnClickListener {
            // 👇 This opens a third activity (create it next)
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }
    }
}
