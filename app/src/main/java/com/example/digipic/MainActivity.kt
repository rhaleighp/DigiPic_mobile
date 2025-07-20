package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.index)

        val getStartedButton = findViewById<Button>(R.id.getStartedButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        getStartedButton.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, toLogin::class.java)
            startActivity(intent)
        }
    }
}
