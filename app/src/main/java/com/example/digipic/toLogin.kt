package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class toLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val signupText = findViewById<TextView>(R.id.tvSignup)
        signupText.setOnClickListener {
            val intent = Intent(this, toSignup::class.java) // 👈 toSignup must exist
            startActivity(intent)
        }
    }
}
