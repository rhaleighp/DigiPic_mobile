package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class toLogin : AppCompatActivity() {

    private val client = OkHttpClient()
    private val serverUrl = "http://10.0.2.2:5000/login" // Your backend login endpoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.getStartedButton)
        val tvSignup = findViewById<TextView>(R.id.tvSignup)
        val forgetPass = findViewById<TextView>(R.id.tvForgot)

        tvSignup.setOnClickListener {
            val intent = Intent(this, toSignup::class.java)
            startActivity(intent)
        }

        forgetPass.setOnClickListener {
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            val body = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@toLogin, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // ✅ Save session using SharedPreferences
                            val sharedPrefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
                            sharedPrefs.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putString("userUsername", username) // 🔑 Save username
                                apply()
                            }

                            Toast.makeText(this@toLogin, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@toLogin, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@toLogin, "Login failed: $responseBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }
    }
}
