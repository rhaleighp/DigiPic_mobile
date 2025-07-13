package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class toSignup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val signupText = findViewById<TextView>(R.id.tvLoginClickable)
        signupText.setOnClickListener {
            val intent = Intent(this, toLogin::class.java)
            startActivity(intent)
        }
    }
}
